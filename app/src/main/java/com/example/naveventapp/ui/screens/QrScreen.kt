package com.example.naveventapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import com.example.naveventapp.ui.components.BottomBar
import com.example.naveventapp.ui.permissions.rememberCameraPermission
import com.example.naveventapp.ui.theme.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun QrScreen(
    qrContent: String = """{"evento":"FeriaBogotá2025","stand":"InnovaciónGastronómica","zona":"B"}""",   // simular QR
    onNavMap: () -> Unit = {},
    onNavAgenda: () -> Unit = {},
    onNavProfile: () -> Unit = {},
    onBellClick: () -> Unit = {},
) {
    // Generar el QR una vez para el contenido dado
    val qrBitmap: ImageBitmap? by remember(qrContent) {
        mutableStateOf(runCatching { generateQrImage(qrContent, 720) }.getOrNull())
    }

    val (hasCameraPermission, requestCameraPermission) = rememberCameraPermission()

    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp) // margen lateral
                .padding(bottom = 72.dp),    // deja espacio para el BottomBar
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))

            // Título
            Text(
                text = "QR Stands",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(Modifier.height(16.dp))

            // Imagen QR
            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap!!,
                    contentDescription = "Tu código QR",
                    modifier = Modifier
                        .size(400.dp)
                        .background(Blanco, RoundedCornerShape(12.dp))
                        .padding(8.dp)
                )
            } else {
                // fallback simple si fallara la generación
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .background(Blanco, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("QR no disponible", color = GrisOscuro)
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Tu codigo QR",
                style = MaterialTheme.typography.bodyMedium,
                color = androidx.compose.ui.graphics.Color.Black
            )

            Spacer(Modifier.height(30.dp))

            // Botón "Escanear QR"
            OutlinedButton(
                onClick = requestCameraPermission, // Se pide permiso y luego se abre la cámara del sistema
                border = BorderStroke(2.dp, Vinotinto),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Vinotinto
                ),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(50.dp)
            ) {
                Text(
                    "Escanear QR",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Campana (notificaciones)
        IconButton(
            onClick = onBellClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 12.dp)
                .background(Blanco.copy(alpha = 0.9f), shape = MaterialTheme.shapes.small)
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "Notificaciones",
                tint = Vinotinto
            )
        }

        // Bottom bar (reutilizamos el que ya tienes)
        BottomBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            onMap = onNavMap,
            onAgenda = onNavAgenda,
            onQr = { /* ya estás en el Qr */ },
            onProfile = onNavProfile
        )

    }
}

/** Utilidad para generar un ImageBitmap de un QR con ZXing */
private fun generateQrImage(content: String, sizePx: Int): ImageBitmap {
    val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap.asImageBitmap()
}
