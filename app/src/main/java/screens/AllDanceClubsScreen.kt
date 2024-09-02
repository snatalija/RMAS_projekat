package com.example.projekat.screens

import AllDanceClubsViewModel
import Club
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    val authorNames by viewModel.authorNames.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        LazyColumn {
            items(clubs) { club ->
                ClubListItem(club = club, navController = navController, authorNames = authorNames)
            }
        }
    }
}

@Composable
fun ClubListItem(club: Club, navController: NavHostController, authorNames: Map<String, String>) {
    val authorName = authorNames[club.ownerId] ?: "Loading..."

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                navController.navigate("club_detail/${club.name}") // Navigate to club detail screen
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(club.name, style = MaterialTheme.typography.h6)
            Text(club.danceType, style = MaterialTheme.typography.body1)
            Text("Working Hours: ${club.workingHours}", style = MaterialTheme.typography.body2)
            //Text("Created At: ${club.createdAt}", style = MaterialTheme.typography.body2)
            Text("Author: $authorName", style = MaterialTheme.typography.body2)
        }
    }
}
