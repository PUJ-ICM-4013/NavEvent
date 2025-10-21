package com.example.naveventapp

import android.app.Application
import com.example.naveventapp.sensors.HeatNotificationHelper
import com.example.naveventapp.sensors.TemperatureMonitor

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        HeatNotificationHelper.createChannel(this)
        // Comienza a escuchar el sensor
        TemperatureMonitor.init(this)
        TemperatureMonitor.start()
    }
}
