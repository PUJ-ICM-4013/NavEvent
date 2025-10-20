package com.example.naveventapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.naveventapp.ui.theme.*

@Composable
fun SignInScreen(
    onRegister: (name: String, lastName: String, email: String, password: String, confirm: String, termsAccepted: Boolean) -> Unit = { _,_,_,_,_,_ -> },
    onTermsClick: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var acceptTerms by remember { mutableStateOf(false) }

    // ✅ Scroll habilitado
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState) // <--- habilita el scroll
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Spacer(Modifier.height(70.dp))

        // 🔹 Título centrado
        Text(
            text = "Crear Cuenta",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(40.dp))

        // Campo: Nombres
        Text("Nombres", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = Color.Black))
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                cursorColor = Vinotinto
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Campo: Apellidos
        Text("Apellidos", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = Color.Black))
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                cursorColor = Vinotinto
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Campo: Correo electrónico
        Text("Correo Electrónico", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = Color.Black))
        Spacer(Modifier.height(6.dp))
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
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Campo: Contraseña
        Text("Contraseña", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = Color.Black))
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Text(
                    text = if (showPassword) "Ocultar" else "Ver",
                    color = Vinotinto,
                    modifier = Modifier.clickable { showPassword = !showPassword }
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                cursorColor = Vinotinto
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Campo: Confirmar contraseña
        Text("Confirmar contraseña", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = Color.Black))
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            singleLine = true,
            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Text(
                    text = if (showConfirmPassword) "Ocultar" else "Ver",
                    color = Vinotinto,
                    modifier = Modifier.clickable { showConfirmPassword = !showConfirmPassword }
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                cursorColor = Vinotinto
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        // Checkbox de términos
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = acceptTerms,
                onCheckedChange = { acceptTerms = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = Vinotinto,
                    uncheckedColor = Vinotinto,
                    checkmarkColor = Blanco
                )
            )
            Text(
                text = "Acepto los ",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
            Text(
                text = "Términos y Condiciones",
                style = MaterialTheme.typography.bodyMedium.copy(color = Vinotinto),
                modifier = Modifier.clickable { onTermsClick() }
            )
        }

        Spacer(Modifier.height(80.dp))

        // 🔹 Bloque gris inferior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp)
                .background(GrisOscuro)
                .padding(vertical = 24.dp, horizontal = 16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onRegister(name, lastName, email, password, confirmPassword, acceptTerms) },
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
                        "Registrarse",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 20.sp)
                    )
                }
            }
        }

        Spacer(Modifier.height(30.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun SignInPreview() {
    NavEventTheme { SignInScreen() }
}

