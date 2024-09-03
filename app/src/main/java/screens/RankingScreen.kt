package com.example.projekat.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RankingScreen(viewModel: RankingViewModel = viewModel()) {
    val rankings by viewModel.rankings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Leaderboard") })
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize()
        ) {
            items(rankings) { ranking ->
                RankingItem(ranking)
            }
        }
    }
}

@Composable
fun RankingItem(ranking: UserRanking) {
    Card(
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = ranking.userName, style = MaterialTheme.typography.h6)
                Text(text = "Points: ${ranking.points}", style = MaterialTheme.typography.body2)
            }
        }
    }
}
