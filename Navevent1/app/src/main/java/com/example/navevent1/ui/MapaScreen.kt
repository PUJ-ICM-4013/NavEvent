package com.example.navevent1.ui


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.navevent1.R

//composable de pantalla de mapa
@Composable
fun MapaScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mapa del Evento", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))
        //imagen mock del mapa
        Image(
            painter = painterResource(id = R.drawable.mapa_mock),
            contentDescription = "Mapa del evento",
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
        )
    }
}