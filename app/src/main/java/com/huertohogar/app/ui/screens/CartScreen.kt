package com.huertohogar.app.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
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

// Objeto global para formateo consistente de moneda (Pesos Chilenos) en toda la pantalla.
private val chileLocale: Locale = Locale.forLanguageTag("es-CL")

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    cartViewModel: CartViewModel
) {
    // Suscripción reactiva al estado del ViewModel.
    // 'collectAsState' convierte el Flow de datos en un State de Compose.
    // Si el ViewModel actualiza la lista o los totales, la UI se redibuja automáticamente.
    val cartUiState by cartViewModel.uiState.collectAsState()

    // --- MANEJO DE EFECTOS DE UI (Feedback al usuario) ---
    // Se usan condicionales simples basados en flags del estado para mostrar diálogos modales.
    // Esto desacopla la lógica de visualización de la lógica de negocio.

    // 1. Diálogo de Éxito: Bloquea la interacción hasta que el usuario confirma.
    if (cartUiState.checkoutSuccess) {
        AlertDialog(
            onDismissRequest = {
                // Limpiamos el estado antes de navegar para evitar bucles o datos residuales.
                cartViewModel.resetCheckoutStatus()
                navController.popBackStack()
            },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("¡Compra Exitosa!") },
            text = { Text("Tu pedido ha sido procesado correctamente. El stock ha sido actualizado.") },
            confirmButton = {
                Button(onClick = {
                    cartViewModel.resetCheckoutStatus()
                    navController.popBackStack()
                }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // 2. Diálogo de Error: Informa sin interrumpir el flujo de navegación.
    if (cartUiState.checkoutError != null) {
        AlertDialog(
            onDismissRequest = { cartViewModel.resetCheckoutStatus() },
            title = { Text("Error en la Compra") },
            text = { Text(cartUiState.checkoutError ?: "Ocurrió un error desconocido.") },
            confirmButton = {
                TextButton(onClick = { cartViewModel.resetCheckoutStatus() }) {
                    Text("Cerrar")
                }
            },
            containerColor = MaterialTheme.colorScheme.errorContainer,
            titleContentColor = MaterialTheme.colorScheme.onErrorContainer,
            textContentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    }

    // Estructura base de la pantalla. Scaffold maneja los espacios seguros (TopBar, etc.)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tu Carrito") },
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
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            // Lógica de Renderizado Condicional:
            // Decide qué interfaz mostrar basándose en si hay datos, evitando mostrar una lista vacía.
            if (cartUiState.items.isEmpty() && !cartUiState.checkoutSuccess) {
                // VISTA VACÍA
                EmptyCartView(navController)
            } else {
                // VISTA CON ITEMS (Contenido principal)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // LazyColumn para renderizado eficiente de listas.
                    // A diferencia de Column, recicla las vistas y solo dibuja lo que se ve en pantalla.
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        // 'key' ayuda a Compose a identificar cada item único, mejorando el rendimiento
                        // y evitando errores visuales al borrar o reordenar elementos.
                        items(cartUiState.items, key = { it.producto.id }) { cartItem ->
                            CartListItem(
                                cartItem = cartItem,
                                // Lambda Hoisting: Pasamos eventos hacia arriba (al ViewModel)
                                // en lugar de pasar el ViewModel hacia abajo.
                                onQuantityChange = { qty -> cartViewModel.updateQuantity(cartItem.producto.id, qty) },
                                onRemoveClick = { cartViewModel.removeFromCart(cartItem.producto.id) }
                            )
                            HorizontalDivider()
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Componente separado para mostrar cálculos financieros.
                    CartSummary(cartUiState = cartUiState)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botones de Acción
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { cartViewModel.clearCart() },
                            modifier = Modifier.weight(1f),
                            // Deshabilitamos interacción durante estados de carga para evitar errores.
                            enabled = !cartUiState.isLoading
                        ) {
                            Text("Vaciar")
                        }
                        Button(
                            onClick = { cartViewModel.realizarPedido() }, // Dispara la lógica asíncrona (Red/BD)
                            modifier = Modifier.weight(1f),
                            enabled = !cartUiState.isLoading
                        ) {
                            // Feedback visual inmediato dentro del botón mientras carga
                            if (cartUiState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Pagar")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Componente Stateless (sin estado) para la vista vacía.
// Recibe solo lo necesario (navController) para redirigir al usuario.
@Composable
fun EmptyCartView(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Tu carrito está vacío", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Ir a comprar")
            }
        }
    }
}

// Item individual de la lista.
// Se mantiene puro (sin ViewModel dentro), recibiendo datos y emitiendo eventos via lambdas.
// Esto lo hace reutilizable y fácil de previsualizar o testear.
@Composable
private fun CartListItem(
    cartItem: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemoveClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Carga asíncrona de imágenes (usando librería Coil).
        // Maneja automáticamente placeholders y estados de error.
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(cartItem.producto.imagenUrl)
                .crossfade(true)
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_error_image)
                .build(),
            contentDescription = null,
            modifier = Modifier.size(80.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))

        // Información textual del producto
        Column(modifier = Modifier.weight(1f)) {
            Text(cartItem.producto.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "$${String.format(chileLocale, "%,.0f", cartItem.producto.precio)} / ${cartItem.producto.unidad}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Subtotal: $${String.format(chileLocale, "%,.0f", cartItem.subtotal)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Controles de manipulación (Borrar y +/- cantidad)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = onRemoveClick) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onQuantityChange(cartItem.cantidad - 1) }) {
                    Icon(Icons.Default.Remove, contentDescription = "Restar")
                }
                Text(text = "${cartItem.cantidad}", fontWeight = FontWeight.Bold)
                IconButton(onClick = { onQuantityChange(cartItem.cantidad + 1) }) {
                    Icon(Icons.Default.Add, contentDescription = "Sumar")
                }
            }
        }
    }
}

// Componente para resumir los costos.
// Separado para mantener el código principal (CartScreen) limpio y legible.
@Composable
private fun CartSummary(cartUiState: CartUiState) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal")
                Text("$${String.format(chileLocale, "%,.0f", cartUiState.subtotal)}")
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Envío")
                Text("$${String.format(chileLocale, "%,.0f", cartUiState.costoEnvio)}")
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "$${String.format(chileLocale, "%,.0f", cartUiState.total)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}