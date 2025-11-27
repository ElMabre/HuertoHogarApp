package com.huertohogar.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue // IMPORTANTE: Soluciona el error de "getValue"
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.huertohogar.app.navigation.AppScreens
import com.huertohogar.app.ui.components.ProductCard
import com.huertohogar.app.viewmodel.CartViewModel
import com.huertohogar.app.viewmodel.ProductsViewModel
import com.huertohogar.app.model.Producto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    navController: NavController,
    productsViewModel: ProductsViewModel = viewModel(),
    cartViewModel: CartViewModel
) {
    // 1. Observamos los flujos del Nuevo ViewModel (Backend Real)
    val productsList by productsViewModel.products.collectAsState()
    val isLoading by productsViewModel.isLoading.collectAsState()
    val errorMessage by productsViewModel.errorMessage.collectAsState()

    val cartUiState by cartViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Todos los Productos") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { navController.navigate(AppScreens.CartScreen.route) }) {
                        BadgedBox(
                            badge = {
                                if (cartUiState.numeroTotalItems > 0) {
                                    Badge { Text("${cartUiState.numeroTotalItems}") }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Filled.ShoppingCart,
                                contentDescription = "Carrito de compras"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->

        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            // Manejo de estados de carga y error
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "Error desconocido",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Lista de productos
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 180.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(productsList) { productDto ->
                        // MAPEO: Convertimos de ProductDto (Red) a Producto (UI)
                        // Usamos el SKU como ID para la navegación
                        val productoUi = Producto(
                            id = productDto.sku,
                            nombre = productDto.nombre,
                            descripcion = productDto.descripcion,
                            precio = productDto.precio.toDouble(),
                            stock = productDto.stock,
                            categoria = productDto.categoria ?: "General",
                            imagenUrl = productDto.imagenUrl ?: "",
                            // Estos campos no vienen en el DTO simple, ponemos valores por defecto
                            origen = "Chile",
                            unidad = "Unidad"
                        )

                        ProductCard(
                            producto = productoUi,
                            onProductClick = { productId ->
                                navController.navigate(
                                    AppScreens.ProductDetailScreen.createRoute(productId)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}