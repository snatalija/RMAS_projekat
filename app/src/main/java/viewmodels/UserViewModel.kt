package com.example.projekat.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    private val _userPoints = MutableLiveData(0)
    val userPoints: LiveData<Int> = _userPoints

    private val _userRank = MutableLiveData("Beginner")
    val userRank: LiveData<String> = _userRank

    fun addPoints(points: Int) {
        _userPoints.value = (_userPoints.value ?: 0) + points
        updateRank()
    }

    private fun updateRank() {
        val points = _userPoints.value ?: 0
        _userRank.value = when {
            points >= 1000 -> "Expert"
            points >= 500 -> "Intermediate"
            else -> "Beginner"
        }
    }
}
