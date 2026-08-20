package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.models.*
import com.example.myapplication.ui.theme.NeonCyan
import com.example.myapplication.ui.theme.NeonPink
import com.example.myapplication.ui.theme.SurfaceDark
import com.example.myapplication.viewmodel.ClubViewModel

@Composable
fun BarScreen(viewModel: ClubViewModel) {
    val items by viewModel.barItems.collectAsState()
    val consoles by viewModel.consoles.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()
    
    var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }
    var showAssignDialog by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    
    var quantity by remember { mutableIntStateOf(1) }
    
    // Reset quantity when dialog opens
    LaunchedEffect(showAssignDialog) {
        if (showAssignDialog) quantity = 1
    }

    val categories = remember(items) {
        items.map { it.category }.distinct().sorted()
    }

    val filteredItems = remember(items, searchQuery, selectedCategory) {
        items.filter { item ->
            val matchesSearch = item.name.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null || item.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if(lang == Language.RU) "БАР И СНЕКИ" else "БАР ЖӘНЕ СНЕКТЕР",
                color = NeonCyan,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = { showAddItemDialog = true },
                modifier = Modifier.background(NeonCyan.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = NeonCyan)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(if(lang == Language.RU) "Поиск товаров..." else "Тауарларды іздеу...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Categories
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text(if(lang == Language.RU) "ВСЕ" else "БАРЛЫҒЫ") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan,
                        selectedLabelColor = Color.Black,
                        labelColor = Color.Gray
                    )
                )
            }
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan,
                        selectedLabelColor = Color.Black,
                        labelColor = Color.Gray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(filteredItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = item.name, color = Color.White, fontWeight = FontWeight.Bold)
                            val categoryTranslated = if(lang == Language.KK) {
                                if(item.category == "СНЕКИ") "СНЕКТЕР" else "СУСЫНДАР"
                            } else item.category
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = categoryTranslated, color = Color.Gray, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if(lang == Language.RU) "В наличии: ${item.stock}" else "Қалды: ${item.stock}",
                                    color = if(item.stock < 5) NeonPink else NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "${item.price.toInt()} ₸", color = NeonCyan, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(16.dp))
                            Button(
                                onClick = { 
                                    selectedItem = item
                                    showAssignDialog = true 
                                },
                                enabled = item.stock > 0,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    disabledContainerColor = Color.DarkGray
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if(item.stock > 0) Loc.s("add_to_cart", lang) else (if(lang == Language.RU) "НЕТ" else "ЖОҚ"), 
                                    color = if(item.stock > 0) Color.Black else Color.Gray, 
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAssignDialog && selectedItem != null) {
        AlertDialog(
            onDismissRequest = { showAssignDialog = false },
            containerColor = SurfaceDark,
            title = { Text("${Loc.s("add_to_cart", lang)}: ${selectedItem!!.name}", color = Color.White) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if(lang == Language.RU) "Количество:" else "Саны:", color = Color.White)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (quantity > 1) quantity-- }) {
                                Text("-", color = NeonCyan, fontSize = 24.sp)
                            }
                            Text(quantity.toString(), color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(horizontal = 8.dp))
                            IconButton(onClick = { quantity++ }) {
                                Text("+", color = NeonCyan, fontSize = 24.sp)
                            }
                        }
                    }

                    Text(if(lang == Language.RU) "Куда добавить товар?" else "Тауарды қайда қосу керек?", color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { 
                            val totalItemPrice = selectedItem!!.price * quantity
                            val qText = if(quantity > 1) " x$quantity" else ""
                            viewModel.addToCart(CartItem.BarCart(
                                itemId = selectedItem!!.id,
                                quantity = quantity,
                                title = "БАР: ${selectedItem!!.name}$qText",
                                price = totalItemPrice
                            ))
                            showAssignDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text(Loc.s("direct_sale", lang))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(Loc.s("add_to_console", lang), color = Color.White, fontWeight = FontWeight.Bold)
                    consoles.forEach { console ->
                        if (console.currentSession != null) {
                            val priceSuffix = "₸"
                            val totalItemPrice = selectedItem!!.price * quantity
                            TextButton(
                                onClick = {
                                    val qText = if(quantity > 1) " x$quantity" else ""
                                    viewModel.addToCart(CartItem.BarCart(
                                        itemId = selectedItem!!.id,
                                        consoleId = console.id,
                                        quantity = quantity,
                                        title = "БАР: ${selectedItem!!.name}$qText (${console.name})",
                                        price = totalItemPrice
                                    ))
                                    showAssignDialog = false
                                }
                            ) {
                                Text("${console.name} (+${totalItemPrice.toInt()} $priceSuffix)", color = NeonCyan)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAssignDialog = false }) {
                    Text(Loc.s("cancel", lang), color = Color.Gray)
                }
            }
        )
    }

    if (showAddItemDialog) {
        AddItemDialog(
            lang = lang,
            onDismiss = { showAddItemDialog = false },
            onConfirm = { name, cat, price, cost, stock ->
                viewModel.addInventoryItem(name, cat, price, cost, stock)
                showAddItemDialog = false
            }
        )
    }
}

@Composable
fun AddItemDialog(
    lang: Language,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Double, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text(if(lang == Language.RU) "Новый товар" else "Жаңа тауар", color = Color.White) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if(lang == Language.RU) "Название" else "Атауы") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(if(lang == Language.RU) "Категория (НАПИТКИ/СНЕКИ)" else "Санат") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text(if(lang == Language.RU) "Цена" else "Бағасы") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = costPrice,
                        onValueChange = { costPrice = it },
                        label = { Text(if(lang == Language.RU) "Закуп" else "Закуп") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text(if(lang == Language.RU) "Кол-во на складе" else "Саны") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = price.toDoubleOrNull() ?: 0.0
                    val cp = costPrice.toDoubleOrNull() ?: 0.0
                    val s = stock.toIntOrNull() ?: 0
                    if (name.isNotEmpty() && category.isNotEmpty()) {
                        onConfirm(name, category, p, cp, s)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text(if(lang == Language.RU) "Добавить" else "Қосу", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Loc.s("cancel", lang), color = Color.Gray)
            }
        }
    )
}
