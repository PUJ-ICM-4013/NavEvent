package com.example.naveventapp.ui.permissions

import android.Manifest
import androidx.compose.runtime.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Verifica y solicita permiso de ubicación.
 * Retorna true si el permiso fue otorgado.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberLocationPermission(): Boolean {
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Solicita el permiso automáticamente la primera vez
    LaunchedEffect(Unit) {
        if (permissionState.status !is PermissionStatus.Granted) {
            permissionState.launchPermissionRequest()
        }
    }

    return permissionState.status is PermissionStatus.Granted
}
