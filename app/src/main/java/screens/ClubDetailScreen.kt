package com.example.projekat.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction

@Composable
fun ClubDetailScreen(navController: NavHostController, clubId: String) {
    val viewModel: ClubDetailViewModel = viewModel()
    val club by viewModel.club.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val review by viewModel.review.collectAsState()
    val rating by viewModel.rating.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val reviewExists by viewModel.reviewExists.collectAsState()
    val ownerName by viewModel.ownerName.collectAsState()

    LaunchedEffect(clubId) {
        viewModel.loadClubDetails(clubId)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
        club?.let {
            Text("Club Name: ${it.name}", style = MaterialTheme.typography.h5)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Dance Type: ${it.danceType}")
            Text("Working Hours: ${it.workingHours}")
            Text("Owner: $ownerName") // Display the owner's name
            Spacer(modifier = Modifier.height(16.dp))
            // Rating
            Text("Rating: $rating/5", style = MaterialTheme.typography.body1)
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 1..5) {
                    IconButton(
                        onClick = { viewModel.updateRating(i) }
                    ) {
                        Icon(
                            imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "Rating Star"
                        )
                    }
                }
            }

            OutlinedTextField(
                value = review,
                onValueChange = { viewModel.updateReview(it) },
                label = { Text("Add Review") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                )
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
                Text("Rating: ${review.rating}/5", style = MaterialTheme.typography.body2) // Display the rating
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (reviewExists) {
                Text("You have already reviewed this club.", color = Color.Red, style = MaterialTheme.typography.body2)
            }
            val pinColor = if (it.hasReviewed) Color.Green else Color.Red

        } ?: run {
            Text("Loading...", style = MaterialTheme.typography.h6)
        }
    }
}
