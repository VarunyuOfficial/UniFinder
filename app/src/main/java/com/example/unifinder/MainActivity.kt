package com.example.unifinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHost
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.unifinder.ui.*
import com.example.unifinder.ui.theme.UniFinderTheme
import com.example.unifinder.viewmodel.AuthViewModel
import com.example.unifinder.viewmodel.ItemViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniFinderTheme {
                val nav = rememberNavController()
                val authVm: AuthViewModel = viewModel()
                val itemVm: ItemViewModel = viewModel()
                val ready by authVm.ready.collectAsState()
                val user by authVm.user.collectAsState()

                if (!ready) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    NavHost(navController = nav, startDestination = if (user != null) "home" else "login") {
                        composable("login") {
                            LoginScreen(authVm) {
                                nav.navigate("home") { popUpTo("login") { inclusive = true } }
                            }
                        }
                        composable("home") {
                            HomeScreen(
                                itemVm = itemVm, authVm = authVm,
                                onAdd = { type -> nav.navigate("add/$type") },
                                onOpen = { id -> nav.navigate("detail/$id") },
                                onLogout = {
                                    authVm.logout()
                                    nav.navigate("login") { popUpTo("home") { inclusive = true } }
                                }
                            )
                        }
                        composable(
                            "add/{type}",
                            arguments = listOf(navArgument("type") { type = NavType.StringType })
                        ) { entry ->
                            AddItemScreen(
                                type = entry.arguments?.getString("type") ?: "LOST",
                                itemVm = itemVm, authVm = authVm,
                                onBack = { nav.popBackStack() }
                            )
                        }
                        composable(
                            "detail/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.IntType })
                        ) { entry ->
                            DetailScreen(
                                itemId = entry.arguments?.getInt("id") ?: 0,
                                itemVm = itemVm, authVm = authVm,
                                onBack = { nav.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
