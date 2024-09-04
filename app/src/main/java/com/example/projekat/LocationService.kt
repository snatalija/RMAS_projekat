package com.example.projekat

import android.Manifest
import android.annotation.SuppressLint
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
import com.example.projekat.MainActivity
import com.example.projekat.R
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import android.os.Looper
import kotlin.random.Random

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var currentLocation: Location
    private val firestore = FirebaseFirestore.getInstance()
    private val CHANNEL_ID = "LocationServiceChannel"
    private val NEARBY_THRESHOLD_METERS = 2000 // 2 km

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sendNotification(1,"Location Service Started", "App is now tracking your location.")
    }

    @SuppressLint("MissingPermission", "ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach { location ->
                    currentLocation = location
                    checkNearbyDanceClubs(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            LocationRequest.create().apply {
                interval = 10000 // 10 seconds
                fastestInterval = 5000 // 5 seconds
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            },
            locationCallback,
            Looper.getMainLooper()
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Service")
            .setContentText("Tracking your location")
            .setSmallIcon(R.drawable.ic_notification) // Replace with your notification icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)

        return START_STICKY
    }


    private fun checkNearbyDanceClubs(location: Location) {
        firestore.collection("dance_clubs")
            .get()
            .addOnSuccessListener { result ->
                val nearbyClubs = mutableListOf<String>()
                result.forEach { document ->
                    val lat = document.getDouble("latitude") ?: 0.0
                    val lng = document.getDouble("longitude") ?: 0.0
                    val danceClubLocation = Location("").apply {
                        latitude = lat
                        longitude = lng
                    }

                    val distance = location.distanceTo(danceClubLocation)
                    if (distance <= NEARBY_THRESHOLD_METERS) {
                        val clubName = document.getString("name") ?: "Unknown Club"
                        nearbyClubs.add("$clubName - ${distance.toInt()} meters away")
                        sendNotification(
                            clubName.hashCode(),
                            "Nearby Dance Club",
                            "You are near $clubName. Distance: ${distance.toInt()} meters."
                        )
                    }
                }
                // Optional: Log or handle all nearby clubs' names
                if (nearbyClubs.isNotEmpty()) {
                    Log.d("LocationService", "Nearby dance clubs: ${nearbyClubs.joinToString(", ")}")
                } else {
                    Log.d("LocationService", "No nearby dance clubs found.")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("LocationService", "Error getting dance clubs: ", exception)
            }
    }
    private fun sendNotification(id: Int, title: String, message: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Kreiranje intent-a koji će se pokrenuti kada se klikne na notifikaciju
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Kreiranje PendingIntent-a koji će startovati MainActivity
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Kreiranje Notification Channel-a za Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Kreiranje notifikacije sa pending intent-om
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification) // Zamenite vašom ikonicom za notifikaciju
            .setContentIntent(pendingIntent) // Dodavanje PendingIntent-a
            .setAutoCancel(true) // Automatski zatvara notifikaciju kada se klikne na nju
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // Prikazivanje notifikacije
        notificationManager.notify(id, notification)
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        sendNotification(1,"Location Service Stopped", "App is no longer tracking your location.")
    }
}
