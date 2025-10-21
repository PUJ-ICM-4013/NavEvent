package com.example.naveventapp.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.util.Log

object TemperatureMonitor : SensorEventListener {

    private const val TAG = "TEMP_MON"

    private const val HOT_THRESHOLD  = 25f   // subiendo → notifica “calor”
    private const val COOL_THRESHOLD = 24f   // bajando → notifica “fresco” (histéresis)

    private val COOLDOWN_HOT_MS  = 0L        // súbelo si quieres rate-limit
    private val COOLDOWN_COOL_MS = 0L
    private val MIN_DELTA_FOR_UPDATE = 0.2f   // filtra ruido pequeño

    private lateinit var appCtx: Context
    private var sm: SensorManager? = null
    private var sensor: Sensor? = null
    private var lastTemp: Float? = null
    private var lastHotNotifyAt = 0L
    private var lastCoolNotifyAt = 0L
    private var started = false

    // estado de “ya estaba caliente”
    private var wasHot = false

    fun init(ctx: Context) {
        if (::appCtx.isInitialized) return
        appCtx = ctx.applicationContext
        sm = appCtx.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // TYPE_AMBIENT_TEMPERATURE primero; si no existe, intenta con TYPE_TEMPERATURE (antiguo)
        sensor = sm?.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
            ?: sm?.getDefaultSensor(Sensor.TYPE_TEMPERATURE)

        if (sensor == null) {
            Log.e(TAG, "No hay sensor de temperatura disponible en este dispositivo/emulador.")
        } else {
            Log.d(TAG, "Sensor seleccionado: ${sensor?.name}")
        }
    }

    fun start() {
        if (!::appCtx.isInitialized) {
            Log.w(TAG, "Llama a init(context) antes de start()")
            return
        }
        if (started) return
        val s = sensor ?: run {
            Log.e(TAG, "Sin sensor, no se puede registrar listener.")
            return
        }
        Log.d(TAG, "Registrando listener de temperatura…")
        sm?.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL)
        started = true
    }

    fun stop() {
        if (!started) return
        Log.d(TAG, "Desregistrando listener de temperatura…")
        sm?.unregisterListener(this)
        started = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val t = event?.values?.firstOrNull() ?: return
        val prev = lastTemp
        lastTemp = t
        Log.d(TAG, "Lectura: $t °C (wasHot=$wasHot)")

        // filtra cambios muy pequeños
        if (prev != null && kotlin.math.abs(prev - t) < MIN_DELTA_FOR_UPDATE) return

        val now = SystemClock.elapsedRealtime()

        // si estaba caliente y bajó bajo el umbral “frío” → notifica fresco y resetea
        if (wasHot && t <= COOL_THRESHOLD) {
            if (now - lastCoolNotifyAt >= COOLDOWN_COOL_MS) {
                HeatNotificationHelper.showCoolAlert(appCtx, t)
                lastCoolNotifyAt = now
                Log.d(TAG, "🔵 Notificado ‘fresco’.")
            }
            wasHot = false
            return
        }

        // si NO estaba caliente y subió por encima del umbral “caliente” → notifica calor
        if (!wasHot && t >= HOT_THRESHOLD) {
            if (now - lastHotNotifyAt >= COOLDOWN_HOT_MS) {
                HeatNotificationHelper.showHotAlert(appCtx, t)
                lastHotNotifyAt = now
                wasHot = true
                Log.d(TAG, "🔴 Notificado ‘calor’.")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
