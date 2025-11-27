package com.huertohogar.app.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * DTO ajustado a las columnas reales de tu MySQL.
 */
data class ProductDto(
    // LA CLAVE DEL ÉXITO:
    // Mapeamos el campo "sku" de la BD a la variable "id" de la App.
    @SerializedName("sku") val id: String?,

    // Mapeamos el campo "imagen" de la BD a la variable "imagenUrl" de la App.
    @SerializedName("imagen") val imagenUrl: String?,

    // Los demás campos suelen llamarse igual en BD y JSON
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("precio") val precio: Double?,
    @SerializedName("stock") val stock: Int?,
    @SerializedName("categoria") val categoria: String?,
    @SerializedName("origen") val origen: String?,
    @SerializedName("unidad") val unidad: String?
)