package com.example.projekat.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
        creationDate: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val clubId = UUID.randomUUID().toString()
                val danceClubData = hashMapOf(
                    "id" to clubId,
                    "name" to clubName,
                    "workingHours" to workingHours,
                    "danceType" to danceType,
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "creationDate" to creationDate,
                    "userId" to user?.uid,
                    "averageRating" to 0f
                )

                firestore.collection("dance_clubs")
                    .add(danceClubData)
                    .addOnSuccessListener { documentReference ->
                        Log.d("AddDanceClub", "DocumentSnapshot added with ID: ${documentReference.id}")
                        user?.uid?.let { userId ->
                            updateUserPoints(userId)
                        }
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
    private fun updateUserPoints(userId: String) {
        viewModelScope.launch {
            try {
                val userDoc = firestore.collection("users").document(userId).get().await()
                val currentPoints = userDoc.getLong("points")?.toInt() ?: 0
                val newPoints = currentPoints + 15
                firestore.collection("users").document(userId).update("points", newPoints).await()
                Log.d("AddDanceClub", "User points updated: $newPoints")
            } catch (e: Exception) {
                Log.e("AddDanceClub", "Error updating user points", e)
            }
        }
    }
}
