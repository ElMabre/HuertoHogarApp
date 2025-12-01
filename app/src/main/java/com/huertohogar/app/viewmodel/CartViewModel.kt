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
 * ViewModel que actúa como única fuente de verdad para el carrito.
 * Hereda de AndroidViewModel para tener acceso al contexto (Application),
 * necesario aquí para inicializar SessionManager (DataStore) de forma sencilla.
 */
class CartViewModel(application: Application) : AndroidViewModel(application) {

    // Dependencias de datos y red.
    // Se marcan con @VisibleForTesting y var para poder inyectar "fakes" o mocks
    // en los tests unitarios sin usar un framework complejo de inyección de dependencias.
    @VisibleForTesting
    var sessionManager = SessionManager(application)

    @VisibleForTesting
    var orderRepository = OrderRepository()

    // Patrón de Estado UI (StateFlow).
    // _uiState es mutable y privado para que solo este ViewModel pueda modificarlo.
    // uiState es público y de solo lectura para que la Vista (UI) solo pueda "observar" cambios.
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    // --- Funciones de Gestión del Carrito (Local) ---

    // Lógica para añadir items.
    // Se usa .update para modificar el flujo de forma segura (hilo-segura).
    // Usamos .copy() porque el estado en Compose debe ser inmutable para que la UI se repinte correctamente.
    fun addToCart(producto: Producto) {
        _uiState.update { currentState ->
            val items = currentState.items.toMutableList()
            val existingItemIndex = items.indexOfFirst { it.producto.id == producto.id }

            if (existingItemIndex != -1) {
                val existingItem = items[existingItemIndex]
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

    // Actualización de cantidad con validación.
    // Centraliza la lógica: si la cantidad baja a 0, reutiliza la función de eliminar
    // para no duplicar código.
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

    // Operación asíncrona principal.
    // Usa viewModelScope para lanzar la corrutina, asegurando que si el usuario sale de la pantalla,
    // el proceso se cancele automáticamente para no consumir recursos.
    // Maneja los 3 estados de la UI: Cargando -> Éxito o Error.
    fun realizarPedido() {
        val currentState = _uiState.value
        if (currentState.items.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, checkoutError = null, checkoutSuccess = false) }

            try {
                // Flow.first() suspende la corrutina hasta obtener el valor actual del token
                // almacenado en disco (DataStore).
                val token = sessionManager.authToken.first()

                if (token.isNullOrBlank()) {
                    _uiState.update {
                        it.copy(isLoading = false, checkoutError = "Debes iniciar sesión para comprar.")
                    }
                    return@launch
                }

                // Llamada a red bloqueante (suspend function).
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
                            items = emptyList() // Limpiamos el carrito tras éxito
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

    // Limpieza de eventos de un solo uso.
    fun resetCheckoutStatus() {
        _uiState.update { it.copy(checkoutSuccess = false, checkoutError = null) }
    }
}