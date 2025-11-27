package com.huertohogar.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Map // Importamos el icono del Mapa
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.huertohogar.app.navigation.AppScreens
import com.huertohogar.app.ui.components.ProductCard
import com.huertohogar.app.viewmodel.CartViewModel
import com.huertohogar.app.viewmodel.HomeViewModel
import com.huertohogar.app.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel(),
    cartViewModel: CartViewModel,
    profileViewModel: ProfileViewModel
) {
    val homeUiState by homeViewModel.uiState.collectAsState()
    val cartUiState by cartViewModel.uiState.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HuertoHogar") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
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
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito")
                        }
                    }

                    IconButton(onClick = { navController.navigate(AppScreens.ProfileScreen.route) }) {
                        if (profileUiState.profileImageUri == null) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Mi Perfil"
                            )
                        } else {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(profileUiState.profileImageUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Mi Perfil",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¡Bienvenido a HuertoHogar!",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Productos frescos y naturales directo del campo a tu hogar.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Productos Destacados",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(homeUiState.productosDestacados) { producto ->
                    ProductCard(
                        producto = producto,
                        onProductClick = { productId ->
                            navController.navigate(
                                AppScreens.ProductDetailScreen.createRoute(productId)
                            )
                        }
                    )
                }
            }

            // Empuja el contenido hacia abajo
            Spacer(modifier = Modifier.weight(1f))

            // Botón 1: Ver Productos
            Button(
                onClick = { navController.navigate(AppScreens.ProductsScreen.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Ver Todos los Productos")
            }

            // Botón 2: Ver Mapa (NUEVO)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { navController.navigate(AppScreens.MapScreen.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Filled.Map, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver Nuestras Tiendas")
            }
        }
    }
}