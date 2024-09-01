package com.example.projekat.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.*

@Composable
fun AddDanceClubScreen(navController: NavHostController, latitude: Double, longitude: Double) {
    var clubName by remember { mutableStateOf("") }
    var workingHours by remember { mutableStateOf("") }
    var danceType by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val firestore = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

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

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isSaving = true
                val danceClubData = hashMapOf(
                    "name" to clubName,
                    "workingHours" to workingHours,
                    "danceType" to danceType,
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "userId" to user?.uid
                )

                firestore.collection("dance_clubs")
                    .add(danceClubData)
                    .addOnSuccessListener { documentReference ->
                        Log.d("AddDanceClub", "DocumentSnapshot added with ID: ${documentReference.id}")
                        navController.popBackStack() // Return to the map screen
                    }
                    .addOnFailureListener { e ->
                        Log.w("AddDanceClub", "Error adding document", e)
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            Text("Save")
        }
    }
}
