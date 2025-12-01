package com.huertohogar.app.navigation

/**
 * Define todas las rutas de navegación internas de la app.
 * Cada pantalla se representa como un objeto dentro del sealed class.
 * Esto permite mantener la navegación tipada y centralizada.
 */
sealed class AppScreens(val route: String) {

    // Sección de autenticación
    object LoginScreen : AppScreens("login_screen")
    object RegisterScreen : AppScreens("register_screen")

    // Pantallas principales
    object HomeScreen : AppScreens("home_screen")
    object ProductsScreen : AppScreens("products_screen")

    // Pantalla con argumento dinámico (productId)
    object ProductDetailScreen : AppScreens("product_detail_screen/{productId}") {
        fun createRoute(productId: String) = "product_detail_screen/$productId"
    }

    // Pantallas de interacción del usuario
    object CartScreen : AppScreens("cart_screen")
    object ProfileScreen : AppScreens("profile_screen")

    // Pantallas informativas
    object MapScreen : AppScreens("map_screen")
    object AboutUsScreen : AppScreens("about_us_screen")
    object ContactScreen : AppScreens("contact_screen")

    object RecipesScreen : AppScreens("recipes_screen")

    // Historial de pedidos
    object OrderHistoryScreen : AppScreens("order_history_screen")
}