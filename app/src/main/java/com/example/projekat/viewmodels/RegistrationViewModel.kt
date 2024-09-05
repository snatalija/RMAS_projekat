package com.example.projekat.viewmodels

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class RegistrationViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    var profileBitmap by mutableStateOf<Bitmap?>(null)
        private set

    var profileImageUri by mutableStateOf<Uri?>(null)
        private set



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
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User ID is null")

            val profilePictureUrl = profilePictureUri?.let {
                uploadProfileImage(it, userId)
            } ?: profileBitmap?.let {
                uploadBitmapAsImage(it, userId)
            }

            val userData = mapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "phoneNumber" to phoneNumber,
                "profilePictureUrl" to profilePictureUrl,
                "points" to 0
            )

            firestore.collection("users").document(userId).set(userData).await()

            onSuccess()

        } catch (exception: Exception) {
            onFailure(exception)
        }
    }

    private suspend fun uploadProfileImage(imageUri: String, userId: String): String {
        val storageRef = storage.reference.child("profile_pictures/$userId.jpg")
        val uploadTask = storageRef.putFile(Uri.parse(imageUri)).await()
        return uploadTask.metadata?.reference?.downloadUrl?.await().toString()
    }

    private suspend fun uploadBitmapAsImage(bitmap: Bitmap, userId: String): String {
        val uri = saveBitmapToTempFile(bitmap, userId)
        return uri?.let { uploadProfileImage(it.toString(), userId) } ?: throw Exception("Failed to upload bitmap")
    }

    private fun saveBitmapToTempFile(bitmap: Bitmap, userId: String): Uri? {
        return try {
            val file = File.createTempFile("profile_image_$userId", ".jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: IOException) {
            Log.e("RegistrationViewModel", "Failed to convert bitmap to file", e)
            null
        }
    }
}
