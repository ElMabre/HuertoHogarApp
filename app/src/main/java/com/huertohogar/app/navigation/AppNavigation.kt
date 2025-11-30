package com.huertohogar.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.huertohogar.app.data.local.datastorage.SessionManager
import com.huertohogar.app.ui.screens.AboutUsScreen
import com.huertohogar.app.ui.screens.CartScreen
import com.huertohogar.app.ui.screens.ContactScreen
import com.huertohogar.app.ui.screens.HomeScreen
import com.huertohogar.app.ui.screens.LoginScreen
import com.huertohogar.app.ui.screens.MapScreen
import com.huertohogar.app.ui.screens.OrderHistoryScreen // Importante importar la nueva pantalla
import com.huertohogar.app.ui.screens.ProductDetailScreen
import com.huertohogar.app.ui.screens.ProductsScreen
import com.huertohogar.app.ui.screens.ProfileScreen
import com.huertohogar.app.ui.screens.RecipeScreen
import com.huertohogar.app.ui.screens.RegisterScreen
import com.huertohogar.app.viewmodel.CartViewModel
import com.huertohogar.app.viewmodel.ProfileViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val sessionManager = remember { SessionManager(context) }
    val tokenState by sessionManager.authToken.collectAsState(initial = null)

    val cartViewModel: CartViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    if (tokenState == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val startDest = if (tokenState.isNullOrBlank()) AppScreens.LoginScreen.route else AppScreens.HomeScreen.route

        NavHost(
            navController = navController,
            startDestination = startDest
        ) {
            composable(route = AppScreens.LoginScreen.route) {
                LoginScreen(navController = navController)
            }
            composable(route = AppScreens.RegisterScreen.route) {
                RegisterScreen(navController = navController)
            }
            composable(route = AppScreens.HomeScreen.route) {
                HomeScreen(
                    navController = navController,
                    cartViewModel = cartViewModel,
                    profileViewModel = profileViewModel
                )
            }
            composable(route = AppScreens.ProductsScreen.route) {
                ProductsScreen(navController = navController, cartViewModel = cartViewModel)
            }
            composable(
                route = AppScreens.ProductDetailScreen.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")
                ProductDetailScreen(
                    navController = navController,
                    productId = productId,
                    cartViewModel = cartViewModel
                )
            }
            composable(route = AppScreens.CartScreen.route) {
                CartScreen(navController = navController, cartViewModel = cartViewModel)
            }
            composable(route = AppScreens.ProfileScreen.route) {
                ProfileScreen(
                    navController = navController,
                    viewModel = profileViewModel
                )
            }
            composable(route = AppScreens.MapScreen.route) {
                MapScreen(navController = navController)
            }
            composable(route = AppScreens.AboutUsScreen.route) {
                AboutUsScreen(navController = navController)
            }
            composable(route = AppScreens.ContactScreen.route) {
                ContactScreen(navController = navController, cartViewModel = cartViewModel)
            }
            composable(route = AppScreens.RecipesScreen.route) {
                RecipeScreen(navController = navController, cartViewModel = cartViewModel)
            }

            composable(route = AppScreens.OrderHistoryScreen.route) {
                OrderHistoryScreen(
                    navController = navController,
                    cartViewModel = cartViewModel
                )
            }
        }
    }
}