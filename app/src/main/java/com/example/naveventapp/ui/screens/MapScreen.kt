package com.example.naveventapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.naveventapp.data.DirectionsService
import com.example.naveventapp.ui.components.BottomBar
import com.example.naveventapp.ui.permissions.rememberLocationPermission
import com.example.naveventapp.ui.theme.*
import com.example.naveventapp.ui.location.LocationTracker
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.example.naveventapp.ui.location.LegendBar
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.google.android.gms.maps.model.BitmapDescriptor

private enum class PoiKind { STAND, BANO, ENTRADA, RESTAURANTE, INFO }

private fun hueFor(kind: PoiKind): Float = when (kind) {
    PoiKind.STAND       -> BitmapDescriptorFactory.HUE_VIOLET
    PoiKind.BANO        -> BitmapDescriptorFactory.HUE_GREEN
    PoiKind.ENTRADA     -> BitmapDescriptorFactory.HUE_ORANGE
    PoiKind.RESTAURANTE -> BitmapDescriptorFactory.HUE_YELLOW
    PoiKind.INFO        -> BitmapDescriptorFactory.HUE_CYAN
}

private fun iconFor(kind: PoiKind): BitmapDescriptor =
    BitmapDescriptorFactory.defaultMarker(hueFor(kind))

private fun randomAround(center: LatLng, dx: Double, dy: Double) =
    LatLng(center.latitude + dy, center.longitude + dx)

@Composable
fun MapScreen(
    onNavAgenda: () -> Unit = {},
    onNavQr: () -> Unit = {},
    onNavProfile: () -> Unit = {},
    onBellClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiKey = getMapsApiKey(context)

    val myLocationEnabled = rememberLocationPermission()

    val currentLatLng by produceState<LatLng?>(initialValue = null, key1 = myLocationEnabled) {
        if (myLocationEnabled) {
            LocationTracker.locationFlow(context).collect { value = it }
        } else {
            value = null
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(4.5981, -74.0760), 16f)
    }

    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }

    // Estado para la polyline de Directions
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var isLoadingRoute by remember { mutableStateOf(false) }

    val baseCenter = currentLatLng ?: LatLng(4.5981, -74.0760)

    val poiMarkers by remember(baseCenter) {
        mutableStateOf(
            buildList {
                val deltas = listOf(
                    0.0004 to 0.0002, -0.0003 to 0.0001, 0.0002 to -0.00025,
                    -0.00015 to -0.0002, 0.00035 to 0.00015
                )
                // Stands (3)
                addAll(List(3) { PoiKind.STAND to randomAround(baseCenter, deltas[it % deltas.size].first, deltas[it % deltas.size].second) })
                // Baños (2)
                addAll(List(2) { PoiKind.BANO to randomAround(baseCenter, deltas[(it+1) % deltas.size].first, -deltas[(it+1) % deltas.size].second) })
                // Entradas/Salidas (2)
                addAll(List(2) { PoiKind.ENTRADA to randomAround(baseCenter, -deltas[(it+2) % deltas.size].first, deltas[(it+2) % deltas.size].second) })
                // Restaurantes (2)
                addAll(List(2) { PoiKind.RESTAURANTE to randomAround(baseCenter, deltas[(it+3) % deltas.size].first, deltas[(it+3) % deltas.size].second) })
                // Info (1)
                add(PoiKind.INFO to randomAround(baseCenter, 0.0001, -0.0001))
            }
        )
    }

    LaunchedEffect(currentLatLng) {
        currentLatLng?.let { here ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(here, 17f)
            )
        }
    }

    // cuando ambos puntos existen, pedimos la ruta real
    LaunchedEffect(currentLatLng, selectedLatLng) {
        val origin = currentLatLng
        val dest = selectedLatLng
        if (origin != null && dest != null && apiKey.isNotBlank()) {
            isLoadingRoute = true
            routePoints = emptyList()
            routePoints = runCatching {
                DirectionsService.fetchRoutePolyline(origin, dest, apiKey, mode = "walking")
            }.getOrDefault(emptyList())
            isLoadingRoute = false
        } else {
            routePoints = emptyList()
        }
    }

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
                    properties = MapProperties(isMyLocationEnabled = myLocationEnabled),
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
                        routePoints = emptyList()
                        selectedLatLng = latLng
                    }
                ) {
                    currentLatLng?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = "Mi ubicación",
                            snippet = "Tu ubicación actual",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE) // azul
                        )
                    }
                    selectedLatLng?.let {
                        Marker(state = MarkerState(position = it), title = "Destino", snippet = "Punto seleccionado")
                    }

                    poiMarkers.forEach { (kind, pos) ->
                        Marker(
                            state = MarkerState(position = pos),
                            title = when (kind) {
                                PoiKind.STAND       -> "Stand"
                                PoiKind.BANO        -> "Baños"
                                PoiKind.ENTRADA     -> "Entrada/Salida"
                                PoiKind.RESTAURANTE -> "Restaurante"
                                PoiKind.INFO        -> "Información"
                            },
                            icon = iconFor(kind)
                        )
                    }

                    if (routePoints.isNotEmpty()) {
                        Polyline(points = routePoints, width = 10f, color = Vinotinto)
                    }
                }

                if (isLoadingRoute) {
                    CircularProgressIndicator(
                        color = Vinotinto,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    )
                }
            }

            // Leyenda desde ui/poi
            LegendBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Blanco.copy(alpha = 0.95f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }

        IconButton(
            onClick = onBellClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 12.dp, end = 12.dp)
                .background(Blanco.copy(alpha = 0.9f), shape = MaterialTheme.shapes.small)
        ) {
            Icon(Icons.Filled.Notifications, contentDescription = "Notificaciones", tint = Vinotinto)
        }

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


