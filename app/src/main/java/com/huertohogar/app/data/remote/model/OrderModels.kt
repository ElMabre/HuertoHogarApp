package com.huertohogar.app.data.remote.model

import com.google.gson.annotations.SerializedName
import com.huertohogar.app.model.Producto

/**
 * DTO que se envía al backend para crear un nuevo pedido.
 * Contiene el monto total del pedido y la lista de productos comprados.
 * Estructura JSON enviada:
 * {
 *   "total": 1000,
 *   "productos": [ ... ]
 * }
 */
data class PedidoRequestDto(
    @SerializedName("total") val total: Double,
    @SerializedName("productos") val productos: List<DetalleRequestDto>
)

/**
 * DTO que representa el detalle de cada producto dentro de un pedido.
 * Esto se ENVÍA al backend cuando el usuario compra:
 * - productoId -> ID del producto en la base de datos
 * - cantidad -> cuántas unidades se compran
 * - precio -> precio unitario en el momento de la compra
 */
data class DetalleRequestDto(
    @SerializedName("productoId") val productoId: Long,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("precio") val precio: Double
)

/**
 * DTO recibido desde el backend al crear un pedido o al consultar el historial.
 * Incluye datos generales del pedido + la lista de detalles comprados.
 */
data class PedidoResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("estado") val estado: String,
    @SerializedName("total") val total: Double,
    @SerializedName("metodoPago") val metodoPago: String?,
    @SerializedName("fecha") val fecha: String?,

    // Lista de productos comprados dentro del pedido.
    // Puede venir nulo si el backend no envía los detalles en algunos contextos.
    @SerializedName("detalles") val detalles: List<DetallePedidoResponseDto>? = null
)

/**
 * DTO que representa el detalle de un producto dentro de un pedido (RESPUESTA).
 * Incluye cantidad, precio unitario, e información completa del producto comprado.
 */
data class DetallePedidoResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("precioUnitario") val precioUnitario: Double,

    // Aquí se reutiliza tu modelo Producto para mostrar nombre, imagen, etc.
    @SerializedName("producto") val producto: Producto?
)
