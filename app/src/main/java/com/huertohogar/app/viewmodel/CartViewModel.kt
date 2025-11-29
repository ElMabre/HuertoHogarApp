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

/**
 * ViewModel para gestionar el estado y la lógica del carrito de compras.
 * Ahora incluye la lógica para confirmar el pedido (Checkout) conectándose al backend.
 */
class CartViewModel(application: Application) : AndroidViewModel(application) {

    // Dependencias necesarias
    private val sessionManager = SessionManager(application)
    private val orderRepository = OrderRepository()

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

    /**
     * Inicia el proceso de compra enviando los datos al servidor.
     */
    fun realizarPedido() {
        val currentState = _uiState.value
        if (currentState.items.isEmpty()) return

        viewModelScope.launch {
            // 1. Iniciar carga
            _uiState.update { it.copy(isLoading = true, checkoutError = null, checkoutSuccess = false) }

            try {
                // 2. Obtener Token (necesario para el endpoint protegido)
                val token = sessionManager.authToken.first()

                if (token.isNullOrBlank()) {
                    _uiState.update {
                        it.copy(isLoading = false, checkoutError = "Debes iniciar sesión para comprar.")
                    }
                    return@launch
                }

                // 3. Llamar al repositorio
                // Usamos el total calculado en el UI State (incluye envío si aplica)
                val response = orderRepository.createOrder(
                    token = token,
                    cartItems = currentState.items,
                    total = currentState.total
                )

                if (response.isSuccessful) {
                    // 4. Éxito: Limpiamos el carrito y marcamos success
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            checkoutSuccess = true,
                            items = emptyList() // Vaciamos el carrito local
                        )
                    }
                } else {
                    // 5. Error del servidor (ej: sin stock)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            checkoutError = "Error al procesar: ${response.message()} (Código ${response.code()})"
                        )
                    }
                }

            } catch (e: Exception) {
                // 6. Error de red o inesperado
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        checkoutError = "Error de conexión: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Resetea los estados de éxito/error para que no se muestren diálogos repetidos al volver a la pantalla.
     */
    fun resetCheckoutStatus() {
        _uiState.update { it.copy(checkoutSuccess = false, checkoutError = null) }
    }
}