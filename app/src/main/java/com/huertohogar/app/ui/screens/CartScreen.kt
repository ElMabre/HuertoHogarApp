package com.huertohogar.app.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.huertohogar.app.R
import com.huertohogar.app.model.CartItem
import com.huertohogar.app.model.CartUiState
import com.huertohogar.app.viewmodel.CartViewModel
import java.util.Locale
private val chileLocale: Locale = Locale.forLanguageTag("es-CL")

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    cartViewModel: CartViewModel // Recibe la instancia compartida
) {
    val cartUiState by cartViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tu Carrito de Compras") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        if (cartUiState.items.isEmpty()) {
            // Muestra un mensaje si el carrito está vacío
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground), // Replace with a cart icon if available
                        contentDescription = "Carrito vacío",
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Tu carrito está vacío", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { navController.popBackStack() }) {  // Volver a la pantalla anterior
                        Text("Ver productos")
                    }
                }
            }
        } else {
            // Muestra la lista de items y el resumen si hay productos
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // Lista de items del carrito
                LazyColumn(
                    modifier = Modifier.weight(1f) // Ocupa el espacio disponible
                ) {
                    items(cartUiState.items, key = { it.producto.id }) { cartItem ->
                        CartListItem(
                            cartItem = cartItem,
                    
                            onQuantityChange = { newQuantity ->
                                cartViewModel.updateQuantity(cartItem.producto.id, newQuantity)
                            },
                            onRemoveClick = {
                                cartViewModel.removeFromCart(cartItem.producto.id)
                            }
                        )
                        // 'Divider' está obsoleto, usamos 'HorizontalDivider'
                        HorizontalDivider() // Separador entre items
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Resumen de Compra
                CartSummary(cartUiState = cartUiState)

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de Acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { cartViewModel.clearCart() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Vaciar Carrito")
                    }
                    Button(
                        onClick = {  /* TODO: Navegar al proceso de pago  */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Proceder al Pago")
                    }
                }
            }
        }
    }
}

// Composable para mostrar cada item en la lista del carrito
@Composable
private fun CartListItem(
    cartItem: CartItem,
    onQuantityChange: (Int) -> Unit, 
    onRemoveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Imagen del producto
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(cartItem.producto.imagenUrl)
                .crossfade(true)
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_error_image)
                .build(),
            contentDescription = cartItem.producto.nombre,
            modifier = Modifier
                .size(80.dp) // Imagen más pequeña
                .clip(CircleShape) // Hacemos la imagen redonda
                .padding(end = 16.dp),
            contentScale = ContentScale.Crop
        )

        // Nombre y Precio
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(cartItem.producto.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "$${String.format(chileLocale, "%,.0f", cartItem.producto.precio)} CLP / ${cartItem.producto.unidad}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Subtotal: $${String.format(chileLocale, "%,.0f", cartItem.subtotal)} CLP",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Controles de Cantidad y Eliminación
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // Botón de eliminar
            IconButton(onClick = onRemoveClick) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar item", tint = MaterialTheme.colorScheme.error)
            }

            
            // Implementamos los botones de +/-
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Botón de Restar
                IconButton(
                    onClick = { onQuantityChange(cartItem.cantidad - 1) },
                    enabled = cartItem.cantidad > 0 // Deshabilitado si es 0 (aunque se borraría)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Restar uno")
                }

                Text(
                    text = "${cartItem.cantidad}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.widthIn(min = 24.dp) // Da espacio para números de 2 dígitos
                )

                // Botón de Sumar
                IconButton(onClick = { onQuantityChange(cartItem.cantidad + 1) }) {
                    Icon(Icons.Default.Add, contentDescription = "Sumar uno")
                }
            }
        }
    }
}

// Composable para mostrar el resumen de la compra
@Composable
private fun CartSummary(cartUiState: CartUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Resumen de Compra", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal:")
                Text("$${String.format(chileLocale, "%,.0f", cartUiState.subtotal)} CLP")
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Costo de Envío:")
                Text("$${String.format(chileLocale, "%,.0f", cartUiState.costoEnvio)} CLP")
            }


            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    "$${String.format(chileLocale, "%,.0f", cartUiState.total)} CLP",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

