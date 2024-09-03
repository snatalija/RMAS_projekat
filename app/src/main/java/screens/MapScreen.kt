package com.example.projekat.screens

import LocationViewModel
import android.util.Log
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
import androidx.compose.foundation.clickable

@Composable
fun MapScreen(navController: NavHostController) {
    val locationViewModel: LocationViewModel = viewModel()
    val currentLocation by locationViewModel.currentLocation.observeAsState()
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // State for rating range
    var minRating by remember { mutableStateOf(0.0) }
    var maxRating by remember { mutableStateOf(5.0) }

    // State for filter dropdown
    var expanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Filter by") }

    // State for dance style filter
    var danceStyles by remember { mutableStateOf(emptyList<String>()) }
    var selectedDanceStyle by remember { mutableStateOf<String?>(null) }

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

    // Load dance styles from Firestore
    LaunchedEffect(Unit) {
        firestore.collection("dance_clubs")
            .get()
            .addOnSuccessListener { result ->
                // Collect dance types
                val styles = result.mapNotNull { it.getString("danceType") }
                // Get distinct styles
                danceStyles = styles.distinct()
            }
            .addOnFailureListener { exception ->
                Log.w("MapScreen", "Error getting dance styles: ", exception)
            }
    }

    // Load dance club data from Firestore with rating and style filter
    LaunchedEffect(minRating, maxRating, selectedDanceStyle) {
        var query = firestore.collection("dance_clubs")
            .whereGreaterThanOrEqualTo("averageRating", minRating)
            .whereLessThanOrEqualTo("averageRating", maxRating)

        if (selectedDanceStyle != null) {
            query = query.whereEqualTo("danceType", selectedDanceStyle)
        }

        query.get()
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
        // Filter by dropdown
        Box(modifier = Modifier.padding(16.dp)) {
            Text(selectedFilter, style = MaterialTheme.typography.h6, modifier = Modifier.clickable { expanded = true })
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(onClick = {
                    selectedFilter = "Filter by Rating"
                    expanded = false
                }) {
                    Text("Filter by Rating")
                }
                DropdownMenuItem(onClick = {
                    selectedFilter = "Filter by Style"
                    expanded = false
                }) {
                    Text("Filter by Style")
                }
            }
        }

        if (selectedFilter == "Filter by Rating") {
            // Rating range sliders
            Text("Min Rating: ${"%.1f".format(minRating)}", modifier = Modifier.padding(start = 16.dp, end = 16.dp))
            Slider(
                value = minRating.toFloat(),
                onValueChange = { minRating = it.toDouble() },
                valueRange = 0f..5f,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            )

            Text("Max Rating: ${"%.1f".format(maxRating)}", modifier = Modifier.padding(start = 16.dp, end = 16.dp))
            Slider(
                value = maxRating.toFloat(),
                onValueChange = { maxRating = it.toDouble() },
                valueRange = 0f..5f,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            )
        } else if (selectedFilter == "Filter by Style") {
            // Dance style dropdown
            Box(modifier = Modifier.padding(16.dp)) {
                Text("Dance Style: ${selectedDanceStyle ?: "Select a style"}", style = MaterialTheme.typography.h6, modifier = Modifier.clickable { expanded = true })
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    danceStyles.forEach { style ->
                        DropdownMenuItem(onClick = {
                            selectedDanceStyle = style
                            expanded = false
                        }) {
                            Text(style)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings,
            onMapLongClick = { latLng ->
                // Navigate to AddDanceClubScreen and pass the clicked location
                navController.navigate("add_dance_club?latitude=${latLng.latitude}&longitude=${latLng.longitude}")
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
                            Log.d("MapScreen", "Navigating to ClubDetailScreen with ID: $clubId")
                            navController.navigate("club_detail/$clubId")
                        } else {
                            Log.w("MapScreen", "No club ID found for marker at ${marker.position}")
                        }
                        true
                    }
                )
            }
        }
    }
}
