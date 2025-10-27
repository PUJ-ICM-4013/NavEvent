package com.example.naveventapp.ui.location

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.naveventapp.ui.theme.GrisOscuro

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)


fun LegendBar(
    modifier: Modifier = Modifier,
    items: List<PoiType> = poiLegend,
    selected: Set<String> = items.map { it.title }.toSet(),
    onToggle: (String) -> Unit = {}
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEach { poi ->
            val isOn = poi.title in selected
            AssistChip(
                onClick = { onToggle(poi.title) },
                label = { Text(poi.title, color = GrisOscuro) },
                leadingIcon = {
                    Icon(
                        imageVector = poi.icon,
                        contentDescription = poi.title,
                        tint = if (isOn) poi.color else Color.LightGray
                    )
                },
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = if (isOn) poi.color else Color.LightGray,
                    borderWidth = 1.5.dp
                ),
                modifier = Modifier.padding(end = 0.dp)
            )
        }
    }
}
