package com.example.forumapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.forumapp.ui.ForumFeedScreen
import com.example.forumapp.ui.LoginScreen
import com.example.forumapp.ui.RegisterScreen
import com.example.forumapp.theme.ForumAppTheme
import com.example.forumapp.ui.CreatePostScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.forumapp.ui.ForumViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForumAppTheme {
                val navController = rememberNavController()
                val forumViewModel: ForumViewModel = viewModel()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "login"
                    ) {
                        composable("login") {
                            LoginScreen(
                                modifier = Modifier.fillMaxSize(),
                                onLoginSuccess = {
                                    navController.navigate("feed") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onRegisterClick = {
                                    navController.navigate("register")
                                }
                            )
                        }

                        composable("register") {
                            RegisterScreen(
                                modifier = Modifier.fillMaxSize(),
                                onRegisterSuccess = {
                                    navController.navigate("feed") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onLoginClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("feed") {
                            ForumFeedScreen(
                                modifier = Modifier.fillMaxSize(),
                                viewModel = forumViewModel,
                                onCreatePostClick = {
                                    navController.navigate("create_post")
                                }
                            )
                        }

                        composable("create_post") {
                            CreatePostScreen(
                                modifier = Modifier.fillMaxSize(),
                                viewModel = forumViewModel,
                                onPostCreated = {
                                    navController.popBackStack()
                                },
                                onCancel = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}