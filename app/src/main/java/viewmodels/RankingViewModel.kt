package com.example.projekat.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserRanking(
    val userId: String = "",
    val userName: String = "",
    val points: Int = 0,
    val profilePictureUrl: String? = null
)

class RankingViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _rankings = MutableStateFlow<List<UserRanking>>(emptyList())
    val rankings: StateFlow<List<UserRanking>> = _rankings

    init {
        fetchRankings()
    }

    private fun fetchRankings() {
        viewModelScope.launch {
            try {
                val usersSnapshot = firestore.collection("users")
                    .orderBy("points", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                val userRankings = usersSnapshot.documents.map { document ->
                    val userId = document.id
                    val points = document.getLong("points")?.toInt() ?: 0
                    val userName = "${document.getString("firstName") ?: "Unknown"} ${document.getString("lastName") ?: ""}"
                    val profilePictureUrl = getProfilePictureUrl(userId)
                    UserRanking(userId, userName, points, profilePictureUrl)
                }

                _rankings.value = userRankings
                Log.d("RankingViewModel", "Fetched rankings: $userRankings")
            } catch (e: Exception) {
                Log.e("RankingViewModel", "Error fetching rankings", e)
                _rankings.value = emptyList()
            }
        }
    }

    private suspend fun getProfilePictureUrl(userId: String): String? {
        return try {
            val storageRef = storage.reference.child("profile_pictures/$userId.jpg")
            val downloadUrl = storageRef.downloadUrl.await()
            Log.d("RankingViewModel", "Fetched image URL for user $userId: $downloadUrl")
            downloadUrl.toString()
        } catch (e: Exception) {
            Log.e("RankingViewModel", "Error getting image URL for user $userId", e)
            null
        }
    }
}
