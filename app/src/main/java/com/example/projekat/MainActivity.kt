package com.example.projekat

import RegistrationScreen
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.projekat.screens.*
import com.example.projekat.ui.theme.ProjekatTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "onCreate called")

        FirebaseApp.initializeApp(this)
        Log.d("MainActivity", "Firebase initialized")

        setContent {
            ProjekatTheme {
                val navController = rememberNavController()

                var isLoggedIn by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }

                DisposableEffect(Unit) {
                    val authStateListener = FirebaseAuth.AuthStateListener { auth ->
                        isLoggedIn = auth.currentUser != null
                        Log.d("MainActivity", "Auth state changed: User logged in: $isLoggedIn")
                    }
                    FirebaseAuth.getInstance().addAuthStateListener(authStateListener)

                    onDispose {
                        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
                    }
                }

                val startDestination = if (isLoggedIn) "profile" else "login"
                Log.d("MainActivity", "Start destination: $startDestination")

                Scaffold(
                    bottomBar = {
                        if (isLoggedIn) {
                            BottomNavigationBar(navController)
                        }
                    },
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
            icon = { Icon(imageVector = Icons.Filled.LocationOn, contentDescription = "Map") },
            label = { Text("Map") },
            selected = currentRoute == "map",
            onClick = {
                navController.navigate("map") {
                    popUpTo("map") { inclusive = true }
                }
            }
        )

        BottomNavigationItem(
            icon = { Icon(imageVector = Icons.Filled.List, contentDescription = "All Clubs") },
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
                startDestination = "login",
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
