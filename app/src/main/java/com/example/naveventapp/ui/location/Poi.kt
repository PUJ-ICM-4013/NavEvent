package com.example.naveventapp.ui.location

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Wc
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.android.gms.maps.model.LatLng

// Tipo de POI para la leyenda y para agrupar puntos
data class PoiType(
    val title: String,
    val color: Color,
    val icon: ImageVector
)

// Lista base de tipos (la usan la leyenda y el mapa)
val poiLegend: List<PoiType> = listOf(
    PoiType("Stands",         Color(0xFF7E57C2), Icons.Filled.LocalMall),
    PoiType("Baños",          Color(0xFF26A69A), Icons.Filled.Wc),
    PoiType("Entrada/Salida", Color(0xFFFF7043), Icons.Filled.DoorFront),
    PoiType("Restaurantes",   Color(0xFFFFC107), Icons.Filled.Fastfood),
    PoiType("Info",           Color(0xFF42A5F5), Icons.Filled.Info),
)

// POI concreto con posición
data class Poi(
    val type: PoiType,
    val position: LatLng,
    val label: String? = null
)



