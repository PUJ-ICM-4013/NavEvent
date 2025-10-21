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
import com.example.naveventapp.sensors.isNightModeFlow
import com.example.naveventapp.R
import com.example.naveventapp.sensors.compassAzimuthFlow
import com.example.naveventapp.ui.components.CompassOverlay
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MapStyleOptions
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
    // --- claves y scope ---
    val context = LocalContext.current
    val apiKey = getWebApiKey(context)   // ← usa la WEB_API_KEY (Directions/Roads)
    val scope = rememberCoroutineScope()

    val isNight by isNightModeFlow(context).collectAsState(initial = false)
// Carga el estilo del mapa según el estado
    val mapStyleOptions = remember(isNight) {
        try {
            MapStyleOptions.loadRawResourceStyle(
                context,
                if (isNight) R.raw.map_style_night else R.raw.map_style_day
            )
        } catch (_: Exception) {
            null // si falla la carga, sin estilo
        }
    }

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

    // Sensor Brujula
    var autoRotate by remember { mutableStateOf(false) }

    // Azimuth: solo lo coleccionamos siempre (barato), tú decides si aplicarlo con "autoRotate"
    val azimuth by remember(context) {
        compassAzimuthFlow(context)
    }.collectAsState(initial = 0f)

    // Throttle simple para no spamear animaciones
    var lastAppliedBearing by remember { mutableStateOf<Float?>(null) }
    val BEARING_THRESHOLD_DEG = 5f

    // Aplica la rotación de cámara cuando autoRotate está ON y cambie el rumbo “lo suficiente”
    LaunchedEffect(autoRotate, azimuth) {
        if (!autoRotate) return@LaunchedEffect
        val currentBearing = azimuth
        val last = lastAppliedBearing
        val delta = if (last == null) 360f else kotlin.math.abs(currentBearing - last)
        if (last == null || delta >= BEARING_THRESHOLD_DEG) {
            val current = cameraPositionState.position
            val newCam = CameraPosition(
                /* target = */ current.target,
                /* zoom   = */ current.zoom,
                /* tilt   = */ current.tilt,
                /* bearing*/ currentBearing
            )
            cameraPositionState.animate(
                update = CameraUpdateFactory.newCameraPosition(newCam),
                durationMs = 180
            )
            lastAppliedBearing = currentBearing
        }
    }

// 3) Estados de routing
    var destination by remember { mutableStateOf<LatLng?>(null) }
    var route by remember { mutableStateOf<List<LatLng>>(emptyList()) }           // tramo mostrado
    var fullRoute by remember { mutableStateOf<List<LatLng>>(emptyList()) }       // ruta completa (no recortada)
    var routeSummary by remember { mutableStateOf<Pair<String, String>?>(null) }  // (distancia, duración) opcional
    var isLoadingRoute by remember { mutableStateOf(false) }
    var hasCenteredOnUser by remember { mutableStateOf(false) }

// Follow con umbral / llegada
    var followUser by remember { mutableStateOf(false) }
    var lastFollowedPos by remember { mutableStateOf<LatLng?>(null) }
    val FOLLOW_THRESHOLD_M = 6f
    val ARRIVAL_RADIUS_M   = 15f

// 4) Centrar SOLO la primera vez que llega ubicación
    LaunchedEffect(currentLatLng) {
        if (!hasCenteredOnUser) {
            currentLatLng?.let { here ->
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(here, 17f),
                    durationMs = 600
                )
                hasCenteredOnUser = true
            }
        }
    }

// 5) Calcular ruta inteligente (Directions → Roads snap → recta)
    LaunchedEffect(destination, currentLatLng) {
        val origin = currentLatLng
        val dest = destination
        if (origin != null && dest != null && apiKey.isNotBlank()) {
            isLoadingRoute = true

            val smart = DirectionsService.fetchSmartRoute(
                origin = origin,
                destination = dest,
                apiKey = apiKey,
                mode = "walking"
            )

            if (smart.size >= 2) {
                fullRoute = smart      // guarda la ruta completa
                route = smart          // muestra completa al inicio
                // routeSummary = result?.let { it.distanceText to it.durationText } // si lo expones desde tu service
                // fitToRoute(smart) // opcional: encuadrar la ruta completa
            } else {
                fullRoute = listOf(origin, dest)
                route = fullRoute
            }

            isLoadingRoute = false
        }
    }

// 6) Recorte dinámico según te mueves (SIEMPRE desde fullRoute)
    LaunchedEffect(currentLatLng) {
        val origin = currentLatLng
        val dest = destination
        if (origin != null && dest != null && fullRoute.size >= 2) {
            val remaining = remainingRouteFromPos(origin, fullRoute)
            route = if (remaining.size >= 2) remaining else listOf(origin, dest)
        }
    }

// 7) Follow: mover cámara solo si avanzó (umbral) y detener al llegar
    LaunchedEffect(currentLatLng, followUser, destination) {
        val pos = currentLatLng ?: return@LaunchedEffect
        if (!followUser) return@LaunchedEffect

        // parar follow si ya estás muy cerca del destino
        destination?.let { dest ->
            if (distMeters(pos, dest) <= ARRIVAL_RADIUS_M) {
                lastFollowedPos = null
                // followUser = false // descomenta si quieres apagarlo automáticamente
                return@LaunchedEffect
            }
        }

        val movedEnough = lastFollowedPos == null || distMeters(lastFollowedPos!!, pos) >= FOLLOW_THRESHOLD_M
        if (movedEnough) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLng(pos),
                durationMs = 300
            )
            lastFollowedPos = pos
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
                        mapStyleOptions = mapStyleOptions,
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

                CompassOverlay(
                    azimuthDeg = azimuth,
                    autoRotate = autoRotate,
                    onToggleAutoRotate = { autoRotate = it },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 64.dp, end = 12.dp)
                )

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

private fun getWebApiKey(context: Context): String {
    return try {
        val ai = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
        ai.metaData.getString("com.example.naveventapp.WEB_API_KEY") ?: ""
    } catch (_: Exception) { "" }
}



