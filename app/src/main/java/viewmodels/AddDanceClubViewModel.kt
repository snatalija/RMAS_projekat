package com.example.projekat.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.UUID

class AddDanceClubViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser

    fun addDanceClub(
        clubName: String,
        workingHours: String,
        danceType: String,
        latitude: Double,
        longitude: Double,
        creationDate: String, // New parameter
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val clubId = UUID.randomUUID().toString() // Generate a unique ID for the club

                val danceClubData = hashMapOf(
                    "id" to clubId,
                    "name" to clubName,
                    "workingHours" to workingHours,
                    "danceType" to danceType,
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "creationDate" to creationDate, // Add creation date
                    "userId" to user?.uid
                )

                firestore.collection("dance_clubs")
                    .add(danceClubData)
                    .addOnSuccessListener { documentReference ->
                        Log.d("AddDanceClub", "DocumentSnapshot added with ID: ${documentReference.id}")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.w("AddDanceClub", "Error adding document", e)
                        onFailure(e)
                    }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }
}
