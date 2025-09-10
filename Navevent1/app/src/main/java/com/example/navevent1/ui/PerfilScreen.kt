package com.example.navevent1.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


private val PurplePrimary = Color(0xFF673AB7)
private val PurpleOutline = Color(0xFFB39DDB)

@Composable
fun PerfilScreen() {
    var email by remember { mutableStateOf("Correo@gmail.com") }
    var phone by remember { mutableStateOf("3156728732") }
    var editEmail by remember { mutableStateOf(false) }
    var editPhone by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Botón Guardar Cambios (morado)
                OutlinedButton(
                    onClick = { /* TODO guardar */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.5.dp,
                        brush = SolidColor(PurplePrimary) // borde morado
                    )
                ) {
                    Text("Guardar Cambios")
                }

                Spacer(Modifier.height(12.dp))

                // Botón Cerrar Sesión (morado relleno)
                Button(
                    onClick = { /* TODO cerrar sesión */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Cerrar Sesión", color = Color.White)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.Start
        ) {
            // Título
            Text("Mi Perfil", fontSize = 26.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(12.dp))

            // Avatar + nombre + roles
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .border(width = 2.dp, color = PurplePrimary, shape = CircleShape) // borde morado
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Mateo Moreno", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Cliente/ Organizador/ Expositor",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Campo correo
            LabeledEditableField(
                label = "Correo Electrónico",
                value = email,
                enabled = editEmail,
                onValueChange = { email = it },
                onToggleEdit = { editEmail = !editEmail }
            )

            Spacer(Modifier.height(12.dp))

            // Campo teléfono
            LabeledEditableField(
                label = "Teléfono",
                value = phone,
                enabled = editPhone,
                onValueChange = { phone = it },
                onToggleEdit = { editPhone = !editPhone }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabeledEditableField(
    label: String,
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onToggleEdit: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray,
            modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurplePrimary,
                unfocusedBorderColor = PurplePrimary,
                disabledBorderColor = PurpleOutline,
                focusedTextColor = Color.Black,
                disabledTextColor = Color.Black
            ),
            trailingIcon = {
                TextButton(
                    onClick = onToggleEdit,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = PurplePrimary)
                ) {
                    Text(if (enabled) "Listo" else "Editar")
                }
            }
        )
    }
}
