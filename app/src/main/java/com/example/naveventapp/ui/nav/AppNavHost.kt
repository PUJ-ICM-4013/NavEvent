package com.example.naveventapp.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.naveventapp.ui.screens.*

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = AppRoute.Home.route,
        modifier = modifier
    ) {
        composable(AppRoute.Home.route) {
            HomeScreen(
                onLoginClick = { nav.navigate(AppRoute.Login.route) },
                onCreateAccountClick = { nav.navigate(AppRoute.SignIn.route) }
            )
        }

        composable(AppRoute.Login.route) {
            LogInScreen(
                onLogin = { _, _, _ -> nav.navigate(AppRoute.Map.route) },
                onCreateAccountClick = { nav.navigate(AppRoute.SignIn.route) }
            )
        }

        composable(AppRoute.SignIn.route) {
            SignInScreen(
                onRegister = { _,_,_,_,_,_ -> nav.navigate(AppRoute.Login.route) },
                onTermsClick = { }
            )
        }

        composable(AppRoute.Map.route) {
            MapScreen(
                onNavAgenda = { nav.navigate(AppRoute.Agenda.route) },
                onNavQr = { nav.navigate(AppRoute.Qr.route) },
                onNavProfile = { nav.navigate(AppRoute.Profile.route) },
                onBellClick = { nav.navigate(AppRoute.Notifications.route) }
            )
        }

        composable(AppRoute.Qr.route) {
            QrScreen(
                qrContent = """{"evento":"FeriaBogotá2025","stand":"InnovaciónGastronómica","zona":"B"}""",
                onNavMap = { nav.navigate(AppRoute.Map.route) },         // 👈 ahora sí existe
                onNavAgenda = { nav.navigate(AppRoute.Agenda.route) },
                onNavProfile = { nav.navigate(AppRoute.Profile.route) },
                onBellClick   = { nav.navigate(AppRoute.Notifications.route) }
            )
        }

        composable(AppRoute.Agenda.route) {
            AgendaScreen(
                onNavMap = { nav.navigate(AppRoute.Map.route) },
                onNavQr = { nav.navigate(AppRoute.Qr.route) },
                onNavProfile = { nav.navigate(AppRoute.Profile.route) },
                onBellClick = { nav.navigate(AppRoute.Notifications.route) }
            )
        }

        composable(AppRoute.Profile.route) {
            ProfileScreen(
                onNavMap = { nav.navigate(AppRoute.Map.route) },
                onNavAgenda = { nav.navigate(AppRoute.Agenda.route) },
                onNavQr = { nav.navigate(AppRoute.Qr.route) },
                onBellClick = { nav.navigate(AppRoute.Notifications.route) },
                onSave = { email, phone -> /* guardar cambios */ }
            )
        }

        composable(AppRoute.Notifications.route) {
            NotificationScreen(
                onNavMap = { nav.navigate(AppRoute.Map.route) },
                onNavAgenda = { nav.navigate(AppRoute.Agenda.route) },
                onNavQr = { nav.navigate(AppRoute.Qr.route) },
                onNavProfile = { nav.navigate(AppRoute.Profile.route) },
                onBellClick = { /* ya estás aquí */ }
            )
        }
    }
}



