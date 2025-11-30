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
     */
    suspend fun createOrder(token: String, cartItems: List<CartItem>, total: Double): Response<PedidoResponseDto> {
        val detallesDto = cartItems.map { item ->
            DetalleRequestDto(
                productoId = item.producto.databaseId,
                cantidad = item.cantidad,
                precio = item.producto.precio
            )
        }

        val request = PedidoRequestDto(
            total = total,
            productos = detallesDto
        )

        val authToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
        return api.createPedido(authToken, request)
    }

    /**
     * CORRECCIÓN: Ahora devuelve el objeto Response completo.
     * Esto permite al ViewModel acceder a .isSuccessful, .code() y .body().
     */
    suspend fun getMyOrders(token: String): Response<List<PedidoResponseDto>> {
        val authToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
        return api.getMyPedidos(authToken)
    }

    /**
     * CORRECCIÓN: Ahora devuelve Response<Void> para poder verificar el código de error.
     */
    suspend fun cancelOrder(token: String, orderId: Long): Response<Void> {
        val authToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
        return api.cancelPedido(authToken, orderId)
    }
}