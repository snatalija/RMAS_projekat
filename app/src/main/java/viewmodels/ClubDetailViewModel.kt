package com.example.projekat.screens

import Club
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

    private val _ownerName = MutableStateFlow("") // New state for owner's name
    val ownerName: StateFlow<String> = _ownerName.asStateFlow()

    private val _reviewExists = MutableStateFlow(false)
    val reviewExists: StateFlow<Boolean> = _reviewExists.asStateFlow()

    fun loadClubDetails(clubId: String) {
        viewModelScope.launch {
            try {
                // Fetch club details
                val clubDocument = firestore.collection("dance_clubs").document(clubId).get().await()
                val clubData = clubDocument.toObject(Club::class.java)
                val userId = clubDocument.getString("userId") ?: ""
                _club.value = clubData

                clubData?.userId?.let { ownerId ->
                    _ownerName.value = getUserName(userId)
                }

                // Fetch reviews and map to Review data class
                val reviewsSnapshot = firestore.collection("dance_clubs").document(clubId).collection("reviews").get().await()
                _reviews.value = reviewsSnapshot.map { document ->
                    val reviewText = document.getString("review") ?: ""

                    val userName = getUserName(userId)  // Fetch user name asynchronously
                    val reviewRating = document.getLong("rating")?.toInt() ?: 0
                    Review(reviewText, userName, reviewRating)
                }

                // Check if the current user has already reviewed this club
                auth.currentUser?.let { user ->
                    val userReviewDoc = firestore.collection("dance_clubs").document(clubId).collection("reviews").document(user.uid).get().await()
                    _reviewExists.value = userReviewDoc.exists()
                    _club.value = _club.value?.copy(hasReviewed = userReviewDoc.exists())
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
                val userId = user.uid
                val reviewRef = firestore.collection("dance_clubs").document(clubId).collection("reviews").document(userId)

                try {
                    val reviewDoc = reviewRef.get().await()

                    if (!reviewDoc.exists()) {
                        val reviewData = hashMapOf(
                            "review" to _review.value,
                            "rating" to _rating.value,
                            "userId" to userId
                        )
                        reviewRef.set(reviewData).await()
                        _review.value = "" // Clear review field
                        _rating.value = 0  // Clear rating

                        // Update club status
                        val updatedClub = _club.value?.copy(hasReviewed = true)
                        _club.value = updatedClub
                        _reviewExists.value = true
                    } else {
                        _reviewExists.value = true // Set to true if review already exists
                    }
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



data class Review(
    val review: String = "",
    val userName: String = "",
    val rating: Int = 0
)
