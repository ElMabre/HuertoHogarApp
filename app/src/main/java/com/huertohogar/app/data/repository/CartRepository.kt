package com.huertohogar.app.data.repository

import com.huertohogar.app.data.local.dao.CartDao
import com.huertohogar.app.data.local.entity.CartEntity
import com.huertohogar.app.model.CartItem
import com.huertohogar.app.model.Producto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CartRepository(private val cartDao: CartDao) {

    fun getCartItems(userId: Long): Flow<List<CartItem>> {
        return cartDao.getCartItems(userId).map { entities ->
            entities.map { entity ->
                CartItem(
                    producto = Producto(
                        id = entity.productId,
                        databaseId = entity.databaseId,
                        nombre = entity.nombre,
                        descripcion = entity.descripcion,
                        precio = entity.precio, // Ahora ambos son Double, no habrá error
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
        val itemEntity = CartEntity(
            userId = userId,
            productId = producto.id,
            databaseId = producto.databaseId,
            nombre = producto.nombre,
            descripcion = producto.descripcion,
            precio = producto.precio, // Pasamos Double a Double
            stock = producto.stock,
            categoria = producto.categoria,
            imagenUrl = producto.imagenUrl,
            origen = producto.origen,
            unidad = producto.unidad,
            cantidad = 1
        )
        cartDao.insertCartItem(itemEntity)
    }

    suspend fun updateCartItem(userId: Long, item: CartItem) {
        val entity = CartEntity(
            userId = userId,
            productId = item.producto.id,
            databaseId = item.producto.databaseId,
            nombre = item.producto.nombre,
            descripcion = item.producto.descripcion,
            precio = item.producto.precio,
            stock = item.producto.stock,
            categoria = item.producto.categoria,
            imagenUrl = item.producto.imagenUrl,
            origen = item.producto.origen,
            unidad = item.producto.unidad,
            cantidad = item.cantidad
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