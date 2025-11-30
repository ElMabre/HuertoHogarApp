package com.huertohogar.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.huertohogar.app.data.local.datastorage.SessionManager
import com.huertohogar.app.data.remote.model.PedidoResponseDto
import com.huertohogar.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la UI para la pantalla de pedidos
data class OrderHistoryUiState(
    val isLoading: Boolean = false,
    val pedidos: List<PedidoResponseDto> = emptyList(),
    val error: String? = null,
    val message: String? = null // Para mensajes de éxito al cancelar
)

class OrderViewModel(application: Application) : AndroidViewModel(application) {

    private val orderRepository = OrderRepository()
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(OrderHistoryUiState())
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()

    init {
        loadMyOrders()
    }

    fun loadMyOrders() {
        _uiState.update { it.copy(isLoading = true, error = null, message = null) }

        viewModelScope.launch {
            try {
                val token = sessionManager.authToken.first()
                if (token.isNullOrEmpty()) {
                    _uiState.update { it.copy(isLoading = false, error = "No hay sesión activa") }
                    return@launch
                }

                val response = orderRepository.getMyOrders(token)

                if (response.isSuccessful && response.body() != null) {
                    val pedidosList = response.body()!!
                    // Ordenamos por ID descendente para ver el más reciente primero
                    _uiState.update { it.copy(isLoading = false, pedidos = pedidosList.sortedByDescending { p -> p.id }) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Error al cargar pedidos: ${response.code()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error de conexión: ${e.message}") }
            }
        }
    }

    fun cancelarPedido(idPedido: Long) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val token = sessionManager.authToken.first() ?: return@launch
                val response = orderRepository.cancelOrder(token, idPedido)

                if (response.isSuccessful) {
                    _uiState.update { it.copy(message = "Pedido cancelado exitosamente") }
                    // Recargamos la lista para que desaparezca el pedido (o cambie de estado)
                    loadMyOrders()
                } else {
                    val errorMsg = if (response.code() == 400) "No se puede cancelar un pedido ya procesado" else "Error al cancelar"
                    _uiState.update { it.copy(isLoading = false, error = errorMsg) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error de red") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}