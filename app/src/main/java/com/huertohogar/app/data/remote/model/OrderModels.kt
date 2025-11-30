package com.huertohogar.app.data.remote.model

import com.google.gson.annotations.SerializedName
import com.huertohogar.app.model.Producto

/**
 * DTO para enviar la solicitud de un nuevo pedido al backend.
 * Estructura: { "total": 1000, "productos": [ ... ] }
 */
data class PedidoRequestDto(
    @SerializedName("total") val total: Double,
    @SerializedName("productos") val productos: List<DetalleRequestDto>
)

/**
 * DTO para el detalle de cada producto dentro del pedido (ENVÍO).
 */
data class DetalleRequestDto(
    @SerializedName("productoId") val productoId: Long,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("precio") val precio: Double
)

/**
 * DTO para recibir la respuesta del backend (Sirve para Crear y para Historial).
 * Agregamos la lista de 'detalles' para poder ver qué compramos en el historial.
 */
data class PedidoResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("estado") val estado: String,
    @SerializedName("total") val total: Double,
    @SerializedName("metodoPago") val metodoPago: String?,
    @SerializedName("fecha") val fecha: String?,

    // --- NUEVO: Lista de productos comprados (puede venir nulo en algunos casos)
    @SerializedName("detalles") val detalles: List<DetallePedidoResponseDto>? = null
)

/**
 * DTO para recibir el detalle de un producto ya comprado (RESPUESTA).
 */
data class DetallePedidoResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("precioUnitario") val precioUnitario: Double,
    // Reutilizamos tu modelo de Producto existente para mostrar nombre e imagen
    @SerializedName("producto") val producto: Producto?
)