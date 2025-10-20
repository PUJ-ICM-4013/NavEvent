package com.example.naveventapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.naveventapp.ui.theme.*

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    onMap: () -> Unit = {},
    onAgenda: () -> Unit = {},
    onQr: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    Surface(
        color = GrisOscuro,
        shadowElevation = 10.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(vertical = 10.dp)
        ) {
            //  Botón de Mapa
            IconButton(onClick = onMap, modifier = Modifier.size(56.dp)) {
                Icon(
                    Icons.Default.Map,
                    contentDescription = "Mapa",
                    tint = Blanco,
                    modifier = Modifier.size(40.dp)
                )
            }

            //  Botón de Agenda
            IconButton(onClick = onAgenda, modifier = Modifier.size(56.dp)) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = "Agenda",
                    tint = Blanco,
                    modifier = Modifier.size(40.dp)
                )
            }

            //  Botón de QR
            IconButton(onClick = onQr, modifier = Modifier.size(56.dp)) {
                Icon(
                    Icons.Default.QrCode,
                    contentDescription = "QR",
                    tint = Blanco,
                    modifier = Modifier.size(40.dp)
                )
            }

            //  Botón de Perfil
            IconButton(onClick = onProfile, modifier = Modifier.size(56.dp)) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Perfil",
                    tint = Blanco,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}


