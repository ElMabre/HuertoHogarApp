package com.huertohogar.app.navigation

/**
 * Define todas las rutas (URLs internas) de la aplicación.
 */
sealed class AppScreens(val route: String) {
    // Autenticación
    object LoginScreen : AppScreens("login_screen")
    object RegisterScreen : AppScreens("register_screen")

    // Core
    object HomeScreen : AppScreens("home_screen")
    object ProductsScreen : AppScreens("products_screen")

    // Navegación con argumentos
    object ProductDetailScreen : AppScreens("product_detail_screen/{productId}") {
        fun createRoute(productId: String) = "product_detail_screen/$productId"
    }

    object CartScreen : AppScreens("cart_screen")
    object ProfileScreen : AppScreens("profile_screen")

    // Rutas informativas
    object MapScreen : AppScreens("map_screen")
    object AboutUsScreen : AppScreens("about_us_screen")
    object ContactScreen : AppScreens("contact_screen")

    object RecipesScreen : AppScreens("recipes_screen")

    object OrderHistoryScreen : AppScreens("order_history_screen")
}