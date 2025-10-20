package com.example.naveventapp.ui.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object LocationTracker {

    /**
     * Flujo de ubicaciones en tiempo real (cada ~2s).
     * IMPORTANTE: Debes tener concedido ACCESS_FINE_LOCATION.
     */
    @SuppressLint("MissingPermission")
    fun locationFlow(context: Context): Flow<LatLng> = callbackFlow {
        val client = LocationServices.getFusedLocationProviderClient(context)

        // Última ubicación rápida (si está disponible)
        client.lastLocation.addOnSuccessListener { loc: Location? ->
            loc?.let { trySend(LatLng(it.latitude, it.longitude)) }
        }

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, /* intervalMs = */ 2000L
        )
            .setMinUpdateIntervalMillis(1500L)
            .setWaitForAccurateLocation(true)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    trySend(LatLng(loc.latitude, loc.longitude))
                }
            }
        }

        // Para LocationCallback se usa un Looper, no un Executor
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())

        awaitClose {
            client.removeLocationUpdates(callback)
        }
    }
}

