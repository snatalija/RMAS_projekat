package com.example.projekat.screens

import AllDanceClubsViewModel
import Club
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun AllDanceClubsScreen(navController: NavHostController) {
    val viewModel: AllDanceClubsViewModel = viewModel()
    val clubs by viewModel.clubs.collectAsState()
    val ownerNames by viewModel.ownerNames.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadClubs()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(clubs) { club ->
                val ownerName = ownerNames[club.id] ?: "Unknown"
                ClubListItem(
                    club = club,
                    ownerName = ownerName,
                    onClick = {
                        navController.navigate("club_detail/${club.id}")
                    }
                )
            }
        }
    }
}
@Composable
fun ClubListItem(club: Club, ownerName: String,onClick: () -> Unit) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(club.name, style = MaterialTheme.typography.h6)
            Text(club.danceType, style = MaterialTheme.typography.body1)
            Text("Working Hours: ${club.workingHours}", style = MaterialTheme.typography.body2)
            Text("Owner: $ownerName", style = MaterialTheme.typography.body2)
            Text("Average Rating: ${club.averageRating}", style = MaterialTheme.typography.body2)
        }
    }
}
