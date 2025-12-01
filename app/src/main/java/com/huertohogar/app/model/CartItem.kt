package com.huertohogar.app.model

/**
 * Representa un ítem individual dentro del carrito de compras.
 * Contiene la información del producto y la cantidad seleccionada.
 */
data class CartItem(
    val producto: Producto,
    val cantidad: Int
) {

    val subtotal: Double
        get() = producto.precio * cantidad
}