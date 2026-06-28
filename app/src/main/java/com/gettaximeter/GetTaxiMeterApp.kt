package com.gettaximeter

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.gettaximeter.data.db.AppDatabase

class GetTaxiMeterApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "taxi_service_channel",
                "Taxi Service Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Used for live trip meter services and trip assignment notifications"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
