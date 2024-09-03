package com.example.projekat.screens

import Club
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

    private val _ownerName = MutableStateFlow("") // New state for owner's name
    val ownerName: StateFlow<String> = _ownerName.asStateFlow()


    private val _reviewExists = MutableStateFlow(false)
    val reviewExists: StateFlow<Boolean> = _reviewExists.asStateFlow()

    private val _averageRating = MutableStateFlow(0f)
    val averageRating: StateFlow<Float> = _averageRating.asStateFlow()

    fun loadClubDetails(clubId: String) {
        Log.d("ClubDetailViewModel", "Loading club details for clubId: $clubId")

        viewModelScope.launch {
            try {
                // Fetch club details
                val clubDocument = firestore.collection("dance_clubs").document(clubId).get().await()
                val clubData = clubDocument.toObject(Club::class.java)
                val userId = clubDocument.getString("userId") ?: ""
                _club.value = clubData
                Log.d("ClubDetailViewModel", "Fetched club data: $clubData")

                clubData?.userId?.let { userId ->
                    _ownerName.value = getUserName(userId)
                }

                // Fetch reviews and map to Review data class
                val reviewsSnapshot = firestore.collection("dance_clubs").document(clubId).collection("reviews").get().await()
                val reviewsList = reviewsSnapshot.map { document ->
                    val reviewText = document.getString("review") ?: ""
                    val authorId=document.getString("userId")?:""
                    val userName = getUserName(authorId)  // Fetch user name asynchronously
                    val reviewRating = document.getLong("rating")?.toInt() ?: 0
                    Log.d("ClubDetailViewModel", "Review Rating: $reviewRating")
                    Review(reviewText, userName, reviewRating)
                }
                _reviews.value = reviewsList

                // Calculate average rating
                val newAverageRating = calculateAverageRating(reviewsList)
                _averageRating.value = newAverageRating
                updateAverageRatingInFirestore(clubId, _averageRating.value)
                Log.d("ClubDetailViewModel", "Calculated average rating: $newAverageRating")

                // Check if the current user has already reviewed this club
                auth.currentUser?.let { user ->
                    val userReviewDoc = firestore.collection("dance_clubs").document(clubId).collection("reviews").document(user.uid).get().await()
                    _reviewExists.value = userReviewDoc.exists()
                    Log.d("ClubDetailViewModel", "User has reviewed: ${userReviewDoc.exists()}")

                    _club.value = _club.value?.copy(hasReviewed = userReviewDoc.exists())
                }
            } catch (e: Exception) {
                Log.e("ClubDetailViewModel", "Error loading club details", e)
                // Handle error
                e.printStackTrace()
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
            Log.e("ClubDetailViewModel", "Error fetching user name", e)

            "Unknown"
        }
    }
    private fun calculateAverageRating(reviews: List<Review>): Float {
        return if (reviews.isNotEmpty()) {
            reviews.map { it.rating }.average().toFloat()
        } else {
            0f
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

                        val updatedReviews = _reviews.value + Review(_review.value, getUserName(userId), _rating.value)
                        _reviews.value = updatedReviews

                        // Calculate new average rating
                        val newAverageRating = calculateAverageRating(updatedReviews)
                        _averageRating.value = newAverageRating

                        // Update club status and average rating in Firestore
                        val updatedClub = _club.value?.copy(hasReviewed = true, averageRating = newAverageRating)
                        _club.value = updatedClub

                        firestore.collection("dance_clubs").document(clubId).update("hasReviewed", true, "averageRating", newAverageRating).await()
                        _reviewExists.value = true

                    } else {
                        _reviewExists.value = true // Set to true if review already exists
                    }
                } catch (e: Exception) {
                    // Handle error
                    e.printStackTrace()
                } finally {
                    _isSaving.value = false
                }
            } ?: run {
                _isSaving.value = false
                // Handle case where user is null
            }
        }
    }
    private suspend fun updateAverageRatingInFirestore(clubId: String, averageRating: Float) {
        try {
            firestore.collection("dance_clubs").document(clubId).update("averageRating", averageRating).await()
        } catch (e: Exception) {
            // Handle error
            e.printStackTrace()
        }
    }

}



data class Review(
    val review: String = "",
    val userName: String = "",
    val rating: Int=0
)
