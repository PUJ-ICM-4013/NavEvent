package com.example.naveventapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.naveventapp.ui.theme.*

@Composable
fun LogInScreen(
    onLogin: (email: String, password: String, remember: Boolean) -> Unit = { _,_,_ -> },
    onCreateAccountClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var pwd by remember { mutableStateOf("") }
    var remember by remember { mutableStateOf(false) }
    var showPwd by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Spacer(Modifier.height(100.dp))

        Text(
            text = "Iniciar Sesión",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center, // centrado
            color = Color.Black,
            modifier = Modifier.fillMaxWidth() // ocupa todo el ancho para centrar
        )

        Spacer(Modifier.height(50.dp))

        // Label externo y campo: Correo
        Text(
            text = "Correo electrónico",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            singleLine = true,
            placeholder = { Text("tucorreo@dominio.com") },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                cursorColor = Vinotinto,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Label externo y campo: Contraseña
        Text(
            text = "Contraseña",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = pwd,
            onValueChange = { pwd = it },
            singleLine = true,
            placeholder = { Text("••••••••") },
            visualTransformation = if (showPwd) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Text(
                    text = if (showPwd) "Ocultar" else "Ver",
                    color = Vinotinto,
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .clickable { showPwd = !showPwd }
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                cursorColor = Vinotinto,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = remember,
                onCheckedChange = { remember = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = Vinotinto,
                    uncheckedColor = Vinotinto,
                    checkmarkColor = Blanco
                )
            )
            Text(
                text = "Recordar Contraseña",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        }

        // 🔹 Espacio original (no modificado)
        Spacer(Modifier.height(200.dp))

        // 🔹 Bloque gris (a todo el ancho, sin esquinas redondeadas y más alto)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 260.dp) // más alto para dejar fondo debajo del texto
                .background(GrisOscuro, RoundedCornerShape(12.dp))
                .padding(vertical = 24.dp, horizontal = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onLogin(email, pwd, remember) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Vinotinto,
                        contentColor = Blanco
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(52.dp)
                ) {
                    Text(
                        "Iniciar Sesión",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 20.sp)
                    )
                }

                Spacer(Modifier.height(12.dp))
                Row {
                    Text(
                        text = "¿No tienes cuenta? ",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )
                    Text(
                        text = "crea una",
                        color = Color(0xFF2196F3),
                        textDecoration = TextDecoration.Underline,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable { onCreateAccountClick() }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LogInPreview() {
    NavEventTheme { LogInScreen() }
}
