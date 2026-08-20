package com.example.myapplication.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.models.Language
import com.example.myapplication.ui.theme.NeonCyan
import com.example.myapplication.ui.theme.NeonPink
import com.example.myapplication.ui.theme.SurfaceDark
import com.example.myapplication.viewmodel.ClubViewModel

@Composable
fun LogoHeader(
    currentLanguage: Language,
    viewModel: ClubViewModel,
    onLanguageToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(70.dp), // Even larger icon
            color = Color.Transparent
        ) {
            // Используем foreground ресурс для логотипа в шапке
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "VITSA Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "VITSA",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = NeonCyan,
                letterSpacing = 2.sp
            )
            Text(
                text = "PS CLUB",
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                color = NeonPink,
                letterSpacing = 4.sp
            )
        }

        Surface(
            modifier = Modifier
                .clickable { viewModel.refreshData() }
                .padding(end = 8.dp),
            color = SurfaceDark,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = NeonCyan,
                modifier = Modifier.padding(8.dp).size(20.dp)
            )
        }

        Surface(
            modifier = Modifier
                .clickable { onLanguageToggle() },
            color = SurfaceDark,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
        ) {
            Text(
                text = if (currentLanguage == Language.RU) "RU" else "KK",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}
