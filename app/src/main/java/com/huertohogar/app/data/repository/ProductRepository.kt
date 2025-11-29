package com.huertohogar.app.data.repository

import com.huertohogar.app.data.remote.RetrofitClient
import com.huertohogar.app.data.remote.model.ProductDto
import com.huertohogar.app.model.Producto

class ProductRepository {

    // Usamos la API de catálogo configurada en RetrofitClient
    private val api = RetrofitClient.productApi

    suspend fun getAllProducts(): List<Producto> {
        try {
            val response = api.getAllProductos()
            if (response.isSuccessful && response.body() != null) {
                return response.body()!!.map { dto -> dto.toDomain() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    suspend fun getProductById(sku: String): Producto? {
        try {
            // Buscamos por SKU en la API
            val response = api.getProductoBySku(sku)
            if (response.isSuccessful && response.body() != null) {
                return response.body()!!.toDomain()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    // --- FUNCIÓN DE MAPEO ACTUALIZADA ---
    private fun ProductDto.toDomain(): Producto {
        return Producto(
            // 1. Mapeamos el SKU al campo 'id' (usado para navegación en la app)
            id = this.sku ?: "SIN_SKU",

            // 2. Mapeamos el ID numérico de la BD al nuevo campo 'databaseId' (usado para pedidos)
            databaseId = this.id ?: 0L,

            nombre = this.nombre ?: "Sin Nombre",
            descripcion = this.descripcion ?: "",
            precio = this.precio ?: 0.0,
            stock = this.stock ?: 0,
            categoria = this.categoria ?: "General",
            // Limpiamos la URL de espacios en blanco por seguridad
            imagenUrl = this.imagenUrl?.trim() ?: "",
            origen = this.origen ?: "Chile",
            unidad = this.unidad ?: "Unidad"
        )
    }
}