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
import org.jetbrains.annotations.VisibleForTesting

/**
 * Estado que agrupa los datos de la pantalla de Historial (Loading, Lista, Errores).
 */
data class OrderHistoryUiState(
    val isLoading: Boolean = false,
    val pedidos: List<PedidoResponseDto> = emptyList(),
    val error: String? = null,
    val message: String? = null // Mensaje temporal (ej: "Cancelado con éxito")
)

/**
 * ViewModel para listar los pedidos del usuario y gestionar cancelaciones.
 * Usa AndroidViewModel para acceder al Contexto (SessionManager).
 */
class OrderViewModel(application: Application) : AndroidViewModel(application) {

    // Dependencias accesibles para Testing (Mocks).
    @VisibleForTesting
    var orderRepository = OrderRepository()

    @VisibleForTesting
    var sessionManager = SessionManager(application)

    // Gestión de Estado UI (Mutable interno, Inmutable público).
    private val _uiState = MutableStateFlow(OrderHistoryUiState())
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()

    // Carga inicial automática al entrar a la pantalla.
    init {
        loadMyOrders()
    }

    // Obtiene la lista de pedidos del servidor.
    // Maneja estados de carga y errores de sesión o red.
    fun loadMyOrders() {
        // Limpiamos mensajes previos para evitar confusión visual al recargar.
        _uiState.update { it.copy(isLoading = true, error = null, message = null) }

        viewModelScope.launch {
            try {
                // Verificamos token antes de llamar a la API.
                val token = sessionManager.authToken.first()
                if (token.isNullOrEmpty()) {
                    _uiState.update { it.copy(isLoading = false, error = "No hay sesión activa") }
                    return@launch
                }

                val response = orderRepository.getMyOrders(token)

                if (response.isSuccessful && response.body() != null) {
                    val pedidosList = response.body()!!
                    // Orden descendente por ID para mostrar lo más nuevo arriba.
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pedidos = pedidosList.sortedByDescending { p -> p.id }
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Error al cargar pedidos: ${response.code()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error de conexión: ${e.message}") }
            }
        }
    }

    // Cancela un pedido y actualiza la lista.
    // Sincroniza la recarga de datos con el mensaje de éxito.
    fun cancelarPedido(idPedido: Long) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val token = sessionManager.authToken.first() ?: return@launch
                val response = orderRepository.cancelOrder(token, idPedido)

                if (response.isSuccessful) {
                    // Primero actualizamos la lista para reflejar el cambio de estado en el servidor.
                    loadMyOrders()

                    // Luego mostramos el mensaje. Si lo hacemos antes, loadMyOrders() lo borraría.
                    _uiState.update { it.copy(message = "Pedido cancelado exitosamente") }

                } else {
                    // Mensajes de error amigables según el código HTTP.
                    val errorMsg = if (response.code() == 400) "No se puede cancelar un pedido ya procesado" else "Error al cancelar"
                    _uiState.update { it.copy(isLoading = false, error = errorMsg) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error de red") }
            }
        }
    }

    // Limpia mensajes de éxito/error una vez vistos por el usuario (ej: al cerrar un Snackbar).
    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}