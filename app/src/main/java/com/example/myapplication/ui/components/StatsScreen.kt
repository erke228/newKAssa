package com.example.myapplication.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.models.DailyReport
import com.example.myapplication.models.Language
import com.example.myapplication.models.Loc
import com.example.myapplication.models.Transaction
import java.time.LocalDateTime
import com.example.myapplication.ui.theme.NeonCyan
import com.example.myapplication.ui.theme.NeonPink
import com.example.myapplication.ui.theme.SurfaceDark
import com.example.myapplication.viewmodel.ClubViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: ClubViewModel) {
    val revenue by viewModel.totalRevenue.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val dailyReports by viewModel.dailyReports.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // Dialog States
    var pendingAction by remember { mutableStateOf<AdminAction?>(null) }
    var selectedReportForDetails by remember { mutableStateOf<DailyReport?>(null) }
    var passwordInput by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }

    if (pendingAction != null) {
        AlertDialog(
            onDismissRequest = { 
                pendingAction = null
                passwordInput = ""
                passwordError = false
            },
            containerColor = SurfaceDark,
            title = { 
                Text(
                    text = when(val action = pendingAction) {
                        is AdminAction.Reset -> if(lang == Language.RU) "Завершить смену?" else "Ауысымды аяқтау?"
                        is AdminAction.DeleteTransaction -> if(lang == Language.RU) "Удаление чека" else "Чекті өшіру"
                        is AdminAction.DeleteItem -> if(lang == Language.RU) "Удаление позиции" else "Позицияны өшіру"
                        is AdminAction.DeleteReport -> if(lang == Language.RU) "Удаление отчета" else "Есепті өшіру"
                        else -> ""
                    },
                    color = Color.White
                ) 
            },
            text = {
                Column {
                    Text(if(lang == Language.RU) "Введите пароль администратора:" else "Әкімші құпия сөзін енгізіңіз:", color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { 
                            passwordInput = it
                            passwordError = false
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = passwordError,
                        label = { Text(if(lang == Language.RU) "Пароль" else "Құпия сөз") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (passwordError) {
                        Text(if(lang == Language.RU) "Неверный пароль" else "Қате құпия сөз", color = NeonPink, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (passwordInput == "erkebulan" || passwordInput == "2103" || passwordInput == "2403") {
                            when(val action = pendingAction) {
                                is AdminAction.Reset -> viewModel.saveDailyResult()
                                is AdminAction.DeleteTransaction -> viewModel.removeTransaction(action.transaction)
                                is AdminAction.DeleteItem -> viewModel.removeTransactionItem(action.transactionId, action.itemIndex)
                                is AdminAction.DeleteReport -> viewModel.deleteDailyReport(action.report)
                                null -> {}
                            }
                            pendingAction = null
                            passwordInput = ""
                            passwordError = false
                        } else {
                            passwordError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                ) {
                    Text(if(lang == Language.RU) "ОК" else "ОК")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Summary Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(2.dp, NeonCyan)
        ) {
            Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(if(lang == Language.RU) "ВЫРУЧКА ЗА СМЕНУ" else "АУЫСЫМДАҒЫ ТАБЫС", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("${revenue.toInt()} ₸", color = NeonCyan, fontSize = 32.sp, fontWeight = FontWeight.Black)
                }
                Button(
                    onClick = { pendingAction = AdminAction.Reset },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(if(lang == Language.RU) "ЗАКРЫТЬ СМЕНУ" else "АУЫСЫМДЫ ЖАБУ", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Tabs
        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = NeonCyan,
            divider = {},
            indicator = {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedTab, matchContentSize = true),
                    color = NeonCyan,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(if(lang == Language.RU) "ЖУРНАЛ" else "ЖУРНАЛ") },
                icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(if(lang == Language.RU) "ГРАФИКИ" else "ГРАФИКТЕР") },
                icon = { Icon(Icons.Default.Insights, null) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text(if(lang == Language.RU) "ОТЧЕТЫ" else "ЕСЕПТЕР") },
                icon = { Icon(Icons.Default.History, null) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedContent(targetState = selectedTab, label = "TabChange") { targetTab ->
            when (targetTab) {
                0 -> TransactionList(transactions, lang) { action -> pendingAction = action }
                1 -> AnalyticsSection(viewModel, lang)
                2 -> ReportsList(dailyReports, lang, 
                    onAction = { action -> pendingAction = action },
                    onReportClick = { selectedReportForDetails = it }
                )
            }
        }
    }

    if (selectedReportForDetails != null) {
        ReportDetailsDialog(
            report = selectedReportForDetails!!,
            lang = lang,
            onDismiss = { selectedReportForDetails = null }
        )
    }
}

@Composable
fun ReportDetailsDialog(report: DailyReport, lang: Language, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Column {
                val date = try { 
                    val sanitized = report.date.replace(" ", "T")
                    LocalDateTime.parse(sanitized) 
                } catch(e: Exception) { null }
                Text(
                    text = if(lang == Language.RU) "Детали отчета" else "Есеп мәліметтері",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = date?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) ?: "--.--.----",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if(lang == Language.RU) "Итого за смену:" else "Ауысым қорытындысы:", color = Color.White)
                    Text("${report.amount.toInt()} ₸", color = NeonCyan, fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
                
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.padding(bottom = 8.dp))
                
                if (report.transactions.isNullOrEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(if(lang == Language.RU) "Нет записей" else "Жазбалар жоқ", color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                        items(report.transactions!!) { tx ->
                            val txTime = try { 
                                val sanitized = tx.timestamp.replace(" ", "T")
                                LocalDateTime.parse(sanitized) 
                            } catch(e: Exception) { null }
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = txTime?.format(DateTimeFormatter.ofPattern("HH:mm:ss")) ?: "--:--:--", 
                                            color = NeonCyan, 
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${tx.totalAmount.toInt()} ₸", 
                                            color = Color.White, 
                                            fontSize = 14.sp, 
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    tx.items.forEach { item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "• ${item.title}", 
                                                color = Color.Gray, 
                                                fontSize = 12.sp, 
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = "${item.price.toInt()} ₸", 
                                                color = Color.White, 
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss, 
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun TransactionList(transactions: List<Transaction>, lang: Language, onAction: (AdminAction) -> Unit) {
    if (transactions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(if(lang == Language.RU) "Нет записей" else "Жазбалар жоқ", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(transactions, key = { it.id }) { tx ->
                TransactionCard(tx, onAction)
            }
        }
    }
}

@Composable
fun TransactionCard(tx: Transaction, onAction: (AdminAction) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val date = try { 
                        val sanitized = tx.timestamp.replace(" ", "T")
                        LocalDateTime.parse(sanitized) 
                    } catch(e: Exception) { null }
                    Text(
                        text = date?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "--.--.----",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                    Text(
                        text = date?.format(DateTimeFormatter.ofPattern("HH:mm:ss")) ?: "--:--:--",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${tx.totalAmount.toInt()} ₸",
                        color = NeonCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = NeonPink.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp).clickable { onAction(AdminAction.DeleteTransaction(tx)) }
                    )
                }
            }
            
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))
            
            tx.items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.ArrowRight, null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                        Text(item.title, color = Color.White, fontSize = 13.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${item.price.toInt()} ₸", color = Color.Gray, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp).clickable { 
                                onAction(AdminAction.DeleteItem(tx.id, index)) 
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsSection(viewModel: ClubViewModel, lang: Language) {
    var periodBy by remember { mutableIntStateOf(0) } // 0: Days, 1: Weeks, 2: Months

    val dailyData = viewModel.getDailyRevenueForPeriod(7)
    val weeklyData = viewModel.getWeeklyRevenue()
    val monthlyData = viewModel.getMonthlyRevenue()
    val hourlyRevenue = viewModel.getHourlyRevenue()
    val consoleUsage = viewModel.getConsoleUsage()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            AnalyticsCard(
                title = if(lang == Language.RU) "Выручка" else "Табыс",
                icon = Icons.Default.CalendarMonth
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        FilterChip(
                            selected = periodBy == 0,
                            onClick = { periodBy = 0 },
                            label = { Text(if(lang == Language.RU) "Дни" else "Күндер") }
                        )
                        FilterChip(
                            selected = periodBy == 1,
                            onClick = { periodBy = 1 },
                            label = { Text(if(lang == Language.RU) "Недели" else "Апталар") }
                        )
                        FilterChip(
                            selected = periodBy == 2,
                            onClick = { periodBy = 2 },
                            label = { Text(if(lang == Language.RU) "Месяцы" else "Айлар") }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    val chartData = when(periodBy) {
                        0 -> dailyData.values.toList()
                        1 -> weeklyData.values.toList()
                        else -> monthlyData.values.toList()
                    }
                    val chartLabels = when(periodBy) {
                        0 -> dailyData.keys.map { it.dayOfMonth.toString() }
                        1 -> weeklyData.keys.map { it.toString() }
                        else -> monthlyData.keys.map { it.toString() }
                    }

                    if (chartData.isEmpty()) {
                        EmptyState()
                    } else {
                        EnhancedBarChart(
                            data = chartData,
                            labels = chartLabels,
                            color = NeonCyan
                        )
                    }
                }
            }
        }

        item {
            AnalyticsCard(
                title = if(lang == Language.RU) "Пиковые часы" else "Ең көп келетін уақыт",
                icon = Icons.Default.AccessTime
            ) {
                EnhancedBarChart(
                    data = hourlyRevenue.values.toList(),
                    labels = hourlyRevenue.keys.map { if(it % 4 == 0) "$it:00" else "" },
                    color = NeonPink
                )
            }
        }

        item {
            AnalyticsCard(
                title = if(lang == Language.RU) "Популярность консолей" else "Консольдер танымалдылығы",
                icon = Icons.Default.SportsEsports
            ) {
                if (consoleUsage.isEmpty()) EmptyState()
                else {
                    val maxUsage = consoleUsage.values.maxOrNull() ?: 1
                    consoleUsage.entries.sortedByDescending { it.value }.take(5).forEach { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(entry.key, color = Color.White, fontSize = 14.sp)
                            Text("${entry.value} раз", color = NeonCyan, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { entry.value.toFloat() / maxUsage.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = NeonCyan,
                            trackColor = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun EmptyState() {
    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
        Text(text = "Данных пока нет", color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun EnhancedBarChart(data: List<Double>, labels: List<String>, color: Color) {
    val maxVal = (data.maxOrNull() ?: 1.0).coerceAtLeast(1.0).toFloat()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(top = 8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spacing = 8.dp.toPx()
            val barWidth = (size.width - (data.size - 1) * spacing) / data.size
            
            data.forEachIndexed { index, value ->
                val barHeight = (value.toFloat() / maxVal) * size.height
                val x = index * (barWidth + spacing)
                
                // Actual Bar
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(color, color.copy(alpha = 0.2f))),
                    topLeft = Offset(x, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
                
                // Label
                if (labels.getOrNull(index)?.isNotEmpty() == true) {
                    drawContext.canvas.nativeCanvas.drawText(
                        labels[index],
                        x + barWidth / 2,
                        size.height + 12.dp.toPx(),
                        android.graphics.Paint().apply {
                            this.color = android.graphics.Color.GRAY
                            this.textSize = 9.sp.toPx()
                            this.textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReportsList(
    reports: List<DailyReport>, 
    lang: Language, 
    onAction: (AdminAction) -> Unit,
    onReportClick: (DailyReport) -> Unit
) {
    if (reports.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(if(lang == Language.RU) "Отчетов пока нет" else "Әзірге есептер жоқ", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(reports, key = { it.id }) { report ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onReportClick(report) },
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val date = try { 
                    val sanitized = report.date.replace(" ", "T")
                    LocalDateTime.parse(sanitized) 
                } catch(e: Exception) { null }
                            Text(
                                text = date?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "--.--.----",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = date?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "--:--",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${report.amount.toInt()} ₸",
                                color = NeonCyan,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { onAction(AdminAction.DeleteReport(report)) }) {
                                Icon(Icons.Default.Delete, null, tint = NeonPink.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed class AdminAction {
    data object Reset : AdminAction()
    data class DeleteTransaction(val transaction: Transaction) : AdminAction()
    data class DeleteItem(val transactionId: Long, val itemIndex: Int) : AdminAction()
    data class DeleteReport(val report: DailyReport) : AdminAction()
}
