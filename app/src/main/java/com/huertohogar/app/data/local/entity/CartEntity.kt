package com.huertohogar.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // ID interno de Room (autogenerado)

    val userId: Long,
    val productId: String, // ID del producto (String, ej: "prod01")
    val databaseId: Long,  // ID numérico del producto original

    val nombre: String,
    val descripcion: String,
    val precio: Double,    // CORRECCIÓN: Debe ser Double, no String
    val stock: Int,
    val categoria: String,
    val imagenUrl: String,
    val origen: String,    // Campo nuevo requerido
    val unidad: String,    // Campo nuevo requerido

    val cantidad: Int
)