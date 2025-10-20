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
            }
        }
    }
}

