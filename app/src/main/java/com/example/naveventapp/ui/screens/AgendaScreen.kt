package com.example.naveventapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.itemsIndexed

@Composable
fun AgendaScreen(
    onNavMap: () -> Unit = {},
    onNavAgenda: () -> Unit = {},
    onNavQr: () -> Unit = {},
    onNavProfile: () -> Unit = {},
    onBellClick: () -> Unit = {}
) {
    val agenda = listOf(
        "09:00" to "Charla de apertura",
        ""      to "Charla en el auditorio Restrepo",
        "10:30" to "Panel de Innovación",
        ""      to "Se encuentra en el pabellón 1",
        "12:00" to "Networking con expositores",
        ""      to "Se encuentra sujeto a cambio de horario",
        "14:00" to "Taller de UX design",
        ""      to "Se encuentra sujeto a cambio de ubicación",
        "17:00" to "Conferencia de cierre",
        ""      to "Cierre de puertas"
    )

    val RojoClaro = Color(0xFFF6DADA) // rojo muy claro para los "pill" impares

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
                .padding(bottom = 90.dp)
        ) {
            Text("Agenda", style = MaterialTheme.typography.headlineSmall, color = Color.Black)
            Spacer(Modifier.height(12.dp))

            // Lista con índice para alternar estilos
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(agenda) { index, (hora, titulo) ->
                    val esImpar1Based = ((index + 1) % 2 == 1)
                    if (esImpar1Based) {
                        // 1,3,5... → HORA (grande) + PILL rojo claro (más compacta, pero más grande que antes)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = hora,
                                color = GrisOscuro,
                                fontSize = 18.sp, // hora más grande
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(64.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = RojoClaro,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, Vinotinto),
                                modifier = Modifier
                                    .height(56.dp)  // más grande
                                    .weight(1f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.CenterStart,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    Text(
                                        text = titulo,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    } else {
                        // 2,4,6... → DESCRIPCIÓN (sin hora), caja más alta y fondo blanco
                        Surface(
                            color = Blanco,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, Vinotinto),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp) // más grande que las impares
                        ) {
                            Box(
                                contentAlignment = Alignment.CenterStart,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = titulo,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 17.sp),
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        IconButton(
            onClick = onBellClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 12.dp)
                .background(Blanco.copy(alpha = 0.9f), shape = MaterialTheme.shapes.small)
        ) {
            Icon(Icons.Filled.Notifications, contentDescription = "Notificaciones", tint = Vinotinto)
        }

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
