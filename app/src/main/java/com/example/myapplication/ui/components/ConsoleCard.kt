package com.example.myapplication.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.models.*
import com.example.myapplication.ui.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun ConsoleCard(
    console: Console,
    lang: Language,
    currentCost: Double,
    remainingTime: String,
    onClick: () -> Unit
) {
    val accentColor = if (console.type == ConsoleType.VIP) NeonPurple else NeonBlue
    val isBusy = console.status == ConsoleStatus.BUSY
    val isBooked = console.status == ConsoleStatus.BOOKED
    
    val consoleLabel = if (console.type == ConsoleType.VIP) {
        "${Loc.s("vip", lang)} ${console.name}"
    } else {
        "${Loc.s("common", lang)} ${console.name}"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
            .height(210.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        ),
        border = BorderStroke(
            width = 3.dp,
            color = when {
                isBusy -> accentColor
                isBooked -> Color.Gray
                else -> accentColor.copy(alpha = 0.3f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Icon + Name + Status
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(40.dp) // Larger icon
                    )
                    Text(
                        text = when {
                            isBusy -> Loc.s("busy", lang)
                            isBooked -> Loc.s("booked", lang)
                            else -> Loc.s("free", lang)
                        },
                        color = when {
                            isBusy -> NeonPink
                            isBooked -> Color.Gray
                            else -> NeonCyan
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = consoleLabel,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Body: Time and Cost
            if (isBusy) {
                Column {
                    val session = console.currentSession!!
                    Text(
                        text = if(lang == Language.RU) 
                            "Старт: ${session.startTime.substring(11, 16)}" 
                            else "Басталды: ${session.startTime.substring(11, 16)}",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    if (session.endTime != null) {
                        Text(
                            text = if(lang == Language.RU) "До: ${session.endTime.substring(11, 16)}" 
                                   else "Уақыты: ${session.endTime.substring(11, 16)}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = remainingTime,
                        color = accentColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${currentCost.toInt()} ₸",
                        color = NeonCyan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            } else if (isBooked) {
                Text(
                    text = "${Loc.s("reserved_for", lang)} ${console.bookedBy}",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = Loc.s("tap_to_start", lang),
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }
    }
}
