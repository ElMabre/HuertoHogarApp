package com.huertohogar.app.model

/**
 * Modelo de dominio que representa un Producto en la UI de la App.
 */
data class Producto(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val stock: Int,
    val categoria: String,
    val imagenUrl: String,
    val origen: String,
    val unidad: String
)