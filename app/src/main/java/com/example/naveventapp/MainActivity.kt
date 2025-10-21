// MainActivity.kt
package com.example.naveventapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.naveventapp.ui.nav.AppNavHost
import com.example.naveventapp.ui.theme.NavEventTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NavEventTheme {
                AppNavHost()
                if (android.os.Build.VERSION.SDK_INT >= 33) {
                    val pm = androidx.core.app.NotificationManagerCompat.from(this)
                    val has = pm.areNotificationsEnabled()
                    if (!has) {
                        // Usa Activity Result API o Accompanist Permissions para pedir POST_NOTIFICATIONS
                        // Ejemplo simple con ActivityCompat:
                        androidx.core.app.ActivityCompat.requestPermissions(
                            this,
                            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                            1001
                        )
                    }
                }
            }
        }
    }
}

