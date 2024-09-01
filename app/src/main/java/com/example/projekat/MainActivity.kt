package com.example.projekat

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.projekat.screens.LoginScreen
import com.example.projekat.screens.ProfileScreen
import com.example.projekat.screens.RegistrationScreen
import com.example.projekat.screens.MapScreen
import com.example.projekat.ui.theme.ProjekatTheme
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

    Log.d("BottomNavigationBar", "Current route: $currentRoute")

    BottomNavigation {
        BottomNavigationItem(
            icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = currentRoute == "profile",
            onClick = {
                Log.d("BottomNavigationBar", "Navigating to profile")
                navController.navigate("profile")
            }
        )
        BottomNavigationItem(
            icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = "Map") },
            label = { Text("Map") },
            selected = currentRoute == "map",
            onClick = {
                Log.d("BottomNavigationBar", "Navigating to map")
                navController.navigate("map")
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
            MapScreen()
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
