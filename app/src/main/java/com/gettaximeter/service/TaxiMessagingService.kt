package com.gettaximeter.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.gettaximeter.MainActivity

class TaxiMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("TaxiMessagingService", "FCM Device Token: $token")
        // In fully deployed systems, push this token to the drivers/{driverId}/fcmToken Firestore node
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("TaxiMessagingService", "Received inbound FCM payload: ${remoteMessage.data}")

        val title = remoteMessage.data["title"] ?: "New Trip Assigned"
        val message = remoteMessage.data["message"] ?: "Open Get Taxi Meter to view active booking details."
        val tripId = remoteMessage.data["tripId"]

        sendAssignmentNotification(title, message, tripId)
    }

    private fun sendAssignmentNotification(title: String, message: String, tripId: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NOTIFICATION_TRIP_ID", tripId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            1002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "taxi_service_channel"
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2002, notification)
    }
}
