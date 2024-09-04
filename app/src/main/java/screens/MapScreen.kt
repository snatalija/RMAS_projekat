package com.example.projekat.screens

import LocationViewModel
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.Alignment
import android.content.Intent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import com.example.projekat.LocationService


@RequiresApi(Build.VERSION_CODES.O)
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

    // State for rating filter visibility
    var ratingFilterVisible by remember { mutableStateOf(false) }

    // State for style filter visibility
    var styleFilterVisible by remember { mutableStateOf(false) }

    // State for selected styles
    var selectedStyles by remember { mutableStateOf(setOf<String>()) }

    // Collect available dance styles
    val styles = remember { mutableStateListOf<String>() }

    var radiusFilterVisible by remember { mutableStateOf(false) }

    // State for selected radius (in kilometers)
    var selectedRadius by remember { mutableStateOf(5.0) }


    LaunchedEffect(Unit) {
        firestore.collection("dance_clubs")
            .get()
            .addOnSuccessListener { result ->
                val styleSet = mutableSetOf<String>()
                result.forEach { document ->
                    val style = document.getString("danceType")
                    if (style != null) {
                        styleSet.add(style)
                    }
                }
                styles.clear()
                styles.addAll(styleSet)
            }
            .addOnFailureListener { exception ->
                Log.w("MapScreen", "Error getting documents: ", exception)
            }
    }


    val cameraPositionState = rememberCameraPositionState {
        position =
            CameraPosition.fromLatLngZoom(currentLocation ?: LatLng(43.321445, 21.896104), 15f)
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
    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(it.latitude, it.longitude), 15f)
        }
    }
    // Load dance club data from Firestore with filters
    LaunchedEffect(minRating, maxRating, selectedStyles, selectedRadius) {
        var query = firestore.collection("dance_clubs")
            .whereGreaterThanOrEqualTo("averageRating", minRating)
            .whereLessThanOrEqualTo("averageRating", maxRating)

        if (selectedStyles.isNotEmpty()) {
            query = query.whereIn("danceType", selectedStyles.toList())
            Log.d("MapScreen", "Filtering by styles: $selectedStyles")
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
                    val distance = currentLocation?.let {
                        calculateDistance(LatLng(it.latitude, it.longitude), position)
                    } ?: Double.MAX_VALUE

                    // Add marker if within selected radius
                    if (distance <= selectedRadius * 1000) { // Convert km to meters
                        newMarkers.add(
                            MarkerOptions()
                                .position(position)
                                .title(title)
                                .snippet(snippet)
                        )

                        newMarkerMap[position] = document.id
                    }
                }
                markers = newMarkers
                markerMap = newMarkerMap
            }
            .addOnFailureListener { exception ->
                Log.w("MapScreen", "Error getting documents: ", exception)
            }
    }


    Column(modifier = Modifier.fillMaxSize()) {
        // Service control buttons
        ServiceControl()

        // Spacer to separate service control from the rest of the content
        Spacer(modifier = Modifier.height(8.dp))

        // Filter by dropdown
        Box(modifier = Modifier.padding(16.dp)) {
            Text(
                selectedFilter,
                style = MaterialTheme.typography.h6,
                modifier = Modifier
                    .clickable {
                        expanded = !expanded // Toggle dropdown visibility
                    }
                    .padding(16.dp)
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(onClick = {
                    if (selectedFilter != "Filter by Rating") {
                        selectedFilter = "Filter by Rating"
                        expanded = false // Close the dropdown
                        ratingFilterVisible = true // Show rating filter
                        styleFilterVisible = false // Hide style filter
                        radiusFilterVisible = false // Hide radius filter
                    } else {
                        expanded = false // Close the dropdown
                    }
                }) {
                    Text("Filter by Rating")
                }
                DropdownMenuItem(onClick = {
                    if (selectedFilter != "Filter by Style") {
                        selectedFilter = "Filter by Style"
                        expanded = false // Close the dropdown
                        styleFilterVisible = true // Show style filter
                        ratingFilterVisible = false // Hide rating filter
                        radiusFilterVisible = false // Hide radius filter
                    } else {
                        expanded = false // Close the dropdown
                    }
                }) {
                    Text("Filter by Style")
                }
                DropdownMenuItem(onClick = {
                    if (selectedFilter != "Filter by Radius") {
                        selectedFilter = "Filter by Radius"
                        expanded = false // Close the dropdown
                        radiusFilterVisible = true // Show radius filter
                        ratingFilterVisible = false // Hide rating filter
                        styleFilterVisible = false // Hide style filter
                    } else {
                        expanded = false // Close the dropdown
                    }
                }) {
                    Text("Filter by Radius")
                }
            }
        }

        AnimatedVisibility(
            visible = ratingFilterVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            // Rating filter
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(MaterialTheme.colors.surface, shape = MaterialTheme.shapes.medium)
                    .border(1.dp, MaterialTheme.colors.onSurface, MaterialTheme.shapes.medium)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Close button
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable {
                                selectedFilter = "Filter by"
                                ratingFilterVisible = false
                            }
                            .padding(8.dp)
                    )

                    // Rating range sliders
                    Text(
                        "Min Rating: ${"%.1f".format(minRating)}",
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                    )
                    Slider(
                        value = minRating.toFloat(),
                        onValueChange = { minRating = it.toDouble() },
                        valueRange = 0f..5f,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                    )

                    Text(
                        "Max Rating: ${"%.1f".format(maxRating)}",
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                    )
                    Slider(
                        value = maxRating.toFloat(),
                        onValueChange = { maxRating = it.toDouble() },
                        valueRange = 0f..5f,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = radiusFilterVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(MaterialTheme.colors.surface, shape = MaterialTheme.shapes.medium)
                    .border(1.dp, MaterialTheme.colors.onSurface, MaterialTheme.shapes.medium)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Close button
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable {
                                selectedFilter = "Filter by"
                                radiusFilterVisible = false
                            }
                            .padding(8.dp)
                    )

                    // Radius range slider
                    Text(
                        "Selected Radius: ${"%.1f".format(selectedRadius)} km",
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                    )
                    Slider(
                        value = selectedRadius.toFloat(),
                        onValueChange = { selectedRadius = it.toDouble() },
                        valueRange = 1f..50f, // Assuming 1 km to 50 km as reasonable range
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = styleFilterVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            // Style filter
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(MaterialTheme.colors.surface, shape = MaterialTheme.shapes.medium)
                    .border(1.dp, MaterialTheme.colors.onSurface, MaterialTheme.shapes.medium)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Close button
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable {
                                selectedFilter = "Filter by"
                                styleFilterVisible = false
                            }
                            .padding(8.dp)
                    )

                    // Style checkboxes
                    styles.forEach { style ->
                        Row(modifier = Modifier.padding(8.dp)) {
                            Checkbox(
                                checked = selectedStyles.contains(style),
                                onCheckedChange = { checked ->
                                    selectedStyles = if (checked) {
                                        selectedStyles + style
                                    } else {
                                        selectedStyles - style
                                    }
                                }
                            )
                            Text(text = style, modifier = Modifier.padding(start = 8.dp))
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
                currentLocation?.let {
                    navController.navigate("add_dance_club?latitude=${it.latitude}&longitude=${it.longitude}")
                }
            }
        ) {
            key(markers) {
                markers.forEach { markerOptions ->
                    Marker(
                        state = rememberMarkerState(position = markerOptions.position),
                        title = markerOptions.title,
                        snippet = markerOptions.snippet,
                        onClick = { marker ->
                            selectedMarker = marker
                            val clubId = markerMap[marker.position]
                            if (clubId != null) {
                                Log.d(
                                    "MapScreen",
                                    "Navigating to ClubDetailScreen with ID: $clubId"
                                )
                                navController.navigate("club_detail/$clubId")
                            } else {
                                Log.w(
                                    "MapScreen",
                                    "No club ID found for marker at ${marker.position}"
                                )
                            }
                            true
                        }
                    )
                    Log.d(
                        "MapScreen",
                        "Marker added: ${markerOptions.title} at ${markerOptions.position.latitude}, ${markerOptions.position.longitude}"
                    )

                }
            }
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ServiceControl() {
    val context = LocalContext.current

    // State for service status
    var isServiceRunning by remember { mutableStateOf(false) }

    // Function to start the service
    fun startService() {
        val serviceIntent = Intent(context, LocationService::class.java)
        context.startForegroundService(serviceIntent)
        isServiceRunning = true
    }

    // Function to stop the service
    fun stopService() {
        val serviceIntent = Intent(context, LocationService::class.java)
        context.stopService(serviceIntent)
        isServiceRunning = false
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.surface, shape = MaterialTheme.shapes.medium),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = { startService() },
            enabled = !isServiceRunning // Disable if service is running
        ) {
            Text("Start Service")
        }

        Button(
            onClick = { stopService() },
            enabled = isServiceRunning // Disable if service is not running
        ) {
            Text("Stop Service")
        }
    }
}
fun calculateDistance(start: LatLng, end: LatLng): Double {
    val result = FloatArray(1)
    android.location.Location.distanceBetween(
        start.latitude, start.longitude,
        end.latitude, end.longitude,
        result
    )
    val distance = result[0].toDouble()

    // Log coordinates and distance
    Log.d(
        "DistanceCalculation",
        "Start coordinates: (${start.latitude}, ${start.longitude}), End coordinates: (${end.latitude}, ${end.longitude}), Distance: $distance meters"
    )

    return distance
}