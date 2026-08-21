package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.models.*
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingWorkPolicy
import com.example.myapplication.data.*
import com.example.myapplication.utils.NetworkObserver
import kotlinx.serialization.encodeToString

class ClubViewModel(application: Application) : AndroidViewModel(application) {

    // --- НАСТРОЙКИ SUPABASE ---
    private val SUPABASE_URL = "https://htkoyibefvqvaisbolga.supabase.co"
    private val SUPABASE_KEY = "sb_publishable_m0hy1ji5NvVqxDjvQ5Tovg_7K7JHZ_V"

    private val client = createSupabaseClient(SUPABASE_URL, SUPABASE_KEY) {
        install(Postgrest)
        install(Realtime)
    }

    private val pendingManager = PendingActionManager(application)
    private val networkObserver = NetworkObserver(application)

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        isLenient = true
    }

    private val _consoles = MutableStateFlow<List<Console>>(emptyList())
    val consoles: StateFlow<List<Console>> = _consoles.asStateFlow()

    private val _barItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val barItems: StateFlow<List<InventoryItem>> = _barItems.asStateFlow()

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _dailyReports = MutableStateFlow<List<DailyReport>>(emptyList())
    val dailyReports: StateFlow<List<DailyReport>> = _dailyReports.asStateFlow()

    private val _totalRevenue = MutableStateFlow(0.0)
    val totalRevenue: StateFlow<Double> = _totalRevenue.asStateFlow()
    
    private val _totalProfit = MutableStateFlow(0.0)
    val totalProfit: StateFlow<Double> = _totalProfit.asStateFlow()

    private val _currentLanguage = MutableStateFlow(Language.RU)
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()

    private val _currentTime = MutableStateFlow(LocalDateTime.now())
    val currentTime = _currentTime.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline = _isOnline.asStateFlow()

    val notificationEvents = MutableSharedFlow<String>()
    val uiEvents = MutableSharedFlow<String>()

    private val commonRate = 1500.0
    private val vipRate = 2500.0

    init {
        // Очистка локальных предпочтений
        application.getSharedPreferences("vitsa_prefs", android.content.Context.MODE_PRIVATE)
            .edit().clear().apply()

        fetchInitialData()
        setupRealtime()
        
        viewModelScope.launch {
            networkObserver.isConnected.collect { connected ->
                _isOnline.value = connected
                if (connected) {
                    scheduleSync()
                    refreshData()
                }
            }
        }

        viewModelScope.launch {
            while (true) {
                val now = LocalDateTime.now()
                _currentTime.value = now
                checkNotifications(now)
                delay(1000)
            }
        }
    }

    private fun fetchInitialData() {
        fetchConsoles()
        fetchInventory()
        fetchTransactions()
        fetchReports()
    }

    private fun fetchConsoles() {
        viewModelScope.launch {
            try {
                val consolesList = client.from("consoles").select().decodeList<Console>()
                if (consolesList.isEmpty()) {
                    val defaultConsoles = listOf(
                        Console(1, "1", ConsoleType.COMMON),
                        Console(2, "2", ConsoleType.COMMON),
                        Console(3, "3", ConsoleType.COMMON),
                        Console(4, "1", ConsoleType.VIP)
                    )
                    defaultConsoles.forEach { client.from("consoles").insert(it) }
                    _consoles.value = defaultConsoles
                } else {
                    _consoles.value = consolesList.sortedBy { it.id }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun fetchInventory() {
        viewModelScope.launch {
            try {
                val barItemsList = client.from("inventory_items").select().decodeList<InventoryItem>()
                _barItems.value = barItemsList.sortedBy { it.name }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun fetchTransactions() {
        viewModelScope.launch {
            try {
                val txList = client.from("transactions").select().decodeList<Transaction>()
                _transactions.value = txList.filter { it.totalAmount > 0.0 }.sortedByDescending { it.id }
                recalculateStats()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun fetchReports() {
        viewModelScope.launch {
            try {
                val reportsList = client.from("daily_reports").select().decodeList<DailyReport>()
                _dailyReports.value = reportsList.sortedByDescending { it.id }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun setupRealtime() {
        viewModelScope.launch {
            val channel = client.realtime.channel("vitsa-realtime")
            val consoleFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") { table = "consoles" }
            val txFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") { table = "transactions" }
            val reportFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") { table = "daily_reports" }
            val inventoryFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") { table = "inventory_items" }

            channel.subscribe()

            launch {
                consoleFlow.collect {
                    val updated = client.from("consoles").select().decodeList<Console>()
                    _consoles.value = updated.sortedBy { it.id }
                }
            }
            launch {
                txFlow.collect {
                    val updated = client.from("transactions").select().decodeList<Transaction>()
                    _transactions.value = updated.sortedByDescending { it.id }
                    recalculateStats()
                }
            }
            launch {
                reportFlow.collect {
                    val updated = client.from("daily_reports").select().decodeList<DailyReport>()
                    _dailyReports.value = updated.sortedByDescending { it.id }
                }
            }
            launch {
                inventoryFlow.collect {
                    val updated = client.from("inventory_items").select().decodeList<InventoryItem>()
                    _barItems.value = updated.sortedBy { it.name }
                }
            }
        }
    }

    private fun recalculateStats() {
        _totalRevenue.value = _transactions.value.sumOf { it.totalAmount }
        _totalProfit.value = _transactions.value.sumOf { it.totalAmount - it.totalCost }
    }

    private suspend fun checkNotifications(now: LocalDateTime) {
        _consoles.value.forEach { console ->
            val session = console.currentSession ?: return@forEach
            if (session.endTime != null && !session.notifiedFiveMin) {
                val end = try { LocalDateTime.parse(session.endTime) } catch(e: Exception) { null } ?: return@forEach
                val diff = Duration.between(now, end)
                if (diff.toMinutes() in 0..5) {
                    notificationEvents.emit("${if (console.type == ConsoleType.VIP) "ВИП" else "Общий"} ${console.name}")
                    val updatedSession = session.copy(notifiedFiveMin = true)
                    updateConsoleInDb(console.id) { it.copy(currentSession = updatedSession) }
                }
            }
        }
    }

    private fun updateConsoleInDb(id: Int, transform: (Console) -> Console) {
        val current = _consoles.value.find { it.id == id } ?: return
        val updated = transform(current)
        performAction(
            tableName = "consoles",
            operation = "UPDATE",
            data = updated,
            filterColumn = "id",
            filterValue = id.toString(),
            onSuccess = { fetchConsoles() }
        )
    }

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == Language.RU) Language.KK else Language.RU
    }

    fun refreshData() {
        fetchInitialData()
    }

    fun addToCart(item: CartItem) {
        if (item is CartItem.SessionCart) {
            val alreadyHasSession = _cart.value.any { 
                it is CartItem.SessionCart && it.consoleId == item.consoleId 
            }
            if (alreadyHasSession) return
        }
        _cart.value = _cart.value + item
    }

    fun removeFromCart(index: Int) {
        _cart.value = _cart.value.filterIndexed { i, _ -> i != index }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun checkout() {
        viewModelScope.launch {
            val currentCart = _cart.value
            val transactionItems = mutableListOf<TransactionItem>()
            var totalAmount = 0.0
            var totalCost = 0.0
            
            for (item in currentCart) {
                totalAmount += item.price
                when (item) {
                    is CartItem.SessionCart -> {
                        startSession(item.consoleId, item.durationMinutes, item.fixedPrice, item.packageName)
                        if (item.price > 0.0) {
                            transactionItems.add(TransactionItem(item.title, item.price, 0.0))
                        }
                    }
                    is CartItem.BarCart -> {
                        val invItem = _barItems.value.find { it.id == item.itemId }
                        val cost = (invItem?.costPrice ?: 0.0) * item.quantity
                        totalCost += cost
                        
                        if (item.consoleId != null) {
                            addExtraToConsole(item.consoleId, item.title, item.price, invItem?.costPrice ?: 0.0)
                        }
                        
                        // Списываем остаток
                        if (invItem != null) {
                            val newStock = (invItem.stock - item.quantity).coerceAtLeast(0)
                            client.from("inventory_items").update(mapOf("stock" to newStock)) {
                                filter { eq("id", item.itemId) }
                            }
                        }
                        transactionItems.add(TransactionItem(item.title, item.price, cost))
                    }
                }
            }
            
            if (totalAmount > 0.0 && transactionItems.isNotEmpty()) {
                val transaction = Transaction(
                    id = System.currentTimeMillis(),
                    totalAmount = totalAmount,
                    totalCost = totalCost,
                    timestamp = LocalDateTime.now().toString(),
                    items = transactionItems
                )
                
                performAction(
                    tableName = "transactions",
                    operation = "INSERT",
                    data = transaction,
                    onSuccess = {
                        fetchTransactions()
                        fetchConsoles()
                        fetchInventory()
                    }
                )
            }
            
            clearCart()
        }
    }

    private suspend fun startSession(consoleId: Int, duration: Int?, price: Double?, name: String?) {
        val startTime = LocalDateTime.now()
        val endTime = duration?.let { startTime.plusMinutes(it.toLong()) }
        val console = _consoles.value.find { it.id == consoleId } ?: return
        val rate = if (console.type == ConsoleType.VIP) vipRate else commonRate

        val newSession = Session(
            startTime = startTime.toString(),
            endTime = endTime?.toString(),
            durationMinutes = duration,
            hourlyRate = rate,
            fixedPrice = price,
            packageName = name,
            isPaid = true
        )
        
        performAction(
            tableName = "consoles",
            operation = "UPDATE",
            data = buildJsonObject {
                put("status", "BUSY")
                put("current_session", json.encodeToJsonElement(newSession))
            }.toString(), // Используем toString() для jsonData
            filterColumn = "id",
            filterValue = consoleId.toString(),
            onSuccess = { fetchConsoles() }
        )
    }

    private suspend fun addExtraToConsole(consoleId: Int, name: String, price: Double, costPrice: Double) {
        val console = _consoles.value.find { it.id == consoleId } ?: return
        val session = console.currentSession ?: return
        val updatedSession = session.copy(extras = session.extras + ExtraItem(name, price, costPrice))
        
        performAction(
            tableName = "consoles",
            operation = "UPDATE",
            data = buildJsonObject {
                put("current_session", json.encodeToJsonElement(updatedSession))
            }.toString(),
            filterColumn = "id",
            filterValue = consoleId.toString(),
            onSuccess = { fetchConsoles() }
        )
    }

    fun stopSession(consoleId: Int) {
        viewModelScope.launch {
            val console = _consoles.value.find { it.id == consoleId } ?: return@launch
            val session = console.currentSession ?: return@launch
            
            try {
                if (session.fixedPrice == null) {
                    val amount = calculateCurrentCost(console)
                    val totalExtrasCost = session.extras.sumOf { it.costPrice }
                    val title = "ДО УПОРА: ${if(console.type == ConsoleType.VIP) "ВИП" else "Общий"} ${console.name}"
                    
                    val transaction = Transaction(
                        id = System.currentTimeMillis(),
                        totalAmount = amount,
                        totalCost = totalExtrasCost,
                        timestamp = LocalDateTime.now().toString(),
                        items = listOf(TransactionItem(title, amount, 0.0))
                    )
                    performAction("transactions", "INSERT", transaction)
                }
                
                performAction(
                    tableName = "consoles",
                    operation = "UPDATE",
                    data = buildJsonObject {
                        put("status", "FREE")
                        put("current_session", JsonNull)
                        put("booked_by", JsonNull)
                    }.toString(),
                    filterColumn = "id",
                    filterValue = consoleId.toString(),
                    onSuccess = {
                        fetchConsoles()
                        fetchTransactions()
                    }
                )
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun bookConsole(consoleId: Int, bookedBy: String) {
        performAction(
            tableName = "consoles",
            operation = "UPDATE",
            data = buildJsonObject {
                put("status", "BOOKED")
                put("booked_by", bookedBy)
            }.toString(),
            filterColumn = "id",
            filterValue = consoleId.toString(),
            onSuccess = { fetchConsoles() }
        )
    }

    fun addInventoryItem(name: String, category: String, price: Double, costPrice: Double, stock: Int) {
        val item = InventoryItem(
            id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
            name = name.uppercase(),
            category = category.uppercase(),
            price = price,
            costPrice = costPrice,
            stock = stock
        )
        performAction(
            tableName = "inventory_items",
            operation = "INSERT",
            data = item,
            onSuccess = { fetchInventory() }
        )
    }

    fun saveDailyResult() {
        viewModelScope.launch {
            val currentRevenue = _totalRevenue.value
            val currentTransactions = _transactions.value
            if (currentRevenue <= 0 && currentTransactions.isEmpty()) return@launch

            val report = DailyReport(
                id = System.currentTimeMillis(),
                date = LocalDateTime.now().toString(),
                amount = currentRevenue,
                transactions = currentTransactions
            )
            
            performAction(
                tableName = "daily_reports",
                operation = "INSERT",
                data = report,
                onSuccess = {
                    if (currentTransactions.isNotEmpty()) {
                        currentTransactions.forEach { tx ->
                            performAction(
                                tableName = "transactions",
                                operation = "DELETE",
                                data = "",
                                filterColumn = "id",
                                filterValue = tx.id.toString()
                            )
                        }
                    }
                    uiEvents.emit("Смена успешно закрыта")
                    fetchTransactions()
                    fetchReports()
                }
            )
        }
    }

    fun deleteDailyReport(report: DailyReport) {
        performAction(
            tableName = "daily_reports",
            operation = "DELETE",
            data = "",
            filterColumn = "id",
            filterValue = report.id.toString(),
            onSuccess = {
                uiEvents.emit("Отчет удален")
                fetchReports()
            }
        )
    }

    fun removeTransaction(transaction: Transaction) {
        performAction(
            tableName = "transactions",
            operation = "DELETE",
            data = "",
            filterColumn = "id",
            filterValue = transaction.id.toString(),
            onSuccess = {
                uiEvents.emit("Чек удален")
                fetchTransactions()
            }
        )
    }

    fun removeTransactionItem(transactionId: Long, itemIndex: Int) {
        viewModelScope.launch {
            try {
                val tx = _transactions.value.find { it.id == transactionId } ?: return@launch
                val newItems = tx.items.filterIndexed { index, _ -> index != itemIndex }
                if (newItems.isEmpty()) {
                    removeTransaction(tx)
                } else {
                    val updated = tx.copy(
                        items = newItems, 
                        totalAmount = newItems.sumOf { it.price },
                        totalCost = newItems.sumOf { it.costPrice }
                    )
                    performAction(
                        tableName = "transactions",
                        operation = "UPDATE",
                        data = updated,
                        filterColumn = "id",
                        filterValue = transactionId.toString(),
                        onSuccess = {
                            uiEvents.emit("Позиция удалена")
                            fetchTransactions()
                        }
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                uiEvents.emit("Ошибка при удалении позиции: ${e.message}")
            }
        }
    }

    fun calculateCurrentCost(console: Console): Double {
        val session = console.currentSession ?: return 0.0
        if (session.fixedPrice != null) {
            return session.fixedPrice + session.extras.sumOf { it.price }
        }
        val start = try { LocalDateTime.parse(session.startTime) } catch(e: Exception) { null } ?: return 0.0
        val duration = Duration.between(start, LocalDateTime.now())
        val minutes = duration.toMinutes()
        val timeCost = (minutes.toDouble() / 60.0) * session.hourlyRate
        return timeCost + session.extras.sumOf { it.price }
    }

    fun getRemainingTime(console: Console, lang: Language): String {
        val session = console.currentSession ?: return if (console.status == ConsoleStatus.BOOKED) Loc.s("booked", lang) else ""
        if (session.endTime == null) return Loc.s("until_stop", lang)
        val end = try { LocalDateTime.parse(session.endTime) } catch(e: Exception) { null } ?: return ""
        val duration = Duration.between(LocalDateTime.now(), end)
        if (duration.isNegative) return "00:00:00"
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    // --- Analytics ---

    fun getDailyRevenueForPeriod(days: Int): Map<LocalDate, Double> {
        val result = mutableMapOf<LocalDate, Double>()
        val startDate = LocalDate.now().minusDays(days.toLong())
        
        // Заполняем нулями
        for (i in 0..days) {
            result[startDate.plusDays(i.toLong())] = 0.0
        }
        
        // Считаем из отчетов
        _dailyReports.value.forEach { report ->
            val date = try { LocalDateTime.parse(report.date).toLocalDate() } catch(e: Exception) { null }
            if (date != null && !date.isBefore(startDate)) {
                result[date] = (result[date] ?: 0.0) + report.amount
            }
        }
        
        // Добавляем текущую смену
        val today = LocalDate.now()
        result[today] = (result[today] ?: 0.0) + _totalRevenue.value
        
        return result.toSortedMap()
    }

    fun getWeeklyRevenue(): Map<Int, Double> {
        val weekFields = WeekFields.of(Locale.getDefault())
        val combined = _dailyReports.value.map { it.date to it.amount } + 
                       listOf(LocalDateTime.now().toString() to _totalRevenue.value)
                       
        return combined.groupBy { 
            try { LocalDateTime.parse(it.first).get(weekFields.weekOfWeekBasedYear()) } catch(e: Exception) { 0 }
        }.mapValues { it.value.sumOf { pair -> pair.second } }
    }

    fun getMonthlyRevenue(): Map<Int, Double> {
        val combined = _dailyReports.value.map { it.date to it.amount } + 
                       listOf(LocalDateTime.now().toString() to _totalRevenue.value)
                       
        return combined.groupBy { 
            try { LocalDateTime.parse(it.first).monthValue } catch(e: Exception) { 0 }
        }.mapValues { it.value.sumOf { pair -> pair.second } }
    }

    fun getHourlyRevenue(): Map<Int, Double> {
        val result = (0..23).associateWith { 0.0 }.toMutableMap()
        _transactions.value.forEach { tx ->
            val hour = try { LocalDateTime.parse(tx.timestamp).hour } catch(e: Exception) { 0 }
            result[hour] = (result[hour] ?: 0.0) + tx.totalAmount
        }
        return result
    }
    
    fun getConsoleUsage(): Map<String, Int> {
        val usage = mutableMapOf<String, Int>()
        _transactions.value.forEach { tx ->
            tx.items.forEach { item ->
                if (item.title.contains("ВИП") || item.title.contains("Общий")) {
                    val key = item.title.substringBefore(":")
                    usage[key] = usage.getOrDefault(key, 0) + 1
                }
            }
        }
        return usage
    }

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(getApplication()).enqueueUniqueWork(
            "sync_work",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    private fun performAction(
        tableName: String,
        operation: String,
        data: Any,
        filterColumn: String? = null,
        filterValue: String? = null,
        onSuccess: suspend () -> Unit = {}
    ) {
        viewModelScope.launch {
            val jsonData = if (data is String) data else json.encodeToString(data)
            if (networkObserver.isNetworkAvailable()) {
                try {
                    val table = client.from(tableName)
                    when (operation) {
                        "INSERT" -> table.insert(json.parseToJsonElement(jsonData).jsonObject)
                        "UPDATE" -> {
                            if (filterColumn != null && filterValue != null) {
                                table.update(json.parseToJsonElement(jsonData).jsonObject) {
                                    filter { eq(filterColumn, filterValue) }
                                }
                            }
                        }
                        "DELETE" -> {
                            if (filterColumn != null && filterValue != null) {
                                table.delete {
                                    filter { eq(filterColumn, filterValue) }
                                }
                            }
                        }
                    }
                    onSuccess()
                } catch (e: Exception) {
                    e.printStackTrace()
                    savePendingAction(tableName, operation, jsonData, filterColumn, filterValue)
                    uiEvents.emit("Ошибка сети. Сохранено локально.")
                    onSuccess()
                }
            } else {
                savePendingAction(tableName, operation, jsonData, filterColumn, filterValue)
                uiEvents.emit("Вы вне сети. Будет синхронизировано позже.")
                onSuccess()
            }
        }
    }

    private fun savePendingAction(
        tableName: String,
        operation: String,
        jsonData: String,
        filterColumn: String?,
        filterValue: String?
    ) {
        val action = PendingAction(
            tableName = tableName,
            operation = operation,
            jsonData = jsonData,
            filterColumn = filterColumn,
            filterValue = filterValue
        )
        pendingManager.saveAction(action)
        scheduleSync()
    }
}
