package com.example.projekat

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
class ForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "MyForegroundServiceChannel"
        const val START_NOTIFICATION_ID = 1
        const val STOP_NOTIFICATION_ID = 2
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sendStartNotification()

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText("Service is running")
            .setSmallIcon(R.drawable.ic_notification) // Ensure you have this drawable resource
            .build()

        startForeground(START_NOTIFICATION_ID, notification)

        // Implement your service logic here

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sendStopNotification()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun sendStartNotification() {
        val startNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Service Started")
            .setContentText("The service has started successfully.")
            .setSmallIcon(R.drawable.ic_notification) // Ensure you have this drawable resource
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(START_NOTIFICATION_ID, startNotification)
    }

    private fun sendStopNotification() {
        val stopNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Service Stopped")
            .setContentText("The service has stopped.")
            .setSmallIcon(R.drawable.ic_notification) // Ensure you have this drawable resource
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(STOP_NOTIFICATION_ID, stopNotification)
    }
}
