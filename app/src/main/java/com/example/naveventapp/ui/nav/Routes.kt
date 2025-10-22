package com.example.naveventapp.ui.nav

import android.net.Uri

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object Login : AppRoute("login")
    data object SignIn : AppRoute("sign_in")
    data object Map : AppRoute("map")
    data object Agenda : AppRoute("agenda")
    data object Qr : AppRoute("qr")
    data object Profile : AppRoute("profile")
    data object Notifications : AppRoute("notifications")

    companion object {
        fun mapWith(lat: Double, lng: Double, title: String? = null): String {
            val t = title?.let { "&title=${Uri.encode(it)}" } ?: ""
            return "map?lat=$lat&lng=$lng$t"
        }
    }
}
