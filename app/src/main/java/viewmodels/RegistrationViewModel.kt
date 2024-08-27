package com.example.projekat.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.UUID

class RegistrationViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    fun registerUser(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        profilePictureUri: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.let {
                            val userInfo = hashMapOf(
                                "uid" to it.uid,
                                "email" to email,
                                "firstName" to firstName,
                                "lastName" to lastName,
                                "phoneNumber" to phoneNumber
                            )

                            val userDocRef = db.collection("users").document(it.uid)

                            if (profilePictureUri != null) {
                                val storageRef = storage.reference.child("profile_pictures/${UUID.randomUUID()}")
                                val uploadTask = storageRef.putFile(Uri.parse(profilePictureUri))
                                uploadTask.addOnSuccessListener {
                                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                                        userInfo["profilePictureUri"] = downloadUrl.toString()
                                        userDocRef.set(userInfo)
                                            .addOnSuccessListener { onSuccess() }
                                            .addOnFailureListener { exception -> onFailure(exception) }
                                    }
                                }.addOnFailureListener { exception ->
                                    onFailure(exception)
                                }
                            } else {
                                userDocRef.set(userInfo)
                                    .addOnSuccessListener { onSuccess() }
                                    .addOnFailureListener { exception -> onFailure(exception) }
                            }
                        }
                    } else {
                        onFailure(task.exception ?: Exception("Unknown error"))
                    }
                }
        }
    }
}
