package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.models.Loc
import com.example.myapplication.ui.theme.NeonCyan
import com.example.myapplication.ui.theme.NeonPink
import com.example.myapplication.ui.theme.SurfaceDark
import com.example.myapplication.viewmodel.ClubViewModel

@Composable
fun CartScreen(viewModel: ClubViewModel) {
    val cartItems by viewModel.cart.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()
    var showQR by remember { mutableStateOf(false) }
    val totalAmount = cartItems.sumOf { it.price }

    if (showQR) {
        PaymentQRDialog(
            amount = totalAmount, 
            lang = lang,
            onConfirm = {
                showQR = false
                viewModel.checkout()
            },
            onCancel = {
                showQR = false
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = Loc.s("cart", lang).uppercase(),
            color = NeonCyan,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(Loc.s("cart_empty", lang), color = Color.Gray)
            }
        } else {
            Column(modifier = Modifier.weight(1f)) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(cartItems) { index, item ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, color = Color.White, fontWeight = FontWeight.Bold)
                                    if (item.price > 0) {
                                        Text("${item.price.toInt()} ₸", color = NeonCyan)
                                    } else {
                                        Text(if(lang == com.example.myapplication.models.Language.RU) "Оплата по факту" else "Шыққанда төлеу", color = NeonPink, fontSize = 12.sp)
                                    }
                                }
                                IconButton(onClick = { viewModel.removeFromCart(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = NeonPink)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${Loc.s("total", lang)}:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("${totalAmount.toInt()} ₸", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { 
                                if (totalAmount > 0) {
                                    showQR = true 
                                } else {
                                    viewModel.checkout()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (totalAmount > 0) Loc.s("pay_kaspi", lang) else (if(lang == com.example.myapplication.models.Language.RU) "ПОДТВЕРДИТЬ" else "РАСТАУ"),
                                color = Color.Black, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
