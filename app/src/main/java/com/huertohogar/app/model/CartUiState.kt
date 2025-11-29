package com.huertohogar.app.model

/**
 * Representa el estado completo de la UI para la pantalla del carrito de compras.
 * Contiene la lista de items, el subtotal y el total.
 * Actualizado: Ahora incluye estados para el proceso de compra (Checkout).
 */
data class CartUiState(
    val items: List<CartItem> = emptyList(),

    // Nuevos campos para manejar el flujo de pago
    val isLoading: Boolean = false,       // Para mostrar el spinner de carga
    val checkoutSuccess: Boolean = false, // Para saber si la compra fue exitosa
    val checkoutError: String? = null     // Para mostrar mensajes de error (ej: "Sin stock")
) {
    val subtotal: Double
        get() = items.sumOf { it.subtotal }

    val costoEnvio: Double
        get() = if (items.isNotEmpty()) 3500.0 else 0.0

    val total: Double
        get() = subtotal + costoEnvio

    val numeroTotalItems: Int
        get() = items.sumOf { it.cantidad }
}