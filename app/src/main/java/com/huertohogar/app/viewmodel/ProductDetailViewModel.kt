package com.huertohogar.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.huertohogar.app.data.repository.ProductRepository
import com.huertohogar.app.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de la UI para el detalle del producto.
 */
data class ProductDetailUiState(
    val producto: Producto? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class ProductDetailViewModel(application: Application) : AndroidViewModel(application) {

    // 1. Instanciamos el repositorio (el mismo que usamos en Home y Catálogo)
    private val repository = ProductRepository()

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    /**
     * Carga los detalles usando la API (Backend).
     */
    fun loadProductDetails(productId: String?) {
        if (productId == null) {
            _uiState.update { it.copy(error = "ID inválido", isLoading = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // 2. Llamada real al servidor (Puerto 8082)
                // Esta función ya aplica el .trim() a la imagen si es necesario
                val productoEncontrado = repository.getProductById(productId)

                if (productoEncontrado != null) {
                    _uiState.update {
                        it.copy(
                            producto = productoEncontrado,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            error = "El producto no existe o fue eliminado.",
                            isLoading = false
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error de conexión: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
}