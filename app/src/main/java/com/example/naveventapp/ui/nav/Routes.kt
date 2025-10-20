package com.example.naveventapp.ui.nav

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object Login : AppRoute("login")
    data object SignIn : AppRoute("sign_in")
    data object Map : AppRoute("map")
    data object Agenda : AppRoute("agenda")
    data object Qr : AppRoute("qr")
    data object Profile : AppRoute("profile")
    data object Notifications : AppRoute("notifications")

}
