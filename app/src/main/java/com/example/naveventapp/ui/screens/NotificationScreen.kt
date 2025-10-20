package com.example.naveventapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.naveventapp.ui.components.BottomBar
import com.example.naveventapp.ui.theme.*

@Composable
fun NotificationScreen(
    onNavMap: () -> Unit = {},
    onNavAgenda: () -> Unit = {},
    onNavQr: () -> Unit = {},
    onNavProfile: () -> Unit = {},
    onBellClick: () -> Unit = {}
) {
    val items = listOf(
        Icons.Filled.EventNote to "Cambio en la sala para el taller de UX",
        Icons.Filled.QrCode   to "Nueva promoción en el stand 12",
        Icons.Filled.Campaign to "Se acerca la charla de cierre",
        Icons.Filled.Map      to "Accede por la entrada norte",
        Icons.Filled.QrCode   to "Recuerda visitar los stands destacados",
    )

    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 8.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(bottom = 72.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Notificaciones",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black
            )

            Spacer(Modifier.height(20.dp))

            items.forEach { (icon, text) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .height(64.dp) // 🔹 Más alto
                ) {
                    // Icono más grande
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Vinotinto,
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(Modifier.width(12.dp))

                    // Botón notificación más grande y texto más visible
                    OutlinedButton(
                        onClick = { /* acción por notificación */ },
                        border = BorderStroke(2.dp, Vinotinto),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp) // 🔹 Más alto
                    ) {
                        Text(
                            text,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }

        // Campana
        IconButton(
            onClick = onBellClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 12.dp)
                .background(Blanco.copy(alpha = 0.9f), shape = MaterialTheme.shapes.small)
        ) {
            Icon(
                Icons.Filled.Notifications,
                contentDescription = "Notificaciones",
                tint = Vinotinto,
                modifier = Modifier.size(28.dp)
            )
        }

        // Bottom bar
        BottomBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            onMap = onNavMap,
            onAgenda = onNavAgenda,
            onQr = onNavQr,
            onProfile = onNavProfile
        )
    }
}
