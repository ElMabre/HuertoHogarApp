package com.huertohogar.app.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos que representa un producto.
 * Usamos @SerializedName para mapear los nombres de campos que vienen del JSON (Backend Spring Boot)
 * a los nombres de variables que usamos en Kotlin.
 */
data class Producto(
    // El backend envía "sku" (ej: "FR001"), nosotros lo usamos como "id" en la navegación
    @SerializedName("sku")
    val id: String,

    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val stock: Int,
    val categoria: String,

    // El backend envía "imagen", nosotros usamos "imagenUrl"
    @SerializedName("imagen")
    val imagenUrl: String,

    val origen: String,
    val unidad: String
)