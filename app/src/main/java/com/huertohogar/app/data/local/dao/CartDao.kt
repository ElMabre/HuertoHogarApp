package com.huertohogar.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.huertohogar.app.data.local.entity.CartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    // Obtiene todos los productos del carrito del usuario actual en tiempo real
    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    fun getCartItems(userId: Long): Flow<List<CartEntity>>

    // Inserta un producto. Si ya existe (mismo userId + productId), lo reemplaza (actualiza)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartEntity)

    // Elimina un producto específico del carrito del usuario
    @Query("DELETE FROM cart_items WHERE userId = :userId AND productId = :productId")
    suspend fun deleteCartItem(userId: Long, productId: String)

    // Vacía el carrito completo del usuario (al cerrar compra o cerrar sesión)
    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: Long)
}