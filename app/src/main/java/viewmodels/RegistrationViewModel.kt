package com.example.projekat.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.tasks.await

class RegistrationViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    suspend fun registerUser(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        profilePictureUri: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            // Register user with Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()

            // Get the user ID
            val userId = authResult.user?.uid ?: throw Exception("User ID is null")

            // Upload profile picture if exists
            val profilePictureUrl = profilePictureUri?.let {
                val storageRef = storage.reference.child("profile_pictures/$userId.jpg")
                val uploadTask = storageRef.putFile(Uri.parse(it))
                uploadTask.addOnFailureListener { exception ->
                    Log.e("FirebaseStorage", "Error uploading file", exception)
                }
                val uploadTaskSnapshot = uploadTask.await()
                uploadTaskSnapshot.metadata?.reference?.downloadUrl?.await().toString()
            }

            // Save user information to Firestore
            val userData = mapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "phoneNumber" to phoneNumber,
                "profilePictureUrl" to profilePictureUrl
            )

            firestore.collection("users").document(userId).set(userData).await()

            // Call success callback
            onSuccess()

        } catch (exception: Exception) {
            // Call failure callback
            onFailure(exception)
        }
    }
}
