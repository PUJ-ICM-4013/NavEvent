package com.example.naveventapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.example.naveventapp.ui.theme.Blanco

@Composable
fun CompassOverlay(
    azimuthDeg: Float,
    autoRotate: Boolean,
    onToggleAutoRotate: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Blanco.copy(alpha = 0.92f), shape = MaterialTheme.shapes.medium)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono tipo brújula (gira al revés para que la "punta" apunte al norte visualmente)
        Icon(
            imageVector = Icons.Filled.Explore,
            contentDescription = "Brújula",
            modifier = Modifier
                .size(24.dp)
                .rotate(-azimuthDeg)
        )
        Spacer(Modifier.width(10.dp))
        Text("${azimuthDeg.toInt()}°", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(12.dp))
        Text("Auto-rotar mapa", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(6.dp))
        Switch(checked = autoRotate, onCheckedChange = onToggleAutoRotate)
    }
}
