package com.example.projekat.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddDanceClubScreen(navController: NavHostController, latitude: Double, longitude: Double) {
    val viewModel: AddDanceClubViewModel = viewModel()
    var clubName by remember { mutableStateOf("") }
    var workingHours by remember { mutableStateOf("") }
    var danceType by remember { mutableStateOf("") }
    var creationDate by remember { mutableStateOf("") } // New state for creation date
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Add Dance Club", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = clubName,
            onValueChange = { clubName = it },
            label = { Text("Club Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = workingHours,
            onValueChange = { workingHours = it },
            label = { Text("Working Hours") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = danceType,
            onValueChange = { danceType = it },
            label = { Text("Dance Type") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = creationDate,
            onValueChange = { creationDate = it },
            label = { Text("Creation Date (yyyy-MM-dd)") }, // Assuming date format
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isSaving = true

                // Validate the date format
                val isValidDate = try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    dateFormat.parse(creationDate) != null
                } catch (e: Exception) {
                    false
                }

                if (isValidDate) {
                    viewModel.addDanceClub(
                        clubName = clubName,
                        workingHours = workingHours,
                        danceType = danceType,
                        latitude = latitude,
                        longitude = longitude,
                        creationDate = creationDate, // Pass creation date
                        onSuccess = {
                            isSaving = false
                            navController.popBackStack() // Return to the map screen
                        },
                        onFailure = { e ->
                            isSaving = false
                            Log.w("AddDanceClub", "Error adding document", e)
                        }
                    )
                } else {
                    isSaving = false
                    // Show an error message or handle invalid date
                    Log.w("AddDanceClub", "Invalid date format")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            Text("Save")
        }
    }
}
