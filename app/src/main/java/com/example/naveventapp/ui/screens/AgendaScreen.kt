package com.example.naveventapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.naveventapp.ui.components.BottomBar
import com.example.naveventapp.ui.theme.*
import com.example.naveventapp.ui.location.LocationTracker
import com.example.naveventapp.ui.permissions.rememberLocationPermission
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

@Composable
fun AgendaScreen(
    onOpenMapTo: (LatLng, String) -> Unit = { _, _ -> },
    onNavMap: () -> Unit = {},
    onNavAgenda: () -> Unit = {},
    onNavQr: () -> Unit = {},
    onNavProfile: () -> Unit = {},
    onBellClick: () -> Unit = {}
) {
    val context = LocalContext.current

    // Ubicación del usuario
    val myLocationEnabled = rememberLocationPermission()
    val currentLatLng by produceState<LatLng?>(initialValue = null, key1 = myLocationEnabled) {
        if (myLocationEnabled) {
            LocationTracker.locationFlow(context).collect { value = it }
        } else value = null
    }

    // Si aún no hay ubicación, usa un centro por defecto
    val venueCenter = currentLatLng ?: LatLng(4.5981, -74.0760)

    // Lista de agenda (hora y descripción alternadas)
    val agenda = listOf(
        "09:00" to "Charla de apertura",
        ""      to "Charla en el auditorio Restrepo",
        "10:30" to "Panel de Innovación",
        ""      to "Se encuentra en el pabellón 1",
        "12:00" to "Networking con expositores",
        ""      to "Se encuentra sujeto a cambio de horario",
        "14:00" to "Taller de UX design",
        ""      to "Se encuentra sujeto a cambio de ubicación",
        "17:00" to "Conferencia de cierre",
        ""      to "Cierre de puertas"
    )

    // Coordenadas de eventos alternadas (sólo para los pares con hora)
    val eventOffsets = remember(venueCenter) {
        listOf(
            SphericalUtil.computeOffset(venueCenter, 60.0, 45.0),
            SphericalUtil.computeOffset(venueCenter, 90.0, 135.0),
            SphericalUtil.computeOffset(venueCenter, 80.0, 300.0),
            SphericalUtil.computeOffset(venueCenter, 110.0, 200.0),
            SphericalUtil.computeOffset(venueCenter, 70.0, 10.0),
        )
    }

    val RojoClaro = Color(0xFFF6DADA)

    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 8.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(bottom = 90.dp)
        ) {
            Text("Agenda", style = MaterialTheme.typography.headlineSmall, color = Color.Black)
            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(agenda) { index, (hora, texto) ->
                    val esHora = hora.isNotEmpty()
                    if (esHora) {
                        val eventoIdx = index / 2
                        val destino = eventOffsets.getOrNull(eventoIdx)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    destino?.let { onOpenMapTo(it, texto) }
                                }
                        ) {
                            Text(
                                text = hora,
                                color = GrisOscuro,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(64.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = RojoClaro,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, Vinotinto),
                                modifier = Modifier
                                    .height(56.dp)
                                    .weight(1f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.CenterStart,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    Text(
                                        text = texto,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    } else {
                        // Descripción debajo
                        Surface(
                            color = Blanco,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, Vinotinto),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.CenterStart,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = texto,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 17.sp),
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Icono campana
        IconButton(
            onClick = onBellClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 12.dp)
                .background(Blanco.copy(alpha = 0.9f), shape = MaterialTheme.shapes.small)
        ) {
            Icon(Icons.Filled.Notifications, contentDescription = "Notificaciones", tint = Vinotinto)
        }

        // Barra inferior
        BottomBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            onMap = onNavMap,
            onAgenda = onNavAgenda,
            onQr = onNavQr,
            onProfile = onNavProfile
        )
    }
}