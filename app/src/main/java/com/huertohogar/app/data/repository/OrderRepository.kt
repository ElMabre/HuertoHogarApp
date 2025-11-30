package com.huertohogar.app.data.repository

import com.huertohogar.app.data.remote.RetrofitClient
import com.huertohogar.app.data.remote.model.PedidoRequestDto
import com.huertohogar.app.data.remote.model.PedidoResponseDto
import retrofit2.Response

class OrderRepository {
    // Usamos la API centralizada
    private val api = RetrofitClient.orderApi

    /**
     * Crea un nuevo pedido.
     */
    suspend fun createOrder(token: String, order: PedidoRequestDto): Response<PedidoResponseDto> {
        val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
        // CORRECCIÓN: El método en la API se llama 'createPedido', no 'createOrder'
        return api.createPedido(bearerToken, order)
    }

    /**
     * Obtiene el historial de pedidos del usuario.
     */
    suspend fun getMyOrders(token: String): Response<List<PedidoResponseDto>> {
        val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
        // El método en la API se llama 'getMisPedidos'
        return api.getMisPedidos(bearerToken)
    }

    /**
     * Cancela un pedido existente.
     */
    suspend fun cancelOrder(token: String, orderId: Long): Response<Void> {
        val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
        // El método en la API se llama 'cancelarPedido'
        return api.cancelarPedido(bearerToken, orderId)
    }
}