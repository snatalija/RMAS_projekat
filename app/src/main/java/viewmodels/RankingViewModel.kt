package com.example.projekat.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserRanking(
    val userId: String = "",
    val userName: String = "",
    val points: Int = 0
)

class RankingViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _rankings = MutableStateFlow<List<UserRanking>>(emptyList())
    val rankings: StateFlow<List<UserRanking>> = _rankings.asStateFlow()

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
                    val userName = document.getString("firstName") + " " + document.getString("lastName")
                    UserRanking(userId, userName ?: "Unknown", points)
                }

                _rankings.value = userRankings
                Log.d("RankingViewModel", "Fetched rankings: $userRankings")
            } catch (e: Exception) {
                Log.e("RankingViewModel", "Error fetching rankings", e)
                _rankings.value = emptyList()
            }
        }
    }
}
