package com.huertohogar.app.data.repository

import com.huertohogar.app.data.remote.RetrofitClient
import com.huertohogar.app.data.remote.model.ProductDto
import retrofit2.Response

class ProductRepository {

    // Usamos específicamente la API configurada para el puerto 8082
    private val api = RetrofitClient.productApi

    suspend fun getAllProductos(): List<ProductDto> {
        val response = api.getAllProductos()
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            // Manejo básico de errores: devolvemos lista vacía o lanzamos excepción
            throw Exception("Error al obtener productos: ${response.code()}")
        }
    }

    suspend fun getProductoBySku(sku: String): ProductDto? {
        val response = api.getProductoBySku(sku)
        if (response.isSuccessful) {
            return response.body()
        } else {
            return null
        }
    }
}