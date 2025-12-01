package com.huertohogar.app.data.repository

import com.huertohogar.app.data.remote.RetrofitClient
import com.huertohogar.app.data.remote.model.DetalleRequestDto
import com.huertohogar.app.data.remote.model.PedidoRequestDto
import com.huertohogar.app.data.remote.model.PedidoResponseDto
import com.huertohogar.app.model.CartItem
import retrofit2.Response

/**
 * Repositorio encargado de gestionar las operaciones relacionadas con pedidos.
 */
class OrderRepository {

    private val api = RetrofitClient.orderApi

    /**
     * Crea un pedido a partir de los productos del carrito.
     * Prepara la lista de detalles y envía la solicitud al servidor.
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
     * Obtiene los pedidos del usuario autenticado.
     * El Response completo permite revisar el estado HTTP y la data recibida.
     */
    suspend fun getMyOrders(token: String): Response<List<PedidoResponseDto>> {
        val authToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
        return api.getMyPedidos(authToken)
    }

    /**
     * Solicita la cancelación de un pedido por su ID.
     * Retorna un Response<Void> para evaluar el código de respuesta del servidor.
     */
    suspend fun cancelOrder(token: String, orderId: Long): Response<Void> {
        val authToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
        return api.cancelPedido(authToken, orderId)
    }
}
