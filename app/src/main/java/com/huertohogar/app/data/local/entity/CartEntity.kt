package com.huertohogar.app.data.local.entity

import androidx.room.Entity

/**
 * Entidad que representa un ítem del carrito en la base de datos local.
 * Usamos una clave primaria compuesta por userId y productId para que:
 * 1. Cada usuario tenga su propio carrito (separado por userId).
 * 2. Un mismo producto no se repita en la lista del mismo usuario (se agrupa).
 */
@Entity(
    tableName = "cart_items",
    primaryKeys = ["userId", "productId"]
)
data class CartEntity(
    val userId: Long,       // ID del usuario logueado (SessionManager)
    val productId: String,  // ID del producto (Backend)

    // Guardamos los detalles del producto para poder mostrar el carrito offline
    // sin tener que consultar a la API de nuevo.
    val nombre: String,
    val precio: Double,
    val imagenUrl: String,
    val cantidad: Int,

    // Campos adicionales necesarios para reconstruir el objeto Producto completo
    val descripcion: String,
    val stock: Int,
    val categoria: String,
    val origen: String,
    val unidad: String,
    val databaseId: Long // ID numérico del backend
)