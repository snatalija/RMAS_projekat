package com.example.projekat.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.projekat.viewmodels.ProfileViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = viewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var firstName by remember { mutableStateOf(userProfile.firstName) }
    var lastName by remember { mutableStateOf(userProfile.lastName) }
    var phoneNumber by remember { mutableStateOf(userProfile.phoneNumber) }
    var profilePictureUri by remember { mutableStateOf<Uri?>(userProfile.profilePictureUrl?.let { Uri.parse(it) }) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            profilePictureUri = it
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        userProfile.profilePictureUrl?.let {
            Image(
                painter = rememberImagePainter(data = it),
                contentDescription = null,
                modifier = Modifier.size(128.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = userProfile.firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = userProfile.lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = userProfile.phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") }
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            launcher.launch("image/*")
        }) {
            Text("Change Profile Picture")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            coroutineScope.launch {
                viewModel.updateUserProfile(
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phoneNumber,
                    profilePictureUrl = profilePictureUri?.toString()
                )
            }
        }) {
            Text("Save Changes")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            coroutineScope.launch {
                viewModel.logout()
                // Navigate to login screen or handle post-logout actions
                navController.navigate("login") {
                    popUpTo("profile") { inclusive = true }
                }
            }
        }) {
            Text("Logout")
        }
    }
}
