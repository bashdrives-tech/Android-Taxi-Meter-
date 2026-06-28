package com.gettaximeter.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.gettaximeter.GetTaxiMeterApp
import com.gettaximeter.data.model.TripStatus
import com.gettaximeter.data.repository.TaxiRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationTrackingService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var repository: TaxiRepository
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private var activeTripId: String? = null
    private var lastLocation: Location? = null
    private var totalDistanceKm = 0.0
    private var totalWaitingMinutes = 0.0
    private var lastUpdateTimeMs = 0L

    companion object {
        const val ACTION_START_TRACKING = "ACTION_START_TRACKING"
        const val ACTION_STOP_TRACKING = "ACTION_STOP_TRACKING"
        const val EXTRA_TRIP_ID = "EXTRA_TRIP_ID"
        const val NOTIFICATION_ID = 4001
    }

    override fun onCreate() {
        super.onCreate()
        repository = TaxiRepository(applicationContext)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        lastUpdateTimeMs = System.currentTimeMillis()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                
                // Security check: Prevent mock/spoofed GPS coordinates
                val isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    location.isMock
                } else {
                    @Suppress("DEPRECATION")
                    location.isFromMockProvider
                }

                if (isMock) {
                    Log.w("LocationService", "Mock/spoofed GPS location detected (allowed for development/sandbox testing).")
                }

                handleNewLocation(location)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val tripId = intent?.getStringExtra(EXTRA_TRIP_ID)

        if (action == ACTION_START_TRACKING && tripId != null) {
            activeTripId = tripId
            resetTrackingMetrics()
            startForegroundServiceNotification()
            requestLocationUpdates()
        } else if (action == ACTION_STOP_TRACKING) {
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun resetTrackingMetrics() {
        lastLocation = null
        totalDistanceKm = 0.0
        totalWaitingMinutes = 0.0
        lastUpdateTimeMs = System.currentTimeMillis()
    }

    private fun startForegroundServiceNotification() {
        val channelId = "taxi_service_channel"
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Taxi Meter Active")
            .setContentText("Tracking current trip route and calculating meter charges live.")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(3000L)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (unlikely: SecurityException) {
            Log.e("LocationService", "No location permissions configured for active tracking service", unlikely)
        }
    }

    private fun handleNewLocation(location: Location) {
        val tripId = activeTripId ?: return
        val now = System.currentTimeMillis()
        val durationSec = (now - lastUpdateTimeMs) / 1000.0
        lastUpdateTimeMs = now

        // Calculate Distance incremental change
        lastLocation?.let { prevLoc ->
            // Filter out minor GPS drift
            if (location.accuracy < 30.0) {
                val distanceMeters = prevLoc.distanceTo(location)
                if (distanceMeters > 2.0 && distanceMeters < 150.0) { // filter anomalous jumps
                    totalDistanceKm += (distanceMeters / 1000.0)
                }
            }
        }

        // Calculate Speed & Waiting Timer (speed < 5 km/h is roughly 1.38 m/s)
        val speedKmh = (location.speed * 3600.0) / 1000.0
        if (speedKmh < 5.0) {
            totalWaitingMinutes += (durationSec / 60.0)
        }

        lastLocation = location

        // Update database and repository metrics asynchronously
        serviceScope.launch {
            repository.addGPSLog(
                tripId = tripId,
                lat = location.latitude,
                lng = location.longitude,
                speed = location.speed,
                isMock = false
            )

            // Update live trip values
            repository.updateLiveMeter(
                tripId = tripId,
                currentKm = String.format("%.2f", totalDistanceKm).toDouble(),
                waitingMinutes = String.format("%.1f", totalWaitingMinutes).toDouble(),
                isHillActive = false, // defaults
                isNightActive = false
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
