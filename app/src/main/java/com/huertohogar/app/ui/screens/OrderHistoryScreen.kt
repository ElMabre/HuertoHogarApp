package com.huertohogar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import com.huertohogar.app.viewmodel.CartViewModel
import com.huertohogar.app.viewmodel.OrderViewModel
import java.text.NumberFormat
import java.util.Locale

// Pantalla de Historial de Pedidos.
// Responsabilidad: Orquestar la obtención de datos, manejar estados de carga/error
// y mostrar la lista de pedidos pasados.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    navController: NavController,
    viewModel: OrderViewModel = viewModel(),
    // Se inyecta CartViewModel solo para mantener consistente la TopAppBar (contador del carrito).
    cartViewModel: CartViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    // HostState es necesario para controlar dónde y cuándo aparecen los Snackbars (mensajes emergentes).
    val snackbarHostState = remember { SnackbarHostState() }

    // --- MANEJO DE CICLO DE VIDA (Side Effects) ---

    // 1. Carga inicial: 'LaunchedEffect(Unit)' asegura que este bloque se ejecute
    // UNA sola vez cuando la pantalla entra en la composición, y no en cada redibujado.
    LaunchedEffect(Unit) {
        viewModel.loadMyOrders()
    }

    // 2. Feedback al usuario: Observa cambios en 'message' o 'error'.
    // Si el ViewModel emite un mensaje, se muestra el Snackbar y luego se limpia el estado.
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
                .background(Color(0xFFF5F5F5)) // Fondo gris para separar visualmente las tarjetas
        ) {
            // Lógica de Renderizado según Estado (Loading vs Vacío vs Lista)
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.pedidos.isEmpty()) {
                // Estado Vacío (Feedback visual cuando no hay datos)
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No tienes pedidos realizados", color = Color.Gray)
                }
            } else {
                // Lista de Pedidos
                // LazyColumn es vital aquí: si el usuario tiene 100 pedidos,
                // solo renderiza los que caben en la pantalla para ahorrar memoria.
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

// Componente individual de Tarjeta de Pedido.
// Contiene la lógica visual de cómo mostrar la información de UN pedido.
// Recibe un lambda 'onCancelClick' para delegar la acción al padre (Stateless component).
@Composable
fun PedidoItem(pedido: PedidoResponseDto, onCancelClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera: ID y Chip de Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pedido #${pedido.id}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                EstadoChip(estado = pedido.estado)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Información de Fecha
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = pedido.fecha ?: "Fecha desconocida",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Detalle de productos (Iteración simple dentro de la tarjeta)
            pedido.detalles?.forEach { detalle ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${detalle.cantidad}x ${detalle.producto?.nombre ?: "Producto"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatCurrency(detalle.precioUnitario * detalle.cantidad),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Pie de tarjeta: Total y Botones de Acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: ${formatCurrency(pedido.total)}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Lógica condicional de negocio en la UI:
                // Solo permitimos cancelar si el pedido aún no ha sido procesado ("Pendiente").
                if (pedido.estado.equals("Pendiente", ignoreCase = true)) {
                    Button(
                        onClick = onCancelClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

// Componente auxiliar visual.
// Mapea un string de estado ("Entregado", "Pendiente") a colores específicos
// para dar feedback visual rápido al usuario.
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

// Función utilitaria pura para formateo de moneda local (CLP)
fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("es", "CL")).format(amount)
}