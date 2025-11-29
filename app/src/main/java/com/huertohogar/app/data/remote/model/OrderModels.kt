package com.huertohogar.app.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * DTO para enviar la solicitud de un nuevo pedido al backend.
 * Estructura: { "total": 1000, "productos": [ ... ] }
 */
data class PedidoRequestDto(
    @SerializedName("total") val total: Double,
    @SerializedName("productos") val productos: List<DetalleRequestDto>
)

/**
 * DTO para el detalle de cada producto dentro del pedido.
 * Nota: El backend espera el ID numérico del producto (Long), no el SKU.
 */
data class DetalleRequestDto(
    @SerializedName("productoId") val productoId: Long,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("precio") val precio: Double
)

/**
 * DTO para recibir la confirmación del pedido desde el backend.
 */
data class PedidoResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("estado") val estado: String,
    @SerializedName("total") val total: Double,
    @SerializedName("metodoPago") val metodoPago: String,
    // La fecha viene como string (ej: "2023-11-24")
    @SerializedName("fecha") val fecha: String?
)