package com.example.navevent1.ui


import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//composable de pantalla perfil del usuario
@Composable
fun PerfilScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Perfil de Usuario", fontSize = 22.sp)
        Spacer(modifier = Modifier.height(24.dp))


        Text("Nombre: Juan Pérez", fontSize = 16.sp)
        Text("Correo: juanperez@email.com", fontSize = 16.sp)
        Text("Rol: Asistente", fontSize = 16.sp)


        Spacer(modifier = Modifier.height(32.dp))


        Button(onClick = { /* Acción mock */ }) {
            Text("Cerrar sesión")
        }
    }
}