package com.huertohogar.app.data.repository

import com.huertohogar.app.data.remote.RetrofitClient
import com.huertohogar.app.data.remote.model.DetalleRequestDto
import com.huertohogar.app.data.remote.model.PedidoRequestDto
import com.huertohogar.app.data.remote.model.PedidoResponseDto
import com.huertohogar.app.model.CartItem
import retrofit2.Response

class OrderRepository {

    private val api = RetrofitClient.orderApi

    /**
     * Envía un pedido al servidor.
     * @param token Token de autenticación del usuario (JWT).
     * @param cartItems Lista de items que están en el carrito.
     * @param total Monto total de la compra.
     */
    suspend fun createOrder(token: String, cartItems: List<CartItem>, total: Double): Response<PedidoResponseDto> {

        // 1. Transformación de Datos:
        // Convertimos los objetos de dominio 'CartItem' a los DTOs que espera la API ('DetalleRequestDto').
        // Aquí es donde usamos el 'databaseId' (Long) en lugar del 'id' (String/SKU).
        val detallesDto = cartItems.map { item ->
            DetalleRequestDto(
                productoId = item.producto.databaseId, // CRÍTICO: ID numérico para la BD
                cantidad = item.cantidad,
                precio = item.producto.precio
            )
        }

        // 2. Construcción del Request
        val request = PedidoRequestDto(
            total = total,
            productos = detallesDto
        )

        // 3. Llamada a la API
        // Aseguramos que el token tenga el prefijo correcto
        val authToken = if (token.startsWith("Bearer ")) token else "Bearer $token"

        return api.createPedido(authToken, request)
    }
}