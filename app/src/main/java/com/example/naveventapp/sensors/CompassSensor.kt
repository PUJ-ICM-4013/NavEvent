package com.example.naveventapp.sensors

import android.content.Context
import android.hardware.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Flow que emite el rumbo (azimuth) en grados [0, 360).
 * Usa Rotation Vector si existe; si no, combina Acelerómetro/Magnetómetro.
 */
fun compassAzimuthFlow(context: Context) = callbackFlow<Float> {
    val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Preferimos rotation vector (más estable)
    val rotVector = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    // Fallback: acelerómetro + magnetómetro
    val accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val magnet = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    val R = FloatArray(9)
    val I = FloatArray(9)
    val orientation = FloatArray(3)

    val rotMat = FloatArray(9)
    val rotVals = FloatArray(3)

    var accelVals: FloatArray? = null
    var magnetVals: FloatArray? = null

    val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ROTATION_VECTOR -> {
                    SensorManager.getRotationMatrixFromVector(rotMat, event.values)
                    SensorManager.getOrientation(rotMat, rotVals)
                    val az = Math.toDegrees(rotVals[0].toDouble()).toFloat()
                    trySend(normalizeDeg(az))
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    accelVals = event.values.clone()
                    if (magnetVals != null) {
                        if (SensorManager.getRotationMatrix(R, I, accelVals, magnetVals)) {
                            SensorManager.getOrientation(R, orientation)
                            val az = Math.toDegrees(orientation[0].toDouble()).toFloat()
                            trySend(normalizeDeg(az))
                        }
                    }
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    magnetVals = event.values.clone()
                    if (accelVals != null) {
                        if (SensorManager.getRotationMatrix(R, I, accelVals, magnetVals)) {
                            SensorManager.getOrientation(R, orientation)
                            val az = Math.toDegrees(orientation[0].toDouble()).toFloat()
                            trySend(normalizeDeg(az))
                        }
                    }
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    // Registro
    if (rotVector != null) {
        sm.registerListener(listener, rotVector, SensorManager.SENSOR_DELAY_GAME)
    } else {
        // Fallback
        if (accel != null) sm.registerListener(listener, accel, SensorManager.SENSOR_DELAY_GAME)
        if (magnet != null) sm.registerListener(listener, magnet, SensorManager.SENSOR_DELAY_GAME)
    }

    awaitClose { sm.unregisterListener(listener) }
}
    // Smoothing y eliminación de "saltos" pequeños opcional:
    .map { it % 360f }
    .map { if (it < 0) it + 360f else it }
    .map(::round1)
    .distinctUntilChanged()

private fun normalizeDeg(d: Float): Float {
    var v = d
    while (v < 0f) v += 360f
    while (v >= 360f) v -= 360f
    return v
}
private fun round1(v: Float) = (v * 10f).toInt() / 10f.toFloat()
