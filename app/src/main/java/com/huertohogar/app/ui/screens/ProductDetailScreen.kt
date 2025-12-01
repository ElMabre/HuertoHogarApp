@file:OptIn(ExperimentalMaterial3Api::class)

package com.huertohogar.app.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.huertohogar.app.R
import com.huertohogar.app.model.Producto
import com.huertohogar.app.navigation.AppScreens
import com.huertohogar.app.ui.theme.HuertoHogarAppTheme
import com.huertohogar.app.viewmodel.CartViewModel
import com.huertohogar.app.viewmodel.ProductDetailViewModel
import java.util.Locale

// Configuración global para formateo de moneda.
// Se define fuera de los composables para evitar recrear el objeto constantemente.
private val chileLocale: Locale = Locale.forLanguageTag("es-CL")

// Pantalla "Contenedor" (Stateful).
// Su responsabilidad es orquestar la lógica: obtener datos del ViewModel, gestionar estados de carga
// y conectar eventos (como añadir al carrito) con la lógica de negocio.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: String?,
    cartViewModel: CartViewModel,
    productDetailViewModel: ProductDetailViewModel = viewModel()
) {
    // Observamos los estados de ambos ViewModels.
    // 'detailUiState' maneja la carga del producto actual.
    // 'cartUiState' se necesita para actualizar el contador del carrito en la barra superior (Badge).
    val detailUiState by productDetailViewModel.uiState.collectAsState()
    val cartUiState by cartViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // --- EFECTO SECUNDARIO (Side Effect) ---
    // 'LaunchedEffect' es vital aquí. Ejecuta la llamada a la API (loadProductDetails)
    // SOLO cuando entra a la pantalla o si el 'productId' cambia.
    // Sin esto, la llamada se ejecutaría infinitamente en cada recomposición (redibujado).
    LaunchedEffect(productId) {
        productDetailViewModel.loadProductDetails(productId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detailUiState.producto?.nombre ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                // Icono del carrito con Badge (notificación roja).
                // Al observar 'cartUiState', el número se actualiza en tiempo real incluso desde esta pantalla.
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Manejo de Estados de UI (Loading / Error / Success).
            // Muestra una interfaz diferente según la respuesta asíncrona del ViewModel.
            when {
                detailUiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                detailUiState.error != null -> {
                    Text(
                        text = "Error: ${detailUiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                detailUiState.producto != null -> {
                    // Delegamos el dibujo del contenido a un composable "Puro".
                    // Pasamos solo los datos necesarios y una lambda para el evento de click.
                    ProductDetailsContent(
                        producto = detailUiState.producto!!,
                        onAddToCartClick = {
                            cartViewModel.addToCart(detailUiState.producto!!)
                            Toast.makeText(context, "${detailUiState.producto!!.nombre} añadido al carrito", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

// Componente de Contenido .
@SuppressLint("DefaultLocale")
@Composable
private fun ProductDetailsContent(
    producto: Producto,
    onAddToCartClick: () -> Unit
) {
    // Usamos 'verticalScroll' porque la descripción del producto puede ser larga
    // y debe ser leíble en pantallas pequeñas sin cortar el botón de compra.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Carga asíncrona de imágenes optimizada (Coil).
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(producto.imagenUrl)
                .crossfade(true)
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_error_image)
                .build(),
            contentDescription = "Imagen de ${producto.nombre}",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(bottom = 16.dp),
            contentScale = ContentScale.Crop
        )

        Text(
            text = producto.nombre,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Origen: ${producto.origen}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Formateo de moneda CLP usando el Locale definido arriba.
            Text(
                text = "$${String.format(chileLocale, "%,.0f", producto.precio)} CLP / ${producto.unidad}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Stock: ${producto.stock}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Descripción",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = producto.descripcion,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Botón de acción principal.
        // Se deshabilita visual y funcionalmente si no hay stock (Lógica de UI simple).
        Button(
            onClick = onAddToCartClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = producto.stock > 0
        ) {
            Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(if (producto.stock > 0) "Añadir al carrito" else "Sin Stock")
        }
    }
}

// Preview para desarrollo.
// Utiliza datos falsos (Mock) para renderizar la UI en Android Studio
// sin necesidad de ejecutar el backend ni la navegación.
@Preview(showBackground = true)
@Composable
fun ProductDetailScreenPreview() {
    val productoDeEjemplo = Producto(
        id = "FR001",
        databaseId = 1L,
        nombre = "Manzanas Fuji",
        descripcion = "...",
        precio = 1200.0,
        stock = 150,
        categoria = "frutas",
        imagenUrl = "",
        origen = "Valle del Maule",
        unidad = "Kg"
    )
    HuertoHogarAppTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("Detalle Producto") }) }) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                ProductDetailsContent(producto = productoDeEjemplo, onAddToCartClick = {})
            }
        }
    }
}