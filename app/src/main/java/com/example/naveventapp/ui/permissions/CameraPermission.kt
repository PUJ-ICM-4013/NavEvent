package com.example.naveventapp.ui.permissions

import android.Manifest
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*

@Composable
fun rememberCameraPermission(): Pair<Boolean, () -> Unit> {
    var hasPermission by remember { mutableStateOf(false) }

    // Launcher que abre la app de cámara del dispositivo (Intent implícito)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Puedes manejar el resultado si deseas guardar o mostrar la foto
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            // Aquí podrías acceder a result.data?.extras?.get("data") -> miniatura de la foto
        }
    }

    // Launcher que pide el permiso
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            // Si el permiso fue concedido, abrimos la cámara del sistema
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        }
    }

    // Función combinada
    val requestOrOpenCamera: () -> Unit = {
        if (hasPermission) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    return Pair(hasPermission, requestOrOpenCamera)
}
