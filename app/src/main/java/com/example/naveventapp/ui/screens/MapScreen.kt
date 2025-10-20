package com.example.naveventapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.naveventapp.ui.components.BottomBar
import com.example.naveventapp.ui.permissions.rememberLocationPermission
import com.example.naveventapp.ui.theme.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapScreen(
    onNavAgenda: () -> Unit = {},
    onNavQr: () -> Unit = {},
    onNavProfile: () -> Unit = {},
    onBellClick: () -> Unit = {}
) {
    val myLocationEnabled = rememberLocationPermission()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(4.5981, -74.0760), 16f)
    }

    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()            // 1) baja todo para no chocar con la barra del sistema
            .padding(top = 8.dp)            // pequeño aire extra opcional
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp)     // 2) deja espacio para la bottom bar (altura aprox.)
        ) {
            // Mapa arriba, ocupa el espacio restante
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = myLocationEnabled),
                    uiSettings = MapUiSettings(
                        myLocationButtonEnabled = true,
                        zoomControlsEnabled = false,
                        compassEnabled = true
                    )
                )
            }

            // Leyenda debajo del mapa (más alta)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp) // ↑ antes 90dp; ahora más espacio para tu contenido
                    .background(Blanco.copy(alpha = 0.9f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Leyenda / Referencias (en construcción)",
                    style = MaterialTheme.typography.bodyMedium.copy(color = GrisOscuro)
                )
            }
        }

        // Campana arriba a la derecha
        IconButton(
            onClick = onBellClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 12.dp)
                .background(Blanco.copy(alpha = 0.9f), shape = MaterialTheme.shapes.small)
        ) {
            Icon(Icons.Filled.Notifications, contentDescription = "Notificaciones", tint = Vinotinto)
        }

        // Bottom bar fijo al fondo
        BottomBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            onMap = { /* ya estás en el mapa */ },
            onAgenda = onNavAgenda,
            onQr = onNavQr,
            onProfile = onNavProfile
        )
    }
}



