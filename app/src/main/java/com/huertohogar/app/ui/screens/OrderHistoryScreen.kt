package com.huertohogar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.huertohogar.app.data.remote.model.PedidoResponseDto
import com.huertohogar.app.ui.components.HuertoTopAppBar
import com.huertohogar.app.viewmodel.OrderViewModel
import java.text.NumberFormat
import java.util.Locale

// ViewModel temporal para el carrito en el TopBar (si no lo usas en esta pantalla, puedes pasar uno dummy)
import com.huertohogar.app.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    navController: NavController,
    viewModel: OrderViewModel = viewModel(),
    // Necesitamos esto para la barra superior, aunque sea vacío
    cartViewModel: CartViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Efecto para mostrar mensajes (éxito al cancelar o error)
    LaunchedEffect(uiState.message, uiState.error) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            HuertoTopAppBar(
                title = "Mis Pedidos",
                canNavigateBack = true,
                navController = navController,
                cartViewModel = cartViewModel
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5)) // Fondo gris suave
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.pedidos.isEmpty()) {
                // Estado vacío
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No tienes pedidos realizados", color = Color.Gray)
                }
            } else {
                // Lista de Pedidos
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.pedidos) { pedido ->
                        PedidoItem(
                            pedido = pedido,
                            onCancelClick = { viewModel.cancelarPedido(pedido.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PedidoItem(pedido: PedidoResponseDto, onCancelClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera: ID y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pedido #${pedido.id}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                EstadoChip(estado = pedido.estado)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fecha
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(pedido.fecha ?: "Fecha desconocida", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Lista de productos (resumida)
            pedido.detalles?.forEach { detalle ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${detalle.cantidad}x ${detalle.producto?.nombre ?: "Producto"}", style = MaterialTheme.typography.bodyMedium)
                    Text(formatCurrency(detalle.precioUnitario * detalle.cantidad), style = MaterialTheme.typography.bodyMedium)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Total y Acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total: ${formatCurrency(pedido.total)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                // Solo se puede cancelar si está Pendiente
                if (pedido.estado.equals("Pendiente", ignoreCase = true)) {
                    Button(
                        onClick = onCancelClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

@Composable
fun EstadoChip(estado: String) {
    val (bgColor, textColor) = when (estado.lowercase()) {
        "pendiente" -> Color(0xFFFFF8E1) to Color(0xFFFFA000) // Amarillo
        "en camino" -> Color(0xFFE3F2FD) to Color(0xFF1976D2) // Azul
        "entregado", "completado" -> Color(0xFFE8F5E9) to Color(0xFF388E3C) // Verde
        else -> Color(0xFFEEEEEE) to Color.Gray
    }

    Surface(color = bgColor, shape = MaterialTheme.shapes.small) {
        Text(
            text = estado.uppercase(),
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("es", "CL")).format(amount)
}