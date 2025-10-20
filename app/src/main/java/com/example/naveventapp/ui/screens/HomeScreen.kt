package com.example.naveventapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.naveventapp.ui.theme.*

@Composable
fun HomeScreen(
    onLoginClick: () -> Unit = {},
    onCreateAccountClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisOscuro)
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(90.dp))

            // Nombre de la app -> Cutive 32 (displaySmall del tema)
            Text(
                text = "NavEvent",
                style = MaterialTheme.typography.displaySmall // Cutive 32 del Theme.kt
            )

            Spacer(Modifier.height(40.dp))

            // Marco circular
            Box(
                modifier = Modifier
                    .size(270.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCBCBCB))
            )

            Spacer(Modifier.height(28.dp))

            // Slogan -> Calligraffitti (titleSmall del tema), alineado a la izquierda
            Text(
                text = "\"Eventos que fluyen contigo\"",
                style = MaterialTheme.typography.titleSmall, // Calligraffitti del Theme.kt
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )

            Spacer(Modifier.height(100.dp))

            // Botón Iniciar Sesión -> Cairo (labelLarge del tema)
            Button(
                onClick = onLoginClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Vinotinto,
                    contentColor = Blanco
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp)
            ) {
                Text(
                    text = "Iniciar Sesión",
                    style = MaterialTheme.typography.labelLarge // Cairo Bold
                )
            }

            Spacer(Modifier.height(10.dp))

            // Link crear cuenta -> Cairo (bodyMedium) y "crea una" en azul
            Row {
                Text(
                    text = "¿No tienes cuenta? ",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White) // Cairo
                )
                Text(
                    text = "crea una",
                    color = Color(0xFF2196F3), // Azul
                    style = MaterialTheme.typography.bodyMedium, // Cairo
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onCreateAccountClick() }
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    NavEventTheme { HomeScreen() }
}


