package com.huertohogar.app.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * DTO que representa un producto recibido desde el backend.
 * Este modelo es usado en:
 * - La lista de productos
 * - El detalle de un producto
 * - Los pedidos (microservicio necesita el ID numérico)
 */
data class ProductDto(

    // ID interno de la base de datos (Long).
    // Este ID es necesario para operaciones técnicas como crear un pedido.
    @SerializedName("id") val id: Long?,

    // SKU: identificador de negocio (String).
    // Se utiliza para navegación y para mostrar el producto en la UI.
    // Ejemplo: "FR001"
    @SerializedName("sku") val sku: String?,

    // Información básica del producto
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("precio") val precio: Double?,
    @SerializedName("stock") val stock: Int?,
    @SerializedName("categoria") val categoria: String?,

    // URL de la imagen almacenada en el backend
    @SerializedName("imagen") val imagenUrl: String?,

    // Datos adicionales del producto
    @SerializedName("origen") val origen: String?,
    @SerializedName("unidad") val unidad: String?
)
