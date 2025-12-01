package com.huertohogar.app.model

/**
 * Representa el estado completo de la UI para la pantalla del carrito de compras.
 * Contiene la lista de items, el subtotal y el total.
 * También maneja estados relacionados al proceso de pago (Checkout).
 */
data class CartUiState(
    val items: List<CartItem> = emptyList(),

    // Estados para manejar el flujo del proceso de compra
    val isLoading: Boolean = false,
    val checkoutSuccess: Boolean = false,
    val checkoutError: String? = null
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