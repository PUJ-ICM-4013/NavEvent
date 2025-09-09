package com.example.navevent1.ui


import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//composable de pantalla de panel de control para organizadores
@Composable
fun OrganizadorScreen() {
    val context = LocalContext.current


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Panel del Organizador", fontSize = 22.sp)

        //mock boton crear evento
        Button(onClick = {
            Toast.makeText(context, "Evento creado (mock)", Toast.LENGTH_SHORT).show()
        }) {
            Text("Crear nuevo evento")
        }

        //mock boton enviar alerta
        Button(onClick = {
            Toast.makeText(context, "Alerta enviada (mock)", Toast.LENGTH_SHORT).show()
        }) {
            Text("Enviar alerta de evacuación")
        }

        //mock boton marcar como destacado
        Button(onClick = {
            Toast.makeText(context, "Stand marcado como destacado (mock)", Toast.LENGTH_SHORT).show()
        }) {
            Text("Destacar stand")
        }
    }
}