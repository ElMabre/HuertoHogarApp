package com.huertohogar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.huertohogar.app.navigation.AppScreens
import com.huertohogar.app.ui.components.HuertoTopAppBar
import com.huertohogar.app.ui.components.ProductCard
import com.huertohogar.app.viewmodel.CartViewModel
import com.huertohogar.app.viewmodel.HomeViewModel
import com.huertohogar.app.viewmodel.ProfileViewModel

// Pantalla Principal (Dashboard).
// Actúa como un orquestador que muestra un resumen de la app.
// Recibe múltiples ViewModels porque necesita datos de distintas fuentes (Productos, Carrito, Perfil).
@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel(),
    cartViewModel: CartViewModel,
    profileViewModel: ProfileViewModel
) {
    // Patrón de observación de estado:
    // La UI reacciona automáticamente a cambios en 'homeUiState' (ej. cuando terminan de cargar los productos).
    val homeUiState by homeViewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            // Componente reutilizable para mantener consistencia visual.
            // Se le pasa el cartViewModel para que el ícono del carrito muestre el contador actualizado.
            HuertoTopAppBar(
                title = "HuertoHogar",
                canNavigateBack = false,
                navController = navController,
                cartViewModel = cartViewModel
            )
        }
    ) { innerPadding ->
        // Estructura de Scroll Vertical Global.
        // Se usa 'Column' con 'verticalScroll' en lugar de 'LazyColumn' porque los elementos hijos
        // son heterogéneos (Banner, Lista Horizontal, Botones) y no una lista infinita de items iguales.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- HERO SECTION (Banner Visual) ---
            // Uso de Box para superponer elementos (Z-Index):
            // 1. Imagen de fondo -> 2. Gradiente oscurecedor -> 3. Texto.
            // Esto asegura que el texto sea legible sin importar los colores de la imagen.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                // Imagen cargada desde URL (Coil)
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data("https://i.ibb.co/8LzHdNZR/Campo-Chileno.jpg")
                        .crossfade(true)
                        .build(),
                    contentDescription = "Banner Huerto",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradiente vertical negro con transparencia para contraste
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Black.copy(alpha = 0.7f))
                            )
                        )
                )

                // Texto superpuesto centrado
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Del Campo a tu Mesa",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black,
                                blurRadius = 4f
                            )
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Productos 100% orgánicos y frescos",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // --- CARRUSEL DE PRODUCTOS DESTACADOS ---
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Productos Destacados",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Manejo de estado de carga para feedback visual al usuario.
                if (homeUiState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Lista Horizontal .
                    // Compose maneja eficientemente un LazyRow dentro de un Column con verticalScroll.
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(homeUiState.productosDestacados) { producto ->
                            // Reutilización del componente 'ProductCard' usado en el catálogo
                            ProductCard(
                                producto = producto,
                                onProductClick = { productId ->
                                    navController.navigate(AppScreens.ProductDetailScreen.createRoute(productId))
                                },
                                modifier = Modifier.width(200.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- SECCIÓN DE NAVEGACIÓN RÁPIDA ---
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { navController.navigate(AppScreens.ProductsScreen.route) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Storefront, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver Catálogo Completo")
                }

                OutlinedButton(
                    onClick = { navController.navigate(AppScreens.MapScreen.route) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Map, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Encuentra Nuestras Tiendas")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}