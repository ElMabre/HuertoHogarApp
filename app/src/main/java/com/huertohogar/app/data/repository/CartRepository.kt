package com.huertohogar.app.data.repository

import com.huertohogar.app.data.local.dao.CartDao
import com.huertohogar.app.data.local.entity.CartEntity
import com.huertohogar.app.model.CartItem
import com.huertohogar.app.model.Producto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CartRepository(private val cartDao: CartDao) {

    // Obtiene los items y los convierte automáticamente de "Formato Base de Datos" a "Formato App"
    fun getCartItems(userId: Long): Flow<List<CartItem>> {
        return cartDao.getCartItems(userId).map { entities ->
            entities.map { entity ->
                CartItem(
                    producto = Producto(
                        id = entity.productId,
                        databaseId = entity.databaseId,
                        nombre = entity.nombre,
                        descripcion = entity.descripcion,
                        precio = entity.precio,
                        stock = entity.stock,
                        categoria = entity.categoria,
                        imagenUrl = entity.imagenUrl,
                        origen = entity.origen,
                        unidad = entity.unidad
                    ),
                    cantidad = entity.cantidad
                )
            }
        }
    }

    suspend fun addToCart(userId: Long, producto: Producto) {
        // Creamos la entidad para guardarla
        val itemEntity = CartEntity(
            userId = userId,
            productId = producto.id,
            nombre = producto.nombre,
            precio = producto.precio,
            imagenUrl = producto.imagenUrl,
            cantidad = 1, // Por defecto 1, luego manejamos la lógica de sumar si ya existe en el ViewModel
            descripcion = producto.descripcion,
            stock = producto.stock,
            categoria = producto.categoria,
            origen = producto.origen,
            unidad = producto.unidad,
            databaseId = producto.databaseId
        )
        // Ojo: Aquí la lógica simple es insertar.
        // En el ViewModel manejaremos si se suma +1 o se crea nuevo.
        // Pero como definimos en el DAO "OnConflictStrategy.REPLACE",
        // necesitamos gestionar la cantidad actual antes de insertar si queremos sumar.
        // Para simplificar, haremos la lógica de "verificar si existe" en el ViewModel
        // y aquí solo guardamos lo que nos manden.
        cartDao.insertCartItem(itemEntity)
    }

    // Función para guardar un item con cantidad específica (usada al sumar/restar)
    suspend fun updateCartItem(userId: Long, item: CartItem) {
        val entity = CartEntity(
            userId = userId,
            productId = item.producto.id,
            nombre = item.producto.nombre,
            precio = item.producto.precio,
            imagenUrl = item.producto.imagenUrl,
            cantidad = item.cantidad,
            descripcion = item.producto.descripcion,
            stock = item.producto.stock,
            categoria = item.producto.categoria,
            origen = item.producto.origen,
            unidad = item.producto.unidad,
            databaseId = item.producto.databaseId
        )
        cartDao.insertCartItem(entity)
    }

    suspend fun removeFromCart(userId: Long, productId: String) {
        cartDao.deleteCartItem(userId, productId)
    }

    suspend fun clearCart(userId: Long) {
        cartDao.clearCart(userId)
    }
}