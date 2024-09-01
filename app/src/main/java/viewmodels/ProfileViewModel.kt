package com.example.projekat.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserProfile(
    var firstName: String = "",
    var lastName: String = "",
    var phoneNumber: String = "",
    var profilePictureUrl: String? = null
)
class ProfileViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            val userDocument = firestore.collection("users").document(userId).get().await()
            val userData = userDocument.data ?: return@launch

            _userProfile.value = UserProfile(
                firstName = userData["firstName"] as? String ?: "",
                lastName = userData["lastName"] as? String ?: "",
                phoneNumber = userData["phoneNumber"] as? String ?: "",
                profilePictureUrl = userData["profilePictureUrl"] as? String
            )
        }
    }

    fun updateUserProfile(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        profilePictureUrl: String?
    ) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            val userData = mapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "phoneNumber" to phoneNumber,
                "profilePictureUrl" to profilePictureUrl
            )

            firestore.collection("users").document(userId).set(userData).await()
            loadUserProfile()
        }
    }

    fun updateProfilePicture(uri: Uri) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            val storageRef = storage.reference.child("profilePictures/$userId.jpg")
            val uploadTask = storageRef.putFile(uri).await()
            val downloadUrl = uploadTask.metadata?.reference?.downloadUrl?.await().toString()

            updateUserProfile(
                firstName = _userProfile.value.firstName,
                lastName = _userProfile.value.lastName,
                phoneNumber = _userProfile.value.phoneNumber,
                profilePictureUrl = downloadUrl
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                auth.signOut()
                // Handle additional logout actions if needed
            } catch (e: Exception) {
                // Handle errors if needed
            }
        }
    }
}
