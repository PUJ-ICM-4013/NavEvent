package com.example.naveventapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.naveventapp.data.DirectionsService
import com.example.naveventapp.ui.components.BottomBar
import com.example.naveventapp.ui.location.LocationTracker
import com.example.naveventapp.ui.location.LegendBar
import com.example.naveventapp.ui.permissions.rememberLocationPermission
import com.example.naveventapp.ui.theme.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.launch

private fun distMeters(a: LatLng, b: LatLng): Float {
    val out = FloatArray(1)
    android.location.Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, out)
    return out[0]
}

/** índice del punto de la ruta (route) más cercano a "pos". Si route está vacío, -1 */
private fun nearestIndexOnRoute(route: List<LatLng>, pos: LatLng): Int {
    if (route.isEmpty()) return -1
    var bestIdx = 0
    var best = Float.MAX_VALUE
    for (i in route.indices) {
        val d = distMeters(route[i], pos)
        if (d < best) { best = d; bestIdx = i }
    }
    return bestIdx
}

/** recorta la ruta para mostrar solo desde tu pos hasta el final */
private fun remainingRouteFromPos(pos: LatLng, full: List<LatLng>): List<LatLng> {
    if (full.size < 2) return emptyList()
    val idx = nearestIndexOnRoute(full, pos)
    val tail = if (idx in full.indices) full.drop(idx) else full
    // preponer la ubicación actual para que el primer tramo sea real
    return listOf(pos) + tail
}

@Composable
fun MapScreen(
    onNavAgenda: () -> Unit = {},
    onNavQr: () -> Unit = {},
    onNavProfile: () -> Unit = {},
    onBellClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val apiKey = getMapsApiKey(context)
    val scope = rememberCoroutineScope()

    // 1) Ubicación actual y permiso
    val myLocationEnabled = rememberLocationPermission()
    val currentLatLng by produceState<LatLng?>(initialValue = null, key1 = myLocationEnabled) {
        if (myLocationEnabled) {
            LocationTracker.locationFlow(context).collect { value = it }
        } else value = null
    }

    // 2) Cámara
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(4.5981, -74.0760), 16f)
    }

    // 3) Estados de routing
    var destination by remember { mutableStateOf<LatLng?>(null) }
    var route by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var routeSummary by remember { mutableStateOf<Pair<String, String>?>(null) } // (distancia, duración)
    var isLoadingRoute by remember { mutableStateOf(false) }
    var hasCenteredOnUser by remember { mutableStateOf(false) }
    var followUser by remember { mutableStateOf(false) }

    LaunchedEffect(currentLatLng) {
        if (!hasCenteredOnUser) {
            currentLatLng?.let { here ->
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(here, 17f),
                    durationMs = 600
                )
                hasCenteredOnUser = true  // 👈 no volver a centrar automáticamente
            }
        }
    }

    LaunchedEffect(destination, currentLatLng) {
        val origin = currentLatLng
        val dest = destination
        if (origin != null && dest != null && apiKey.isNotBlank()) {
            isLoadingRoute = true
            val result = runCatching {
                DirectionsService.fetchRoute(origin, dest, apiKey, mode = "walking")
            }.getOrNull()

            // si falla Directions, nos quedamos con la recta que pusimos en onMapClick
            if (result != null && result.points.size >= 2) {
                // guarda la ruta COMPLETA (sin recortar)
                route = result.points
                // si quieres encuadrar de una, descomenta:
                // fitToRoute(result.points)
            }
            isLoadingRoute = false
        }
    }

    // recorte dinámico según te mueves (sin volver a llamar Directions)
    LaunchedEffect(currentLatLng) {
        val origin = currentLatLng
        val dest = destination
        if (origin != null && dest != null) {
            // si aún no hay ruta Directions, al menos mantén la recta:
            if (route.size >= 2) {
                // recorta la ruta para mostrar tramo restante
                val remaining = remainingRouteFromPos(origin, route)
                // OJO: si remaining termina muy pegado al destino, mantenlo (2 puntos)
                if (remaining.size >= 2) {
                    // pintamos el tramo restante
                    route = remaining
                } else {
                    // mínimo línea recta como fallback
                    route = listOf(origin, dest)
                }
            } else {
                route = listOf(origin, dest)
            }
        }
    }

    // efecto que mueve la cámara SOLO si followUser está activo
    LaunchedEffect(currentLatLng, followUser) {
        if (followUser && currentLatLng != null) {
            cameraPositionState.move(CameraUpdateFactory.newLatLng(currentLatLng!!))
        }
    }

    fun clearRoute() {
        destination = null
        route = emptyList()
        routeSummary = null
    }

    // 7) UI
    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 8.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = myLocationEnabled && currentLatLng != null
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        compassEnabled = true,
                        indoorLevelPickerEnabled = true,
                        scrollGesturesEnabled = true,
                        zoomGesturesEnabled = true,
                        mapToolbarEnabled = true,
                        tiltGesturesEnabled = true,
                    ),
                    onMapClick = { latLng ->
                        //Click fija destino
                        destination = latLng
                        followUser = true
                        currentLatLng?.let { here ->
                            route = listOf(here, latLng)
                        }

                        // centrar cámara en el destino
                        scope.launch {
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newLatLngZoom(latLng, 17f),
                                durationMs = 450
                            )
                        }
                    },
                    onMapLongClick = {
                        // Long press para limpiar (opcional)
                        clearRoute()
                    }
                ) {
                    // Destino (rojo)
                    destination?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = "Destino",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                    }

                    // Polyline de la ruta
                    if (route.isNotEmpty()) {
                        Polyline(points = route, width = 10f, color = Vinotinto, zIndex = 2f)
                    }
                }

                // Loading
                if (isLoadingRoute) {
                    CircularProgressIndicator(
                        color = Vinotinto,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    )
                }

                // Resumen ruta (distancia — duración) + limpiar
                routeSummary?.let { (dist, dur) ->
                    Surface(
                        tonalElevation = 4.dp,
                        shape = MaterialTheme.shapes.medium,
                        color = Blanco.copy(alpha = 0.92f),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("$dist — $dur", color = Negro)
                            Spacer(Modifier.width(12.dp))
                            IconButton(onClick = { clearRoute() }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = Vinotinto)
                            }
                        }
                    }
                }
            }

            // (Opcional) Tu leyenda de POI u otros componentes
            LegendBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Blanco.copy(alpha = 0.95f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }

        // Campana
        IconButton(
            onClick = onBellClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 12.dp, end = 12.dp)
                .background(Blanco.copy(alpha = 0.9f), shape = MaterialTheme.shapes.small)
        ) {
            Icon(Icons.Filled.Notifications, contentDescription = "Notificaciones", tint = Vinotinto)
        }

        // Bottom bar
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

private fun getMapsApiKey(context: Context): String {
    return try {
        val ai: ApplicationInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
        ai.metaData.getString("com.google.android.geo.API_KEY") ?: ""
    } catch (_: Exception) {
        ""
    }
}



