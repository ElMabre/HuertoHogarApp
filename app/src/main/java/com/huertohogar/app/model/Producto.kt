package com.huertohogar.app.model

/**
 * Modelo de dominio que representa un Producto dentro de la UI de la aplicación.
 * Contiene toda la información necesaria para mostrarlo y procesarlo en el sistema.
 */
data class Producto(
    val id: String,

    val databaseId: Long,

    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val stock: Int,
    val categoria: String,
    val imagenUrl: String,
    val origen: String,
    val unidad: String
)