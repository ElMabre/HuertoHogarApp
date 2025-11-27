package com.huertohogar.app.data.repository

import com.huertohogar.app.data.remote.RetrofitClient
import com.huertohogar.app.data.remote.model.ProductDto
import com.huertohogar.app.model.Producto

class ProductRepository {

    private val api = RetrofitClient.productApi

    suspend fun getAllProducts(): List<Producto> {
        val response = api.getAllProductos()
        if (response.isSuccessful && response.body() != null) {
            // Convertimos la lista
            return response.body()!!.map { dto -> dto.toDomain() }
        }
        return emptyList()
    }

    suspend fun getProductById(id: String): Producto? {
        val response = api.getProductoBySku(id)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.toDomain()
        }
        return null
    }

    // --- FUNCIÓN DE MAPEO SEGURA ---
    private fun ProductDto.toDomain(): Producto {
        return Producto(
            // Si el ID viene nulo, usamos "SIN_ID" para que no se caiga
            id = this.id ?: "SIN_ID",

            // Si el nombre viene nulo, ponemos "Sin Nombre"
            nombre = this.nombre ?: "Sin Nombre",

            descripcion = this.descripcion ?: "",

            // Si el precio viene nulo, ponemos 0.0
            precio = this.precio ?: 0.0,

            stock = this.stock ?: 0,

            categoria = this.categoria ?: "General",

            // Manejo de imagen (quitamos espacios y nulos)
            imagenUrl = this.imagenUrl?.trim() ?: "",

            origen = this.origen ?: "Chile",
            unidad = this.unidad ?: "Unidad"
        )
    }
}