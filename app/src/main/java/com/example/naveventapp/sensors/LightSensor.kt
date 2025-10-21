package com.example.naveventapp.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map

fun lightLuxFlow(context: Context) = callbackFlow<Float> {
    val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val sensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT)
    val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            trySend(event.values.firstOrNull() ?: 0f)
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
    sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    awaitClose { sm.unregisterListener(listener) }
}

fun isNightModeFlow(context: Context) =
    lightLuxFlow(context).map { lux -> lux < 20f } // umbral simple