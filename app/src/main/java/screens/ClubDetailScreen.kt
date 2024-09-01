package com.example.projekat.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.StateFlow


@Composable
fun ClubDetailScreen(navController: NavHostController, clubId: String) {
    val viewModel: ClubDetailViewModel = viewModel()
    val club by viewModel.club.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val review by viewModel.review.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    LaunchedEffect(clubId) {
        viewModel.loadClubDetails(clubId)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        club?.let {
            Text("Club Name: ${it.name}", style = MaterialTheme.typography.h5)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Dance Type: ${it.danceType}")
            Text("Working Hours: ${it.workingHours}")
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = review,
                onValueChange = { viewModel.updateReview(it) },
                label = { Text("Add Review") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.submitReview(clubId) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                Text("Submit Review")
            }

            Spacer(modifier = Modifier.height(16.dp))

            reviews.forEach { review ->
                Text("Review: ${review.review}", style = MaterialTheme.typography.body1)
                Text("By: ${review.userName}", style = MaterialTheme.typography.body2) // Display the user's name
                Spacer(modifier = Modifier.height(8.dp))
            }
        } ?: run {
            Text("Loading...", style = MaterialTheme.typography.h6)
        }
    }
}
