package com.example.projekat.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.projekat.viewmodels.LoginViewModel

@Composable
fun LoginScreen(navController: NavHostController) {
    val loginViewModel: LoginViewModel = viewModel()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(16.dp)
                .shadow(4.dp, MaterialTheme.shapes.medium)
                .background(MaterialTheme.colors.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colors.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    loginViewModel.loginUser(
                        email = username,
                        password = password,
                        onSuccess = {
                            navController.navigate("profile")
                        },
                        onFailure = { exception ->
                            errorMessage = "Login failed: ${exception.message}"
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Login")
            }
            Spacer(modifier = Modifier.height(16.dp))
            ClickableText(
                text = AnnotatedString("Don't have an account? Register here"),
                onClick = { offset ->
                    // You may want to handle specific text click offsets here
                    navController.navigate("register")
                },
                style = MaterialTheme.typography.body1.copy(
                    color = MaterialTheme.colors.primary
                )
            )
        }
    }
}
