package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.models.*
import com.example.myapplication.ui.theme.NeonCyan
import com.example.myapplication.ui.theme.NeonPink
import com.example.myapplication.ui.theme.SurfaceDark
import java.time.LocalDateTime

@Composable
fun SessionDialog(
    console: Console,
    lang: Language,
    onDismiss: () -> Unit,
    onAddToCart: (CartItem) -> Unit,
    onStop: () -> Unit,
    onBook: (String) -> Unit
) {
    var showBookingInput by remember { mutableStateOf(false) }
    var bookingName by remember { mutableStateOf("") }
    
    val consoleTitle = if (console.type == ConsoleType.VIP) {
        "${Loc.s("vip", lang)} ${console.name}"
    } else {
        "${Loc.s("common", lang)} ${console.name}"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text(text = consoleTitle, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (console.status == ConsoleStatus.FREE) {
                    if (showBookingInput) {
                        OutlinedTextField(
                            value = bookingName,
                            onValueChange = { bookingName = it },
                            label = { Text(Loc.s("client_name", lang)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Color.Gray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { if (bookingName.isNotEmpty()) onBook(bookingName) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                        ) {
                            Text(Loc.s("confirm_booking", lang))
                        }
                    } else {
                        Text(Loc.s("select_session", lang), color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val rate = if (console.type == ConsoleType.VIP) 2500.0 else 1500.0
                        
                        PacketButton("1 " + if(lang == Language.RU) "ЧАС" else "САҒАТ", "${rate.toInt()} T", NeonCyan) {
                            onAddToCart(CartItem.SessionCart(
                                consoleId = console.id,
                                consoleName = console.name,
                                durationMinutes = 60,
                                fixedPrice = rate,
                                packageName = "1 HOUR",
                                title = "$consoleTitle: 1 HOUR",
                                price = rate
                            ))
                            onDismiss()
                        }
                        
                        // "Until Stop" - price is 0 for cart (paid at end)
                        PacketButton(Loc.s("until_stop", lang), "", NeonCyan) {
                            onAddToCart(CartItem.SessionCart(
                                consoleId = console.id,
                                consoleName = console.name,
                                durationMinutes = null,
                                fixedPrice = null,
                                packageName = null,
                                title = "$consoleTitle: ${Loc.s("until_stop", lang)}",
                                price = 0.0
                            ))
                            onDismiss()
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(Loc.s("packets", lang), color = NeonPink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (console.type == ConsoleType.COMMON) {
                            PacketButton("ПАКЕТ «2 + 1» (3h)", "3000 T", NeonCyan) {
                                onAddToCart(CartItem.SessionCart(console.id, console.name, 180, 3000.0, "2 + 1", "$consoleTitle: 2+1", 3000.0))
                                onDismiss()
                            }
                            PacketButton("ПАКЕТ «5 " + (if(lang == Language.RU) "ЧАСОВ" else "САҒАТ") + "»", "4500 T", NeonCyan) {
                                onAddToCart(CartItem.SessionCart(console.id, console.name, 300, 4500.0, "5 HOURS", "$consoleTitle: 5 HOURS", 4500.0))
                                onDismiss()
                            }
                            PacketButton("ПАКЕТ «" + (if(lang == Language.RU) "НОЧЬ" else "ТҮН") + "» (23-08)", "4000 T", NeonCyan) {
                                onAddToCart(CartItem.SessionCart(console.id, console.name, 540, 4000.0, "НОЧЬ", "$consoleTitle: NIGHT", 4000.0))
                                onDismiss()
                            }
                        } else {
                            PacketButton("HAPPY HOURS (3h)", "3500 T", NeonPink) {
                                onAddToCart(CartItem.SessionCart(console.id, console.name, 180, 3500.0, "HAPPY HOURS", "$consoleTitle: HAPPY HOURS", 3500.0))
                                onDismiss()
                            }
                            PacketButton("ПАКЕТ «3 " + (if(lang == Language.RU) "ЧАСА" else "САҒАТ") + "»", "5000 T", NeonPink) {
                                onAddToCart(CartItem.SessionCart(console.id, console.name, 180, 5000.0, "3 HOURS", "$consoleTitle: 3 HOURS", 5000.0))
                                onDismiss()
                            }
                            PacketButton("ПАКЕТ «3 + 2» (5h)", "7500 T", NeonPink) {
                                onAddToCart(CartItem.SessionCart(console.id, console.name, 300, 7500.0, "3 + 2", "$consoleTitle: 3+2", 7500.0))
                                onDismiss()
                            }
                            PacketButton("ПАКЕТ «" + (if(lang == Language.RU) "НОЧЬ" else "ТҮН") + " VIP»", "7000 T", NeonPink) {
                                onAddToCart(CartItem.SessionCart(console.id, console.name, 540, 7000.0, "NIGHT VIP", "$consoleTitle: NIGHT VIP", 7000.0))
                                onDismiss()
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showBookingInput = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) {
                            Text(Loc.s("book", lang))
                        }
                    }
                } else if (console.status == ConsoleStatus.BOOKED) {
                    Text("${Loc.s("reserved_for", lang)} ${console.bookedBy}", color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onStop, 
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                    ) {
                        Text(Loc.s("clear_booking", lang))
                    }
                } else {
                    val session = console.currentSession!!
                    val startTime = try { LocalDateTime.parse(session.startTime) } catch(e: Exception) { null }
                    val endTime = try { session.endTime?.let { LocalDateTime.parse(it) } } catch(e: Exception) { null }
                    
                    Text("${Loc.s("session", lang)}: ${session.packageName ?: "Стандарт"}", color = Color.White)
                    Text("${Loc.s("started", lang)}: ${startTime?.toLocalTime()?.toString()?.substring(0, 5) ?: "--:--"}", color = Color.Gray)
                    if (endTime != null) {
                        Text(
                            text = if(lang == Language.RU) "Закончится в: ${endTime.toLocalTime().toString().substring(0, 5)}" 
                                   else "Аяқталуы: ${endTime.toLocalTime().toString().substring(0, 5)}", 
                            color = NeonPink,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (session.fixedPrice != null) {
                        Text("${Loc.s("packet_price", lang)}: ${session.fixedPrice.toInt()} T", color = NeonCyan)
                    }
                }
            }
        },
        confirmButton = {
            if (console.status == ConsoleStatus.BUSY) {
                Button(
                    onClick = onStop,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                ) {
                    Text(Loc.s("finish", lang))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Loc.s("close", lang), color = Color.Gray)
            }
        }
    )
}

@Composable
fun PacketButton(name: String, price: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.2f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, color = Color.White)
            Text(price, color = color, fontWeight = FontWeight.Bold)
        }
    }
}
