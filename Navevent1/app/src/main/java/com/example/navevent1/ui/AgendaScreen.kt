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

//composable de pantalla agenda
@Composable
fun AgendaScreen() {
    //mock de eventos
    val eventos = listOf(
        "Charla de apertura - 9:00 AM",
        "Panel de innovación - 10:30 AM",
        "Networking con expositores - 12:00 PM",
        "Taller de UX Design - 2:00 PM",
        "Conferencia de cierre - 5:00 PM"
    )

    //lista que muestra cada evento
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(eventos) { evento ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = evento,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 16.sp
                )
            }
        }
    }
}