package com.example.navevent1.ui


import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.navevent1.R

//composable de pantalla escaneo de QR
@Composable
fun QRScreen() {
    val context = LocalContext.current


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Escáner QR", fontSize = 22.sp)


        Image(
            painter = painterResource(id = R.drawable.qr_mock),
            contentDescription = "Imagen QR",
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(220.dp)
        )

        //mock boton escanear QR
        Button(onClick = {
            Toast.makeText(context, "QR escaneado: Stand 15 - Promoción activa!", Toast.LENGTH_SHORT).show()
        }) {
            Text("Simular escaneo")
        }
    }
}