package com.huertohogar.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.huertohogar.app.navigation.AppScreens
import com.huertohogar.app.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HuertoTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    navController: NavController,
    cartViewModel: CartViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val cartUiState by cartViewModel.uiState.collectAsState()

    TopAppBar(
        title = { Text(title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = {
                    if (onNavigateBack != null) {
                        onNavigateBack()
                    } else {
                        navController.navigateUp()
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás"
                    )
                }
            }
        },
        actions = {
            // 1. Icono del Carrito
            IconButton(onClick = { navController.navigate(AppScreens.CartScreen.route) }) {
                BadgedBox(
                    badge = {
                        if (cartUiState.numeroTotalItems > 0) {
                            Badge { Text("${cartUiState.numeroTotalItems}") }
                        }
                    }
                ) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito")
                }
            }

            // 2. Menú Desplegable (Hamburguesa)
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Filled.Menu, contentDescription = "Menú Principal")
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                // Inicio
                DropdownMenuItem(
                    text = { Text("Inicio") },
                    onClick = {
                        menuExpanded = false
                        navController.navigate(AppScreens.HomeScreen.route) {
                            popUpTo(AppScreens.HomeScreen.route) { inclusive = true }
                        }
                    }
                )

                // Perfil
                DropdownMenuItem(
                    text = { Text("Tu Perfil") },
                    onClick = {
                        menuExpanded = false
                        navController.navigate(AppScreens.ProfileScreen.route)
                    }
                )

                // --- NUEVA OPCIÓN: MIS PEDIDOS ---
                DropdownMenuItem(
                    text = { Text("Mis Pedidos") },
                    onClick = {
                        menuExpanded = false
                        navController.navigate(AppScreens.OrderHistoryScreen.route)
                    }
                )

                // Productos
                DropdownMenuItem(
                    text = { Text("Productos") },
                    onClick = {
                        menuExpanded = false
                        navController.navigate(AppScreens.ProductsScreen.route)
                    }
                )

                // Recetas
                DropdownMenuItem(
                    text = { Text("Recetas") },
                    onClick = {
                        menuExpanded = false
                        navController.navigate(AppScreens.RecipesScreen.route)
                    }
                )

                // Nosotros
                DropdownMenuItem(
                    text = { Text("Nosotros") },
                    onClick = {
                        menuExpanded = false
                        navController.navigate(AppScreens.AboutUsScreen.route)
                    }
                )

                // Contacto
                DropdownMenuItem(
                    text = { Text("Contacto") },
                    onClick = {
                        menuExpanded = false
                        navController.navigate(AppScreens.ContactScreen.route)
                    }
                )
            }
        },
        modifier = modifier
    )
}