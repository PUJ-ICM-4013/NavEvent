package com.example.naveventapp.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.naveventapp.ui.screens.*
import com.google.android.gms.maps.model.LatLng

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
                onLoginSuccess = { uid ->
                    // navega donde quieras tras login OK
                    nav.navigate(AppRoute.Map.route) {
                        popUpTo(AppRoute.Login.route) { inclusive = true } // opcional: sacar login del back stack
                    }
                },
                onCreateAccountClick = { nav.navigate(AppRoute.SignIn.route) }
            )
        }

        composable(AppRoute.SignIn.route) {
            SignInScreen(
                onRegister = { _,_,_,_,_,_ -> nav.navigate(AppRoute.Login.route) },
                onTermsClick = { }
            )
        }

        // Map con args opcionales
        composable(
            route = "map?lat={lat}&lng={lng}&title={title}",
            arguments = listOf(
                navArgument("lat")   { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("lng")   { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("title") { type = NavType.StringType; nullable = true; defaultValue = null },
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
            val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull()
            val title = backStackEntry.arguments?.getString("title")

            MapScreen(
                initialDestination = if (lat != null && lng != null) LatLng(lat, lng) else null,
                initialTitle = title,
                onNavAgenda = { nav.navigate(AppRoute.Agenda.route) },
                onNavQr = { nav.navigate(AppRoute.Qr.route) },
                onNavProfile = { nav.navigate(AppRoute.Profile.route) },
                onBellClick = { nav.navigate(AppRoute.Notifications.route) }
            )
        }

        composable(AppRoute.Qr.route) {
            QrScreen(
                qrContent = """{"evento":"FeriaBogotá2025","stand":"InnovaciónGastronómica","zona":"B"}""",
                onNavMap = { nav.navigate(AppRoute.Map.route) },
                onNavAgenda = { nav.navigate(AppRoute.Agenda.route) },
                onNavProfile = { nav.navigate(AppRoute.Profile.route) },
                onBellClick   = { nav.navigate(AppRoute.Notifications.route) }
            )
        }

        composable(AppRoute.Agenda.route) {
            AgendaScreen(
                onOpenMapTo = { latLng, title ->
                    nav.navigate(AppRoute.mapWith(latLng.latitude, latLng.longitude, title))
                },
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
                onSave = { _, _ -> }
            )
        }

        composable(AppRoute.Notifications.route) {
            NotificationScreen(
                onNavMap = { nav.navigate(AppRoute.Map.route) },
                onNavAgenda = { nav.navigate(AppRoute.Agenda.route) },
                onNavQr = { nav.navigate(AppRoute.Qr.route) },
                onNavProfile = { nav.navigate(AppRoute.Profile.route) },
                onBellClick = { /* ya aquí */ }
            )
        }
    }
}

@Composable
fun MapScreen(
    onNavAgenda: () -> Unit,
    onNavQr: () -> Unit,
    onNavProfile: () -> Unit,
    onBellClick: () -> Unit
) {
    TODO("Not yet implemented")
}

