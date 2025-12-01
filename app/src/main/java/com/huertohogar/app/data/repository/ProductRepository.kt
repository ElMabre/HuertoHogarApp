package com.huertohogar.app.data.repository

import com.huertohogar.app.data.remote.RetrofitClient
import com.huertohogar.app.data.remote.model.ProductDto
import com.huertohogar.app.model.Producto

/**
 * Repositorio encargado de obtener productos desde el servicio remoto.
 * Provee funciones para listar productos y obtener detalles individuales.
 */
class ProductRepository {

    // API de catálogo configurada en RetrofitClient.
    private val api = RetrofitClient.productApi

    /**
     * Obtiene el listado completo de productos desde el backend.
     * Devuelve una lista vacía si ocurre un error en la solicitud.
     */
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

    /**
     * Obtiene un producto específico utilizando su SKU.
     * Retorna null si no se encuentra o si ocurre un error.
     */
    suspend fun getProductById(sku: String): Producto? {
        try {
            val response = api.getProductoBySku(sku)
            if (response.isSuccessful && response.body() != null) {
                return response.body()!!.toDomain()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Convierte un ProductDto recibido desde la API al modelo de dominio Producto.
     * Se asignan valores por defecto para evitar fallos por datos incompletos.
     */
    private fun ProductDto.toDomain(): Producto {
        return Producto(
            // Identificador de negocio utilizado para navegación.
            id = this.sku ?: "SIN_SKU",

            // Identificador técnico utilizado en operaciones como creación de pedidos.
            databaseId = this.id ?: 0L,

            nombre = this.nombre ?: "Sin Nombre",
            descripcion = this.descripcion ?: "",
            precio = this.precio ?: 0.0,
            stock = this.stock ?: 0,
            categoria = this.categoria ?: "General",

            // La URL se limpia para evitar errores por espacios en blanco.
            imagenUrl = this.imagenUrl?.trim() ?: "",

            origen = this.origen ?: "Chile",
            unidad = this.unidad ?: "Unidad"
        )
    }
}
