package com.huertohogar.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.huertohogar.app.data.local.datastorage.SessionManager
import com.huertohogar.app.data.repository.OrderRepository
import com.huertohogar.app.model.CartItem
import com.huertohogar.app.model.CartUiState
import com.huertohogar.app.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting

/**
 * ViewModel para gestionar el estado y la lógica del carrito de compras.
 */
class CartViewModel(application: Application) : AndroidViewModel(application) {

    // CAMBIO: Hacemos estas variables accesibles para los Tests
    @VisibleForTesting
    var sessionManager = SessionManager(application)

    @VisibleForTesting
    var orderRepository = OrderRepository()

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    // --- Funciones de Gestión del Carrito (Local) ---

    fun addToCart(producto: Producto) {
        _uiState.update { currentState ->
            val items = currentState.items.toMutableList()
            val existingItemIndex = items.indexOfFirst { it.producto.id == producto.id }

            if (existingItemIndex != -1) {
                val existingItem = items[existingItemIndex]
                // TODO: Validar stock máximo aquí en el futuro
                items[existingItemIndex] = existingItem.copy(cantidad = existingItem.cantidad + 1)
            } else {
                items.add(CartItem(producto = producto, cantidad = 1))
            }
            currentState.copy(items = items)
        }
    }

    fun removeFromCart(productId: String) {
        _uiState.update { currentState ->
            val items = currentState.items.filterNot { it.producto.id == productId }
            currentState.copy(items = items)
        }
    }

    fun updateQuantity(productId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(productId)
            return
        }
        _uiState.update { currentState ->
            val items = currentState.items.toMutableList()
            val itemIndex = items.indexOfFirst { it.producto.id == productId }

            if (itemIndex != -1) {
                items[itemIndex] = items[itemIndex].copy(cantidad = newQuantity)
            }
            currentState.copy(items = items)
        }
    }

    fun clearCart() {
        _uiState.update { it.copy(items = emptyList()) }
    }

    // --- Lógica de Checkout (Conexión con Backend) ---

    fun realizarPedido() {
        val currentState = _uiState.value
        if (currentState.items.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, checkoutError = null, checkoutSuccess = false) }

            try {
                // Obtenemos el token desde el SessionManager (que puede ser mockeado)
                val token = sessionManager.authToken.first()

                if (token.isNullOrBlank()) {
                    _uiState.update {
                        it.copy(isLoading = false, checkoutError = "Debes iniciar sesión para comprar.")
                    }
                    return@launch
                }

                // Llamamos al repositorio (que puede ser mockeado)
                val response = orderRepository.createOrder(
                    token = token,
                    cartItems = currentState.items,
                    total = currentState.total
                )

                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            checkoutSuccess = true,
                            items = emptyList()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            checkoutError = "Error al procesar: ${response.message()} (Código ${response.code()})"
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        checkoutError = "Error de conexión: ${e.message}"
                    )
                }
            }
        }
    }

    fun resetCheckoutStatus() {
        _uiState.update { it.copy(checkoutSuccess = false, checkoutError = null) }
    }
}