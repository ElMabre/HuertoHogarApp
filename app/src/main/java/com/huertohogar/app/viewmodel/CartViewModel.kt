package com.huertohogar.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.huertohogar.app.data.local.database.AppDatabase
import com.huertohogar.app.data.local.datastorage.SessionManager
import com.huertohogar.app.data.repository.CartRepository
import com.huertohogar.app.data.repository.OrderRepository
import com.huertohogar.app.model.CartUiState
import com.huertohogar.app.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting
import retrofit2.HttpException
import java.io.IOException

class CartViewModel(
    application: Application,
    // Dependencias inyectables en el constructor primario para Testing
    private val sessionManagerProvider: SessionManager,
    private val cartRepositoryProvider: CartRepository,
    private val orderRepositoryProvider: OrderRepository
) : AndroidViewModel(application) {

    // Constructor secundario usado por la App (AndroidViewModelFactory)
    constructor(application: Application) : this(
        application,
        SessionManager(application),
        CartRepository(AppDatabase.getDatabase(application).cartDao()),
        OrderRepository()
    )

    // Propiedades públicas para acceso si es necesario, inicializadas desde los parámetros
    @VisibleForTesting
    var sessionManager = sessionManagerProvider

    @VisibleForTesting
    var cartRepository = cartRepositoryProvider

    @VisibleForTesting
    var orderRepository = orderRepositoryProvider

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private var currentUserId: Long? = null

    init {
        viewModelScope.launch {
            // Ahora usa la instancia inyectada correctamente desde el inicio
            sessionManager.userId.collectLatest { userId ->
                currentUserId = userId
                if (userId != null) {
                    cartRepository.getCartItems(userId).collect { items ->
                        _uiState.update {
                            it.copy(
                                items = items,
                                isLoading = false
                            )
                        }
                    }
                } else {
                    _uiState.update { it.copy(items = emptyList()) }
                }
            }
        }
    }

    // --- Funciones de Gestión del Carrito ---

    fun addToCart(producto: Producto) {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            val existingItem = _uiState.value.items.find { it.producto.id == producto.id }

            if (existingItem != null) {
                val newItem = existingItem.copy(cantidad = existingItem.cantidad + 1)
                cartRepository.updateCartItem(userId, newItem)
            } else {
                cartRepository.addToCart(userId, producto)
            }
        }
    }

    fun removeFromCart(productId: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            cartRepository.removeFromCart(userId, productId)
        }
    }

    fun updateQuantity(productId: String, newQuantity: Int) {
        val userId = currentUserId ?: return
        if (newQuantity <= 0) {
            removeFromCart(productId)
            return
        }
        viewModelScope.launch {
            val currentItem = _uiState.value.items.find { it.producto.id == productId }
            if (currentItem != null) {
                cartRepository.updateCartItem(userId, currentItem.copy(cantidad = newQuantity))
            }
        }
    }

    fun clearCart() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            cartRepository.clearCart(userId)
        }
    }

    // --- Checkout (Realizar Pedido) ---

    fun realizarPedido() {
        val currentState = _uiState.value
        val userId = currentUserId

        if (currentState.items.isEmpty() || userId == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, checkoutError = null, checkoutSuccess = false) }

            try {
                val token = sessionManager.authToken.first()
                if (token.isNullOrBlank()) {
                    _uiState.update { it.copy(isLoading = false, checkoutError = "Sesión no válida") }
                    return@launch
                }

                val response = orderRepository.createOrder(
                    token = token,
                    cartItems = currentState.items,
                    total = currentState.total
                )

                if (response.isSuccessful) {
                    cartRepository.clearCart(userId)
                    _uiState.update {
                        it.copy(isLoading = false, checkoutSuccess = true, items = emptyList())
                    }
                } else {
                    val errorMsg = "Error al crear pedido: ${response.code()}"
                    _uiState.update {
                        it.copy(isLoading = false, checkoutError = errorMsg)
                    }
                }

            } catch (e: IOException) {
                _uiState.update {
                    it.copy(isLoading = false, checkoutError = "Sin conexión a internet")
                }
            } catch (e: HttpException) {
                _uiState.update {
                    it.copy(isLoading = false, checkoutError = "Error del servidor: ${e.message()}")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, checkoutError = "Error inesperado: ${e.message}")
                }
            }
        }
    }

    fun resetCheckoutStatus() {
        _uiState.update { it.copy(checkoutSuccess = false, checkoutError = null) }
    }
}