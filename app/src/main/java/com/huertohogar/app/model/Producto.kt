package com.huertohogar.app.model

/**
 * Modelo de dominio que representa un Producto en la UI de la App.
 */
data class Producto(
    // Mantenemos 'id' como el SKU (String) para la navegación y compatibilidad con la UI actual.
    val id: String,

    // NUEVO: ID interno de la Base de Datos (Long).
    // Es fundamental guardarlo aquí porque el endpoint de 'Crear Pedido' exige IDs numéricos, no SKUs.
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