package com.example.projekat.screens

import LocationViewModel
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(
    onLongPress: (LatLng) -> Unit
) {
    val context = LocalContext.current
    val locationViewModel : LocationViewModel = viewModel()

    val currentLocation by locationViewModel.currentLocation.observeAsState()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation ?: LatLng(43.321445, 21.896104), 15f)
    }
    val uiSettings by remember {
        mutableStateOf(MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = true))
    }
    val mapProperties by remember {
        mutableStateOf(MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = true))
    }
    LaunchedEffect(key1 = currentLocation) {
        snapshotFlow { currentLocation }
            .collect {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                    currentLocation ?: LatLng(43.321445, 21.896104), 15f
                )
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
                onLongPress(latLng)
            }
        )
    }
}
