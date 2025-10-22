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

private enum class RouteKind { STRAIGHT, DIRECTIONS }
private enum class RouteSource { MAIN, POI }

@Composable
fun MapScreen(
    initialDestination: LatLng? = null,
    initialTitle: String? = null,
    onNavAgenda: () -> Unit = {},
    onNavQr: () -> Unit = {},
    onNavProfile: () -> Unit = {},
    onBellClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val webApiKey = remember { getMetaDataValue(context, "com.example.naveventapp.WEB_API_KEY") }

    // Estilo día/noche
    val isNight by isNightModeFlow(context).collectAsState(initial = false)
    val mapStyleOptions = remember(isNight) {
        try {
            MapStyleOptions.loadRawResourceStyle(
                context,
                if (isNight) R.raw.map_style_night_clean else R.raw.map_style_day_clean
            )
        } catch (_: Exception) { null }
    }

    // Ubicación & cámara
    val myLocationEnabled = rememberLocationPermission()
    val currentLatLng by produceState<LatLng?>(initialValue = null, key1 = myLocationEnabled) {
        if (myLocationEnabled) LocationTracker.locationFlow(context).collect { value = it }
        else value = null
    }

    var hasCenteredOnUser by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(4.5981, -74.0760), 16f)
    }
    LaunchedEffect(currentLatLng) {
        if (!hasCenteredOnUser) {
            currentLatLng?.let { here ->
                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(here, 17f))
                hasCenteredOnUser = true
            }
        }
    }

    val cameraMoveThresholdMeters = 15.0
    LaunchedEffect(currentLatLng) {
        val here = currentLatLng ?: return@LaunchedEffect
        val curTarget = cameraPositionState.position.target
        if (haversineMeters(curTarget, here) > cameraMoveThresholdMeters) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(here, cameraPositionState.position.zoom)
            )
        }
    }

    // Brújula
    var autoRotate by remember { mutableStateOf(false) }
    val azimuth by remember(context) { compassAzimuthFlow(context) }.collectAsState(initial = 0f)
    var lastAppliedBearing by remember { mutableStateOf<Float?>(null) }
    val bearingThresholdDeg = 5f
    LaunchedEffect(autoRotate, azimuth) {
        if (!autoRotate) return@LaunchedEffect
        val cur = cameraPositionState.position
        val curBearing = azimuth
        val last = lastAppliedBearing
        val delta = if (last == null) 360f else kotlin.math.abs(curBearing - last)
        if (last == null || delta >= bearingThresholdDeg) {
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition(cur.target, cur.zoom, cur.tilt, curBearing)
                ),
                180
            )
            lastAppliedBearing = curBearing
        }
    }

    // ── Anchor de POIs (no se mueven salvo > 2 km) ──
    val fallbackBogota = LatLng(4.5981, -74.0760)
    var lockedCenter by remember { mutableStateOf<LatLng?>(null) }
    val MOVE_THRESHOLD_METERS = 2000.0
    LaunchedEffect(currentLatLng) {
        val here = currentLatLng ?: return@LaunchedEffect
        val anchor = lockedCenter
        if (anchor == null) {
            lockedCenter = here
        } else if (haversineMeters(anchor, here) > MOVE_THRESHOLD_METERS) {
            lockedCenter = here
        }
    }

    // ── Ruta única + destino/target ──
    var routeKind by remember { mutableStateOf(RouteKind.STRAIGHT) }
    var routeSource by remember { mutableStateOf<RouteSource?>(null) }  // MAIN o POI
    var routeTarget by remember { mutableStateOf<LatLng?>(null) }       // endpoint actual (destino o POI)
    var destination by remember { mutableStateOf<LatLng?>(null) }       // solo para pintar marcador rojo cuando es MAIN
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) } // único polyline
    var routeJob by remember { mutableStateOf<Job?>(null) }
    val routeMode = "walking"

    fun setRouteStraight(origin: LatLng?, dest: LatLng, source: RouteSource, showRedMarker: Boolean) {
        routeJob?.cancel()
        routeKind = RouteKind.STRAIGHT
        routeSource = source
        routeTarget = dest
        routePoints = if (origin != null) listOf(origin, dest) else emptyList()
        destination = if (showRedMarker) dest else null
    }

    fun setRouteDirections(origin: LatLng?, dest: LatLng, source: RouteSource, showRedMarker: Boolean) {
        routeJob?.cancel()
        routeKind = RouteKind.DIRECTIONS
        routeSource = source
        routeTarget = dest
        if (origin == null) {
            routePoints = emptyList()
            destination = if (showRedMarker) dest else null
            return
        }
        if (webApiKey.isBlank()) {
            setRouteStraight(origin, dest, source, showRedMarker)
            return
        }
        destination = if (showRedMarker) dest else null
        routeJob = scope.launch {
            val res = DirectionsService.fetchRoute(
                origin = origin, destination = dest,
                apiKey = webApiKey, mode = routeMode, language = "es", region = "CO"
            )
            routePoints = if (res != null && res.points.size >= 2) res.points else listOf(origin, dest)
        }
    }

    // ── POIs anclados ──
    var activeTypes by remember { mutableStateOf<Set<String>>(emptySet()) }
    fun toggleType(title: String) { activeTypes = if (title in activeTypes) activeTypes - title else activeTypes + title }

    val poiAnchor: LatLng = lockedCenter ?: currentLatLng ?: fallbackBogota
    val defaultPois: List<Poi> = remember(poiAnchor) {
        listOf(
            Poi(poiLegend[0], SphericalUtil.computeOffset(poiAnchor,  60.0,  45.0), "Stand A"),
            Poi(poiLegend[0], SphericalUtil.computeOffset(poiAnchor,  90.0, 135.0), "Stand B"),
            Poi(poiLegend[0], SphericalUtil.computeOffset(poiAnchor, 120.0, 280.0), "Stand C"),
            Poi(poiLegend[1], SphericalUtil.computeOffset(poiAnchor, 110.0, 180.0), "Baño Norte"),
            Poi(poiLegend[2], SphericalUtil.computeOffset(poiAnchor,  70.0,   0.0), "Entrada Principal"),
            Poi(poiLegend[3], SphericalUtil.computeOffset(poiAnchor, 130.0, 260.0), "Restaurante 1"),
            Poi(poiLegend[4], SphericalUtil.computeOffset(poiAnchor,  90.0,  90.0), "Punto de Información")
        )
    }

    // Al cambiar la leyenda: si la ruta actual es hacia un POI oculto → borra polyline
    LaunchedEffect(activeTypes, defaultPois) {
        if (routeSource == RouteSource.POI && routeTarget != null) {
            val visiblePoiPositions = defaultPois
                .filter { it.type.title in activeTypes }
                .map { it.position }
                .toSet()
            if (routeTarget !in visiblePoiPositions) {
                routePoints = emptyList()
                routeTarget = null
                routeSource = null
                // destination se deja como está; solo se usa para rutas MAIN
            }
        }
    }

    // Ruta inicial desde Agenda (solo una vez)
    var initialRouteApplied by remember { mutableStateOf(false) }
    LaunchedEffect(initialDestination, currentLatLng) {
        if (!initialRouteApplied && initialDestination != null && currentLatLng != null) {
            setRouteStraight(currentLatLng, initialDestination, RouteSource.MAIN, showRedMarker = true)
            initialRouteApplied = true
        }
    }

    // ── “Recorte” dinámico con ubicación actual (aplica para MAIN y POI) ──
    val RECALC_DEVIATION_METERS = 35.0
    LaunchedEffect(currentLatLng) {
        val here = currentLatLng ?: return@LaunchedEffect
        val target = routeTarget ?: return@LaunchedEffect
        if (routePoints.size < 2) return@LaunchedEffect

        when (routeKind) {
            RouteKind.STRAIGHT -> {
                // Recta viva: orígen se mueve contigo
                routePoints = listOf(here, target)
            }
            RouteKind.DIRECTIONS -> {
                val trim = trimRouteToCurrent(routePoints, here)
                routePoints = trim.points
                if (trim.deviationMeters > RECALC_DEVIATION_METERS) {
                    setRouteDirections(
                        here, target,
                        source = (routeSource ?: RouteSource.MAIN),
                        showRedMarker = (routeSource == RouteSource.MAIN)
                    )
                }
            }
        }
    }

    // ── UI ──
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
                        // Reemplaza ruta actual → Directions viva (MAIN)
                        setRouteDirections(currentLatLng, latLng, RouteSource.MAIN, showRedMarker = true)
                    },
                    onMapLongClick = {
                        destination = null
                        routePoints = emptyList()
                        routeJob?.cancel()
                        routeKind = RouteKind.STRAIGHT
                        routeSource = null
                        routeTarget = null
                    },
                ) {
                    // Polyline único
                    if (routePoints.size >= 2) {
                        Polyline(
                            points = routePoints,
                            width = 12f,
                            color = Color(0xFF0D47A1),
                            geodesic = true,
                            zIndex = 1f
                        )
                    }

                    // POIs anclados
                    defaultPois
                        .filter { it.type.title in activeTypes }
                        .forEach { poi ->
                            Marker(
                                state = MarkerState(position = poi.position),
                                icon = BitmapDescriptorFactory.defaultMarker(poi.type.color.asHue()),
                                onClick = {
                                    // Recta viva al POI (sin marcador rojo) → reemplaza polyline
                                    setRouteStraight(currentLatLng, poi.position, RouteSource.POI, showRedMarker = false)
                                    true
                                }
                            )
                        }

                    // Marcador rojo (solo para rutas MAIN)
                    val dest = destination
                    val destState = remember(dest) { dest?.let { MarkerState(position = it) } }
                    if (destState != null) {
                        Marker(
                            state = destState,
                            title = initialTitle ?: "Destino",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                    }
                }

                // Brújula
                CompassOverlay(
                    azimuthDeg = azimuth,
                    autoRotate = autoRotate,
                    onToggleAutoRotate = { autoRotate = it },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 64.dp, end = 12.dp)
                )
            }

            // Leyenda
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
            onMap = { /* ya estás */ },
            onAgenda = onNavAgenda,
            onQr = onNavQr,
            onProfile = onNavProfile
        )
    }
}

/* ================== Helpers ================== */

private fun Color.asHue(): Float {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    return hsv[0]
}

private fun haversineMeters(a: LatLng, b: LatLng): Double {
    val R = 6371000.0
    val dLat = Math.toRadians(b.latitude - a.latitude)
    val dLon = Math.toRadians(b.longitude - a.longitude)
    val lat1 = Math.toRadians(a.latitude)
    val lat2 = Math.toRadians(b.latitude)
    val sinDLat = kotlin.math.sin(dLat / 2)
    val sinDLon = kotlin.math.sin(dLon / 2)
    val h = sinDLat * sinDLat + kotlin.math.cos(lat1) * kotlin.math.cos(lat2) * sinDLon * sinDLon
    return 2 * R * kotlin.math.asin(kotlin.math.sqrt(h))
}

private data class TrimResult(val points: List<LatLng>, val deviationMeters: Double)

/**
 * Recorta una polyline para que inicie en la ubicación actual:
 * - Busca el segmento más cercano a 'here'
 * - Descarta puntos anteriores
 * - Inserta 'here' como nuevo primer punto
 * Retorna también cuánto te desviaste de la ruta (para posible recalculo).
 */
private fun trimRouteToCurrent(route: List<LatLng>, here: LatLng): TrimResult {
    if (route.size < 2) return TrimResult(route, 0.0)

    var bestIdx = 0
    var bestDev = Double.MAX_VALUE

    for (i in 0 until route.size - 1) {
        val a = route[i]
        val b = route[i + 1]
        val dev = pointToSegmentDistanceMeters(here, a, b)
        if (dev < bestDev) {
            bestDev = dev
            bestIdx = i
        }
    }

    val trimmed = ArrayList<LatLng>(route.size - bestIdx)
    trimmed.add(here)
    for (j in (bestIdx + 1) until route.size) trimmed.add(route[j])

    return TrimResult(trimmed, bestDev)
}

/** Distancia punto–segmento aproximada en metros (proyección local 2D). */
private fun pointToSegmentDistanceMeters(p: LatLng, a: LatLng, b: LatLng): Double {
    val ax = lonToMeters(a.longitude, a.latitude)
    val ay = latToMeters(a.latitude)
    val bx = lonToMeters(b.longitude, a.latitude)
    val by = latToMeters(b.latitude)
    val px = lonToMeters(p.longitude, a.latitude)
    val py = latToMeters(p.latitude)

    val abx = bx - ax
    val aby = by - ay
    val apx = px - ax
    val apy = py - ay
    val ab2 = abx * abx + aby * aby
    val t = if (ab2 == 0.0) 0.0 else ((apx * abx + apy * aby) / ab2).coerceIn(0.0, 1.0)
    val cx = ax + t * abx
    val cy = ay + t * aby
    val dx = px - cx
    val dy = py - cy
    return kotlin.math.sqrt(dx * dx + dy * dy)
}

private fun latToMeters(lat: Double): Double = lat * 111_320.0
private fun lonToMeters(lon: Double, atLat: Double): Double =
    lon * (111_320.0 * kotlin.math.cos(Math.toRadians(atLat)))

