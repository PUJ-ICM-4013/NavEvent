package com.example.naveventapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.naveventapp.ui.components.BottomBar
import com.example.naveventapp.ui.theme.*

@Composable
fun ProfileScreen(
    onNavMap: () -> Unit = {},
    onNavAgenda: () -> Unit = {},
    onNavQr: () -> Unit = {},
    onBellClick: () -> Unit = {},       // navegar a NotificationScreen
    onSave: (email: String, phone: String) -> Unit = { _, _ -> }
) {
    var email by remember { mutableStateOf("correo@gmail.com") }
    var phone by remember { mutableStateOf("3156728732") }

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
                .padding(bottom = 72.dp) // espacio para bottom bar
        ) {
            // Título
            Text(
                text = "Mi Perfil",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black
            )

            Spacer(Modifier.height(20.dp))

            // Avatar + nombre + roles
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .border(width = 2.dp, color = Vinotinto, shape = CircleShape)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Mateo Moreno",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = Color.Black
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Cliente / Organizador / Expositor",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            color = GrisOscuro
                        )
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            // Email
            Text(
                text = "Correo Electrónico",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        cursorColor = Vinotinto
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { /* activar edición explícita si quieres */ },
                    border = BorderStroke(1.5.dp, Vinotinto),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Vinotinto
                    ),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("Editar")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Teléfono
            Text(
                text = "Teléfono",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        cursorColor = Vinotinto
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { /* activar edición explícita si quieres */ },
                    border = BorderStroke(1.5.dp, Vinotinto),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Vinotinto
                    ),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("Editar")
                }
            }

            Spacer(Modifier.height(24.dp))

            // Guardar cambios
            OutlinedButton(
                onClick = { onSave(email, phone) },
                border = BorderStroke(1.5.dp, Vinotinto),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(46.dp)
                    .align(Alignment.CenterHorizontally)
            ) { Text("Guardar Cambios") }
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
                imageVector = Icons.Filled.Notifications,
                contentDescription = "Notificaciones",
                tint = Vinotinto
            )
        }

        // BottomBar reutilizable
        BottomBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            onMap = onNavMap,
            onAgenda = onNavAgenda,
            onQr = onNavQr,
        )
    }
}
