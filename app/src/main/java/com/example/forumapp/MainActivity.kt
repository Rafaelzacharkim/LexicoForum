package com.example.forumapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.forumapp.ui.ForumFeedScreen
import com.example.forumapp.ui.LoginScreen
import com.example.forumapp.ui.RegisterScreen
import com.example.forumapp.theme.ForumAppTheme
import com.example.forumapp.ui.CreatePostScreen
import com.example.forumapp.ui.ForumViewModel
import com.example.forumapp.ui.ProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForumAppTheme {
                val navController = rememberNavController()
                // Criamos o ViewModel aqui para ser compartilhado
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
                                // Navega para "criar" (sem ID)
                                onCreatePostClick = {
                                    navController.navigate("create_post")
                                },
                                // Navega para "editar" (com ID)
                                onEditPostClick = { postId ->
                                    navController.navigate("create_post?postId=$postId")
                                },
                                // Navegar para "perfil"
                                onProfileClick = {
                                    navController.navigate("profile")
                                }
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                modifier = Modifier.fillMaxSize(),
                                viewModel = forumViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onLogout = {
                                    // Limpa toda a pilha de navegação e vai para o login
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onPostClick = { _ ->
                                    navController.popBackStack()
                                }
                            )
                        }

                        // Rota atualizada para aceitar um ID opcional
                        composable(
                            route = "create_post?postId={postId}",
                            arguments = listOf(navArgument("postId") {
                                nullable = true
                                defaultValue = null
                            })
                        ) { backStackEntry ->
                            // Pega o postId da rota
                            val postId = backStackEntry.arguments?.getString("postId")

                            CreatePostScreen(
                                modifier = Modifier.fillMaxSize(),
                                viewModel = forumViewModel,
                                postId = postId, // Passa o ID (ou null)
                                onNavigateBack = {
                                    navController.popBackStack() // Volta
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}