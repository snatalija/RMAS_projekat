package com.example.projekat.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.twotone.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun ClubDetailDialog(
    isDialogOpen: MutableState<Boolean>,
    navController: NavHostController,
    clubId: String
) {
    if (isDialogOpen.value) {
        AlertDialog(
            onDismissRequest = { isDialogOpen.value = false },
            title = { Text("Club Details") },
            text = {
                ClubDetailScreenContent(navController, clubId)
            },
            confirmButton = {
                Button(onClick = { isDialogOpen.value = false }) {
                    Text("Close")
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.9f) // 90% širine ekrana
                .wrapContentHeight() // Visina prema sadržaju
                .padding(16.dp)
        )
    }
}

@Composable
fun ClubDetailScreenContent(navController: NavHostController, clubId: String) {
    val viewModel: ClubDetailViewModel = viewModel()
    val club by viewModel.club.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val review by viewModel.review.collectAsState()
    val rating by viewModel.rating.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val reviewExists by viewModel.reviewExists.collectAsState()
    val ownerName by viewModel.ownerName.collectAsState()
    val averageRating by viewModel.averageRating.collectAsState()

    LaunchedEffect(clubId) {
        viewModel.loadClubDetails(clubId)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        club?.let {
            Text("Club Name: ${it.name}", style = MaterialTheme.typography.h5)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Dance Type: ${it.danceType}")
            Text("Working Hours: ${it.workingHours}")
            Text("Owner: $ownerName")
            Spacer(modifier = Modifier.height(16.dp))

            Text("Average Rating: %.1f/5".format(averageRating), style = MaterialTheme.typography.body1)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 1..5) {
                    IconButton(
                        onClick = { viewModel.updateRating(i) }
                    ) {
                        Icon(
                            imageVector = if (i <= rating) Icons.Filled.Star else Icons.TwoTone.Star,
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
                Text("By: ${review.userName}", style = MaterialTheme.typography.body2)
                Text("Rating: ${review.rating}/5", style = MaterialTheme.typography.body2)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (reviewExists) {
                Text("You have already reviewed this club.", color = Color.Red, style = MaterialTheme.typography.body2)
            }
        } ?: run {
            Text("Loading...", style = MaterialTheme.typography.h6)
        }
    }
}
