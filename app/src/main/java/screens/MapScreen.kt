package com.example.projekat.screens

import LocationViewModel
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*

@Composable
fun MapScreen(navController: NavHostController) {
    val locationViewModel: LocationViewModel = viewModel()
    val currentLocation by locationViewModel.currentLocation.observeAsState()
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation ?: LatLng(43.321445, 21.896104), 15f)
    }
    val uiSettings by remember {
        mutableStateOf(MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = true))
    }
    val mapProperties by remember {
        mutableStateOf(MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = true))
    }
    var markers by remember { mutableStateOf(emptyList<MarkerOptions>()) }
    var markerMap by remember { mutableStateOf<Map<LatLng, String>>(emptyMap()) }
    var selectedMarker by remember { mutableStateOf<Marker?>(null) }

    // Load dance club data from Firestore
    LaunchedEffect(Unit) {
        firestore.collection("dance_clubs").get()
            .addOnSuccessListener { result ->
                val newMarkers = mutableListOf<MarkerOptions>()
                val newMarkerMap = mutableMapOf<LatLng, String>()

                result.forEach { document ->
                    val lat = document.getDouble("latitude") ?: 0.0
                    val lng = document.getDouble("longitude") ?: 0.0
                    val title = document.getString("name") ?: ""
                    val snippet = "Dance Type: ${document.getString("danceType")}, Working Hours: ${document.getString("workingHours")}"
                    val position = LatLng(lat, lng)

                    newMarkers.add(
                        MarkerOptions()
                            .position(position)
                            .title(title)
                            .snippet(snippet)
                    )

                    newMarkerMap[position] = document.id
                }
                markers = newMarkers
                markerMap = newMarkerMap
            }
            .addOnFailureListener { exception ->
                Log.w("MapScreen", "Error getting documents: ", exception)
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Map Screen", style = MaterialTheme.typography.h5, modifier = Modifier.padding(16.dp))
        Spacer(modifier = Modifier.height(8.dp))

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings,
            onMapLongClick = { latLng ->
                // Navigate to AddDanceClubScreen and pass the clicked location
                navController.navigate("add_dance_club?lat=${latLng.latitude}&lng=${latLng.longitude}")
            }
        ) {
            markers.forEach { markerOptions ->
                Marker(
                    state = rememberMarkerState(position = markerOptions.position),
                    title = markerOptions.title,
                    snippet = markerOptions.snippet,
                    onClick = { marker ->
                        selectedMarker = marker
                        // Navigate to ClubDetailScreen with the club ID
                        val clubId = markerMap[marker.position]
                        if (clubId != null) {
                            navController.navigate("club_detail/$clubId")
                        }
                        true
                    }
                )
            }
        }
    }
}