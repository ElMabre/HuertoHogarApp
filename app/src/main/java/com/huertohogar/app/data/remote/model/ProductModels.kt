package com.huertohogar.app.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * DTO ajustado para recibir tanto el ID numérico (para pedidos) como el SKU (para navegación).
 */
data class ProductDto(
    // ID técnico de la base de datos (Long). Vital para el microservicio de pedidos.
    @SerializedName("id") val id: Long?,

    // ID de negocio (String). Lo usamos para la navegación (ej: /producto/FR001).
    @SerializedName("sku") val sku: String?,

    @SerializedName("nombre") val nombre: String?,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("precio") val precio: Double?,
    @SerializedName("stock") val stock: Int?,
    @SerializedName("categoria") val categoria: String?,
    @SerializedName("imagen") val imagenUrl: String?,
    @SerializedName("origen") val origen: String?,
    @SerializedName("unidad") val unidad: String?
)