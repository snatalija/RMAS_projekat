package com.example.projekat.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class ClubDetailViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _club = MutableStateFlow<Club?>(null)
    val club: StateFlow<Club?> = _club.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _review = MutableStateFlow("")
    val review: StateFlow<String> = _review.asStateFlow()

    private val _rating = MutableStateFlow(0)  // Add this for rating
    val rating: StateFlow<Int> = _rating.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun loadClubDetails(clubId: String) {
        viewModelScope.launch {
            try {
                val clubDocument = firestore.collection("dance_clubs").document(clubId).get().await()
                _club.value = clubDocument.toObject(Club::class.java)

                val reviewsSnapshot = firestore.collection("dance_clubs").document(clubId).collection("reviews").get().await()
                _reviews.value = reviewsSnapshot.map { document ->
                    Log.d("nalesim", "" + document.getLong("rating")!!.toInt() + " " + document.getLong("rating"))
                    val reviewText = document.getString("review") ?: ""
                    val userId = document.getString("userId") ?: ""
                    val reviewRating = document.getLong("rating")!!.toInt() // Ensure rating is read as Int

                    updateRating(reviewRating)

                    val userName = getUserName(userId)
                    Review(reviewText, userName, reviewRating)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private suspend fun getUserName(userId: String): String {
        return try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val firstName = userDoc.getString("firstName") ?: ""
            val lastName = userDoc.getString("lastName") ?: ""
            "$firstName $lastName".trim()
        } catch (e: Exception) {
            // Handle error
            "Unknown"
        }
    }

    fun updateReview(newReview: String) {
        _review.value = newReview
    }
    fun updateRating(newRating: Int) {  // Add this to update rating
        _rating.value = newRating
    }

    fun submitReview(clubId: String) {
        viewModelScope.launch {
            _isSaving.value = true
            auth.currentUser?.let { user ->
                val reviewData = hashMapOf(
                    "review" to _review.value,
                    "rating" to _rating.value,
                    "userId" to user.uid
                )

                try {
                    firestore.collection("dance_clubs").document(clubId)
                        .collection("reviews")
                        .add(reviewData)
                        .await()
                    _review.value = "" // Clear review field
                    _rating.value = 0
                } catch (e: Exception) {
                    // Handle error
                } finally {
                    _isSaving.value = false
                }
            } ?: run {
                _isSaving.value = false
                // Handle case where user is null
            }
        }
    }
}

data class Club(
    val name: String = "",
    val danceType: String = "",
    val workingHours: String = ""
)

data class Review(
    val review: String = "",
    val userName: String = "",
    val rating: Int = 0
)
