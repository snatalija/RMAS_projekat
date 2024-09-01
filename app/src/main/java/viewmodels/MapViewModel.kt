package com.example.projekat.screens

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _currentLocation = MutableLiveData<Location>()
    val currentLocation: LiveData<Location> get() = _currentLocation

    fun fetchCurrentLocation() {
        // Check for permission
        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Handle the case where permission is not granted
            return
        }

        // Permission granted, proceed to get location
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                _currentLocation.value = it
            }
        }
    }
}
