package com.huertohogar.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.huertohogar.app.navigation.AppScreens
import com.huertohogar.app.ui.components.HuertoTopAppBar
import com.huertohogar.app.ui.components.ProductCard
import com.huertohogar.app.viewmodel.CartViewModel
import com.huertohogar.app.viewmodel.ProductsViewModel

// Pantalla de Catálogo Completo.
// Responsabilidad: Mostrar todos los productos disponibles en un formato de grilla.
// Es una pantalla "Stateful" (con estado) porque observa directamente el ViewModel.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    navController: NavController,
    productsViewModel: ProductsViewModel = viewModel(),
    cartViewModel: CartViewModel
) {
    // Suscripción al flujo de estado (StateFlow).
    // Asegura que la UI se actualice automáticamente cuando cambian los datos (carga, error o lista).
    val uiState by productsViewModel.uiState.collectAsState()

    // Estructura base de la pantalla.
    // Reutilizamos `HuertoTopAppBar` para mantener la coherencia visual y el acceso al carrito.
    Scaffold(
        topBar = {
            HuertoTopAppBar(
                title = "Todos los Productos",
                canNavigateBack = true,
                navController = navController,
                cartViewModel = cartViewModel
            )
        }
    ) { innerPadding ->

        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            // Lógica de Renderizado Condicional.
            // Decide qué mostrar (Carga, Error o Contenido) basándose en el estado actual,
            // una práctica estándar en arquitecturas MVVM para dar feedback al usuario.
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "Error desconocido",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Grilla Adaptativa y Eficiente.
                // Usamos `LazyVerticalGrid` en lugar de `Column` para mostrar múltiples columnas.
                // `GridCells.Adaptive(minSize = 180.dp)` es clave: calcula automáticamente cuántas
                // columnas caben según el ancho del celular (responsivo).
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 180.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Renderizado de Items.
                    // Usamos el componente `ProductCard` para encapsular el diseño de cada celda
                    // y pasamos la navegación como un evento lambda.
                    items(uiState.productos) { producto ->
                        ProductCard(
                            producto = producto,
                            onProductClick = { productId ->
                                navController.navigate(
                                    AppScreens.ProductDetailScreen.createRoute(productId)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}