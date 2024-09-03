package com.example.projekat

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.projekat.screens.AddDanceClubScreen
import com.example.projekat.screens.AllDanceClubsScreen
import com.example.projekat.screens.ClubDetailScreen
import com.example.projekat.screens.LoginScreen
import com.example.projekat.screens.ProfileScreen
import com.example.projekat.screens.RegistrationScreen
import com.example.projekat.screens.MapScreen
import com.example.projekat.screens.RankingScreen
import com.example.projekat.ui.theme.ProjekatTheme
import com.google.android.gms.games.leaderboard.Leaderboard
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "onCreate called")

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        Log.d("MainActivity", "Firebase initialized")

        // Set up the content view
        setContent {
            ProjekatTheme {
                val navController = rememberNavController()

                // Check user authentication status
                val isLoggedIn by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }
                Log.d("MainActivity", "User logged in: $isLoggedIn")

                // Determine start destination based on authentication status
                val startDestination = if (isLoggedIn) "profile" else "login"
                Log.d("MainActivity", "Start destination: $startDestination")

                Scaffold(
                    bottomBar = { BottomNavigationBar(navController) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavigationHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ""

    BottomNavigation {
        BottomNavigationItem(
            icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = currentRoute == "profile",
            onClick = {
                navController.navigate("profile") {
                    popUpTo("profile") { inclusive = true }
                }
            }
        )
        BottomNavigationItem(
            icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = "Map") },
            label = { Text("Map") },
            selected = currentRoute == "map",
            onClick = {
                navController.navigate("map") {
                    popUpTo("map") { inclusive = true }
                }
            }
        )

        BottomNavigationItem(
            icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = "All Clubs") },
            label = { Text("All Clubs") },
            selected = currentRoute == "all_clubs",
            onClick = {
                Log.d("BottomNavigationBar", "Navigating to all clubs")
                navController.navigate("all_clubs")

            }
        )
        BottomNavigationItem(
            icon = { Icon(imageVector = Icons.Filled.Star, contentDescription = "Rankings") },
            label = { Text("Rankings") },
            selected = currentRoute == "rankings",
            onClick = {
                navController.navigate("rankings") {
                    popUpTo("rankings") { inclusive = true }
                }
            }
        )
    }
}

@Composable
fun NavigationHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("register") {
            RegistrationScreen(navController = navController)
        }
        composable("profile") {
            ProfileScreen(navController = navController)
        }
        composable("map") {
            MapScreen(navController = navController)
        }
        composable("all_clubs") {
            AllDanceClubsScreen(navController = navController)
        }
        composable("add_dance_club?latitude={latitude}&longitude={longitude}") { backStackEntry ->
            val latitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull() ?: 0.0
            val longitude = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull() ?: 0.0
            AddDanceClubScreen(navController = navController, latitude = latitude, longitude = longitude)
        }
        composable("club_detail/{clubId}") { backStackEntry ->
            val clubId = backStackEntry.arguments?.getString("clubId") ?: ""
            Log.d("MapScreen", "Navigating to ClubDetailScreen with clubId: $clubId")

            ClubDetailScreen(navController = navController, clubId = clubId)
        }
        composable("rankings") {
            RankingScreen()
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ProjekatTheme {
        val navController = rememberNavController()
        Scaffold(bottomBar = { BottomNavigationBar(navController) }) { innerPadding ->
            NavigationHost(
                navController = navController,
                startDestination = "login", // Default start destination for preview
                modifier = Modifier.padding(innerPadding)
            )

        }
    }
}
