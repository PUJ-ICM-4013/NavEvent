package com.example.naveventapp.sensors

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.naveventapp.R
import com.example.naveventapp.MainActivity

object HeatNotificationHelper {
    const val CHANNEL_ID = "heat_alerts"
    const val CHANNEL_NAME = "Alertas de calor"
    const val NOTIF_ID = 1001

    fun createChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // 👈 heads-up
            ).apply {
                description = "Notificaciones cuando la temperatura supera el umbral"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    fun showHotAlert(ctx: Context, celsius: Float) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(ctx, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val flags = if (Build.VERSION.SDK_INT >= 23)
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        else PendingIntent.FLAG_UPDATE_CURRENT

        val pi = PendingIntent.getActivity(ctx, 0, intent, flags)

        val text = "Hace ${"%.1f".format(celsius)}°C. Mejor evita salir al aire libre."

        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_thermo)
            .setContentTitle("Temperatura alta")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)              // 👈 heads-up
            .setCategory(NotificationCompat.CATEGORY_ALARM)             // 👈 ayuda a heads-up
            .setDefaults(NotificationCompat.DEFAULT_ALL)                // sonido/vibración/led
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)                                    // 👈 re-alertar si re-publicas
            .build()

        nm.notify(NOTIF_ID, notif) // mismo ID = reemplaza/relanza
    }

    fun showCoolAlert(ctx: Context, celsius: Float) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(ctx, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val flags = if (Build.VERSION.SDK_INT >= 23)
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        else PendingIntent.FLAG_UPDATE_CURRENT

        val pi = PendingIntent.getActivity(ctx, 0, intent, flags)

        val text =
            "Ahora hace ${"%.1f".format(celsius)}°C. Ya está fresco, puedes seguir explorando."

        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_thermo)
            .setContentTitle("Temperatura normal")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)            // no hace falta heads-up aquí
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .build()

        // Usa otro ID si quieres que se vean ambas a la vez; si prefieres reemplazar, usa NOTIF_ID
        nm.notify(NOTIF_ID + 1, notif)
    }
}
