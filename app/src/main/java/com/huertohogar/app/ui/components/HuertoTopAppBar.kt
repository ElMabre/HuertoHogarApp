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
// Componente reutilizable que actúa como barra superior principal de la app.
// Centraliza navegación, acceso al carrito y menú general.
fun HuertoTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    navController: NavController,
    cartViewModel: CartViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null
) {
    // Estado interno para manejar la apertura/cierre del menú desplegable.
    var menuExpanded by remember { mutableStateOf(false) }

    // Estado observable del carrito para actualizar dinámicamente el badge.
    val cartUiState by cartViewModel.uiState.collectAsState()

    // Barra superior estándar de Material3 con título, navegación y acciones.
    TopAppBar(
        title = { Text(title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),

        // Bloque de navegación: muestra botón de retroceso solo cuando aplica.
        // Permite comportamiento personalizado o el típico navigateUp().
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = {
                    if (onNavigateBack != null) onNavigateBack() else navController.navigateUp()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás"
                    )
                }
            }
        },

        // Acciones principales a la derecha: carrito + menú.
        actions = {
            // Botón del carrito: permite ir directamente a la pantalla del carro
            // e incluye un badge que refleja cuántos productos hay actualmente.
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

            // Botón de menú principal: abre el Dropdown con accesos rápidos.
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Filled.Menu, contentDescription = "Menú Principal")
            }

            // Menú desplegable: centraliza navegación a distintas secciones
            // importantes de la aplicación.
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                // Home: opción que reinicia la navegación a la pantalla inicial.
                DropdownMenuItem(
                    text = { Text("Inicio") },
                    onClick = {
                        menuExpanded = false
                        navController.navigate(AppScreens.HomeScreen.route) {
                            popUpTo(AppScreens.HomeScreen.route) { inclusive = true }
                        }
                    }
                )

                // Perfil del usuario.
                DropdownMenuItem(
                    text = { Text("Tu Perfil") },
                    onClick = {
                        menuExpanded = false
                        navController.navigate(AppScreens.ProfileScreen.route)
                    }
                )

                // Historial de pedidos.
                DropdownMenuItem(
                    text = { Text("Mis Pedidos") },
                    onClick = {
                        menuExpanded = false
                        navController.navigate(AppScreens.OrderHistoryScreen.route)
                    }
                )

                // Listado de productos disponibles.
                DropdownMenuItem(
                    text = { Text("Productos") },
                    onClick = {
                        menuExpanded = false
                        navController.navigate(AppScreens.ProductsScreen.route)
                    }
                )

                // Recetario.
                DropdownMenuItem(
                    text = { Text("Recetas") },
                    onClick = {
                        menuExpanded = false
                        navController.navigate(AppScreens.RecipesScreen.route)
                    }
                )

                // Información sobre la empresa.
                DropdownMenuItem(
                    text = { Text("Nosotros") },
                    onClick = {
                        menuExpanded = false
                        navController.navigate(AppScreens.AboutUsScreen.route)
                    }
                )

                // Formas de contacto.
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