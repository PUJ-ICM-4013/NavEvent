package com.example.naveventapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.toArgb
import com.example.naveventapp.ui.location.LegendBar
import com.example.naveventapp.ui.location.Poi
import com.example.naveventapp.ui.location.poiLegend
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.naveventapp.ui.components.BottomBar
import com.example.naveventapp.ui.location.LocationTracker
import com.example.naveventapp.ui.permissions.rememberLocationPermission
import com.example.naveventapp.ui.theme.*
import com.example.naveventapp.sensors.isNightModeFlow
import com.example.naveventapp.R
import com.example.naveventapp.sensors.compassAzimuthFlow
import com.example.naveventapp.ui.components.CompassOverlay
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import com.example.naveventapp.data.DirectionsService
import com.example.naveventapp.utils.getMetaDataValue

private fun Color.asHue(): Float {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    return hsv[0] // hue
}

@Composable
fun MapScreen(
    onNavAgenda: () -> Unit = {},
    onNavQr: () -> Unit = {},
    onNavProfile: () -> Unit = {},
    onBellClick: () -> Unit = {}
) {
    // --- contexto y scope
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Key Web para Directions desde el Manifest
    val webApiKey = remember { getMetaDataValue(context, "com.example.naveventapp.WEB_API_KEY") }

    val isNight by isNightModeFlow(context).collectAsState(initial = false)
    val mapStyleOptions = remember(isNight) {
        try {
            MapStyleOptions.loadRawResourceStyle(
                context,
                if (isNight) R.raw.map_style_night_clean else R.raw.map_style_day_clean
            )
        } catch (_: Exception) {
            null
        }
    }

    // === ubicación actual & permiso ===
    val myLocationEnabled = rememberLocationPermission()
    val currentLatLng by produceState<LatLng?>(initialValue = null, key1 = myLocationEnabled) {
        if (myLocationEnabled) {
            LocationTracker.locationFlow(context).collect { value = it }
        } else value = null
    }

    var destination by remember { mutableStateOf<LatLng?>(null) }
    var hasCenteredOnUser by remember { mutableStateOf(false) }

    // === cámara ===
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(4.5981, -74.0760), 16f)
    }

    // === brújula / autorotate ===
    var autoRotate by remember { mutableStateOf(false) }
    val azimuth by remember(context) { compassAzimuthFlow(context) }.collectAsState(initial = 0f)
    var lastAppliedBearing by remember { mutableStateOf<Float?>(null) }
    val bearingThresholdDeg = 5f

    LaunchedEffect(autoRotate, azimuth) {
        if (!autoRotate) return@LaunchedEffect
        val currentBearing = azimuth
        val last = lastAppliedBearing
        val delta = if (last == null) 360f else kotlin.math.abs(currentBearing - last)
        if (last == null || delta >= bearingThresholdDeg) {
            val current = cameraPositionState.position
            val newCam = CameraPosition(current.target, current.zoom, current.tilt, currentBearing)
            cameraPositionState.animate(
                update = CameraUpdateFactory.newCameraPosition(newCam),
                durationMs = 180
            )
            lastAppliedBearing = currentBearing
        }
    }

    // centrar una vez en el usuario
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

    // ====== Estado de la ruta (Directions) ======
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var routeJob by remember { mutableStateOf<Job?>(null) }
    val routeMode = "walking" // <-- modo fijo para eventos

    fun recomputeRoute(origin: LatLng, dest: LatLng) {
        routeJob?.cancel()
        routeJob = scope.launch {
            val res = DirectionsService.fetchRoute(
                origin = origin,
                destination = dest,
                apiKey = webApiKey,
                mode = routeMode,  // "walking"
                language = "es",
                region = "CO"
            )
            if (res != null && res.points.size >= 2) {
                routePoints = res.points
            } else {
                // fallback: línea recta
                routePoints = listOf(origin, dest)
            }
        }
    }

    // Recalcular automáticamente si te mueves (y hay destino)
    LaunchedEffect(currentLatLng, destination, webApiKey) {
        val o = currentLatLng
        val d = destination
        if (o != null && d != null && webApiKey.isNotBlank()) {
            recomputeRoute(o, d)
        }
    }

    //Poi Y sus localizaciones en el mapa
    var lockedCenter by remember { mutableStateOf<LatLng?>(null) }
    val fallbackBogota = LatLng(4.5981, -74.0760)
    val moveThresholdMeters = 2000.0

    LaunchedEffect(currentLatLng) {
        if (lockedCenter == null && currentLatLng != null) {
            lockedCenter = currentLatLng
        }
    }

    LaunchedEffect(currentLatLng, lockedCenter) {
        val here = currentLatLng
        val center = lockedCenter
        if (here != null && center != null) {
            val d = SphericalUtil.computeDistanceBetween(here, center)
            if (d > moveThresholdMeters) {
                lockedCenter = here
                // Opcional: encuadrar nuevamente para ver POIs alrededor del nuevo centro
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(here, 17f),
                    durationMs = 500
                )
            }
        }
    }

    val venueCenter: LatLng = lockedCenter ?: currentLatLng ?: fallbackBogota

    // Lista de POIs (ejemplo; cambia coordenadas por las reales)
    val defaultPois: List<Poi> = remember(venueCenter) {
        listOf(
            // STANDS: aumentamos distancias (en metros) y añadimos uno más
            Poi(poiLegend[0], SphericalUtil.computeOffset(venueCenter,  60.0,  45.0), "Stand A"),
            Poi(poiLegend[0], SphericalUtil.computeOffset(venueCenter,  90.0, 135.0), "Stand B"),
            Poi(poiLegend[0], SphericalUtil.computeOffset(venueCenter, 120.0, 280.0), "Stand C"),
            // BAÑOS
            Poi(poiLegend[1], SphericalUtil.computeOffset(venueCenter,  110.0,   180.0), "Baño Norte"),
            // ENTRADA / SALIDA
            Poi(poiLegend[2], SphericalUtil.computeOffset(venueCenter, 70.0, 0.0), "Entrada Principal"),
            // RESTAURANTE
            Poi(poiLegend[3], SphericalUtil.computeOffset(venueCenter, 130.0, 260.0), "Restaurante 1"),
            // INFO
            Poi(poiLegend[4], SphericalUtil.computeOffset(venueCenter,  90.0,  90.0), "Punto de Información")
        )
    }

// Filtro por tipo (para activar/desactivar desde la leyenda)
    //  Por defecto: ningún tipo activo (leyenda desactivada)
    var activeTypes by remember { mutableStateOf<Set<String>>(emptySet()) }
    fun toggleType(title: String) {
        activeTypes = if (title in activeTypes) activeTypes - title else activeTypes + title
    }

    // POI seleccionado para trazar línea recta desde tu ubicación
    var selectedPoi by remember { mutableStateOf<Poi?>(null) }

// Si apagas un tipo en la leyenda y el POI seleccionado pertenece a ese tipo, limpia la selección
    LaunchedEffect(activeTypes) {
        if (selectedPoi?.type?.title !in activeTypes) selectedPoi = null
    }

    // ====== UI ======
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
                        selectedPoi = null
                        destination = latLng
                        // calcular ruta si hay origen
                        val origin = currentLatLng
                        if (origin != null && webApiKey.isNotBlank()) {
                            recomputeRoute(origin, latLng)
                        } else {
                            routePoints = if (origin != null) listOf(origin, latLng) else emptyList()
                        }
                    },
                    onMapLongClick = {
                        selectedPoi = null
                        //Limpia destino y ruta con un long-press
                        destination = null
                        routePoints = emptyList()
                    },
                ) {
                    // Polyline de la ruta
                    if (routePoints.size >= 2) {
                        Polyline(
                            points = routePoints,
                            width = 12f, // más gruesa
                            color = Color(0xFF0D47A1), // azul fuerte
                            geodesic = true
                        )
                    }

                    defaultPois
                        .filter { poi -> poi.type.title in activeTypes }
                        .forEach { poi ->
                            val markerState = remember(poi) { MarkerState(position = poi.position) }

                            Marker(
                                state = markerState,
                                // ⚠️ Evita InfoWindow por ahora (algunas builds crashean con title/snippet)
                                // title = poi.label ?: poi.type.title,
                                // snippet = poi.type.title,
                                icon = BitmapDescriptorFactory.defaultMarker(poi.type.color.asHue()),
                                onClick = {
                                    // Solo seleccionamos si ya tenemos ubicación
                                    if (currentLatLng != null) {
                                        selectedPoi = poi
                                    }
                                    true // ✅ consumimos el evento → NO abre InfoWindow
                                }
                            )
                        }

                    val here = currentLatLng
                    val poiSel = selectedPoi
                    if (here != null && poiSel != null) {
                        Polyline(
                            points = listOf(here, poiSel.position),
                            width = 12f, // más gruesa
                            color = Color(0xFF0D47A1), // azul fuerte
                            geodesic = true
                        )
                    }

                    // Destino (rojo)
                    destination?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = "Destino",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                    }
                }

                // Overlay de brújula
                CompassOverlay(
                    azimuthDeg = azimuth,
                    autoRotate = autoRotate,
                    onToggleAutoRotate = { autoRotate = it },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 64.dp, end = 12.dp)
                )
            }

            LegendBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Blanco.copy(alpha = 0.95f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                items = poiLegend,
                selected = activeTypes,
                onToggle = ::toggleType
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

