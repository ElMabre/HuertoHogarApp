package com.huertohogar.app.model

/**
 * Estado de la UI del Carrito.
 * Usamos propiedades calculadas (get) para subtotal, envío y total.
 * Esto asegura que siempre estén sincronizados con la lista de items.
 */
data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val isLoading: Boolean = false,
    val checkoutSuccess: Boolean = false,
    val checkoutError: String? = null
) {
    val subtotal: Double
        get() = items.sumOf { it.subtotal }

    // Si hay productos y el subtotal es menor a 15.000, cobra envío.
    // Si supera los 15.000, es gratis.
    val costoEnvio: Double
        get() = if (items.isNotEmpty() && subtotal < 15000) 3500.0 else 0.0

    val total: Double
        get() = subtotal + costoEnvio

    val numeroTotalItems: Int
        get() = items.sumOf { it.cantidad }
}