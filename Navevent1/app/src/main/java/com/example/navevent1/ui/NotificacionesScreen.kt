package com.example.navevent1.ui


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//composable de pantalla de Pantalla de Notificaciones
@Composable
fun NotificacionesScreen() {
    //mock de notificaciones
    val notificaciones = listOf(
        "Cambio de sala para el taller de UX",
        "Nueva promoción en el stand 12",
        "Se acerca la charla de cierre",
        "Recuerda visitar los stands destacados",
        "Evita aglomeraciones: accede por la entrada norte"
    )


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(notificaciones) { mensaje ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = mensaje,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 15.sp
                )
            }
        }
    }
}