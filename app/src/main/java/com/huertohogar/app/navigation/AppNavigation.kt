package com.huertohogar.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.huertohogar.app.ui.screens.CartScreen
import com.huertohogar.app.ui.screens.HomeScreen
import com.huertohogar.app.ui.screens.LoginScreen
import com.huertohogar.app.ui.screens.MapScreen // Importamos MapScreen
import com.huertohogar.app.ui.screens.ProductDetailScreen
import com.huertohogar.app.ui.screens.ProductsScreen
import com.huertohogar.app.ui.screens.ProfileScreen
import com.huertohogar.app.ui.screens.RegisterScreen
import com.huertohogar.app.viewmodel.CartViewModel
import com.huertohogar.app.viewmodel.ProfileViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // ViewModels compartidos o de alcance global para la navegación
    val cartViewModel: CartViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = AppScreens.LoginScreen.route
    ) {
        // --- Pantallas de Autenticación ---
        composable(route = AppScreens.LoginScreen.route) {
            LoginScreen(navController = navController)
        }
        composable(route = AppScreens.RegisterScreen.route) {
            RegisterScreen(navController = navController)
        }

        // --- Pantallas Principales ---
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
                profileViewModel = profileViewModel
            )
        }

        // --- Nueva Pantalla de Mapa ---
        composable(route = AppScreens.MapScreen.route) {
            MapScreen(navController = navController)
        }
    }
}