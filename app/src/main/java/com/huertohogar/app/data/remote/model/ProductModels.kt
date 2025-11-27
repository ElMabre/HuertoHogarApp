package com.huertohogar.app.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de Producto que viene de la API.
 * Coincide con Producto.java del backend.
 */
data class ProductDto(
    @SerializedName("id") val id: Long, // Aunque uses SKU para navegar, el backend devuelve ID
    @SerializedName("sku") val sku: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("precio") val precio: Int,
    @SerializedName("stock") val stock: Int,
    @SerializedName("imagenUrl") val imagenUrl: String?,
    @SerializedName("categoria") val categoria: String?
)