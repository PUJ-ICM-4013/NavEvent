package com.example.naveventapp.ui.location

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.naveventapp.ui.theme.GrisOscuro

// Tipos de POI (config centralizada)
data class PoiType(
    val title: String,
    val color: Color,
    val icon: ImageVector
)

// Leyenda por defecto (puedes editar colores/íconos aquí)
val poiLegend = listOf(
    PoiType("Stands",         Color(0xFF7E57C2), Icons.Filled.LocalMall),
    PoiType("Baños",          Color(0xFF26A69A), Icons.Filled.Wc),          // reemplaza si quieres otro
    PoiType("Entrada/Salida", Color(0xFFFF7043), Icons.Filled.DoorFront),
    PoiType("Restaurantes",   Color(0xFFFFC107), Icons.Filled.Fastfood),
    PoiType("Info",           Color(0xFF42A5F5), Icons.Filled.Info),
)

// Componente de leyenda reutilizable
@Composable
fun LegendBar(
    modifier: Modifier = Modifier,
    items: List<PoiType> = poiLegend,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEach { poi ->
            AssistChip(
                onClick = { /* futuro: filtrar/centrar */ },
                label = { Text(poi.title, color = GrisOscuro) },
                leadingIcon = {
                    Icon(
                        imageVector = poi.icon,
                        contentDescription = poi.title,
                        tint = poi.color
                    )
                },
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = poi.color,
                    borderWidth = 1.5.dp
                ),
                modifier = Modifier.padding(end = 0.dp)
            )
        }
    }
}
