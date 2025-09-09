package com.example.navevent1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*

import com.example.navevent1.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "login"
            ) {
                //pantalla de login inicial
                composable("login") {
                    LoginScreen(navController)
                }
                //pantalla principal de navegacion inferior
                composable("main") {
                    MainScaffold()
                }
            }
        }
    }
}

@Composable
fun MainScaffold() {
    val innerNavController = rememberNavController()

    Scaffold(
        //barra inferior para navegar entre pantallas
        bottomBar = {
            BottomNavigationBar(innerNavController)
        }
    ) { padding ->
        NavHost(
            navController = innerNavController,
            startDestination = "mapa",
            modifier = Modifier.padding(padding)
        ) {
            composable("mapa") { MapaScreen() }
            composable("agenda") { AgendaScreen() }
            composable("qr") { QRScreen() }
            composable("notificaciones") { NotificacionesScreen() }
            composable("perfil") { PerfilScreen() }
            composable("organizador") { OrganizadorScreen() }
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navItems = listOf(
        "mapa", "agenda", "qr", "notificaciones", "perfil", "organizador"
    )

    NavigationBar {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

        navItems.forEach { route ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = { navController.navigate(route) },
                icon = { Text("🔹") }, // Placeholder visual
                label = { Text(route.replaceFirstChar { it.uppercase() }) }
            )
        }
    }
}
