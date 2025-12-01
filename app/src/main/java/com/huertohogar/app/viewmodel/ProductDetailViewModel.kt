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
 * Agrupa el estado de la pantalla de Detalle (Producto, Carga, Error).
 * Permite pasar todos los datos a la Vista en un solo objeto inmutable.
 */
data class ProductDetailUiState(
    val producto: Producto? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel para gestionar la lógica de visualización de un producto específico.
 * Hereda de AndroidViewModel para mantener consistencia con el resto de la App.
 */
class ProductDetailViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorio para obtener los datos (el mismo usado en otras pantallas).
    private val repository = ProductRepository()

    // Patrón de Estado (StateFlow).
    // _uiState es privado y editable; uiState es público y de solo lectura para la vista.
    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    /**
     * Función principal para cargar el producto desde la API.
     * Se llama al abrir la pantalla, recibiendo el ID pasado por navegación.
     */
    fun loadProductDetails(productId: String?) {
        // Validación inicial: Si el ID viene nulo por algún error de navegación, paramos aquí.
        if (productId == null) {
            _uiState.update { it.copy(error = "ID inválido", isLoading = false) }
            return
        }

        // Usamos una corrutina (hilo secundario) para no bloquear la UI mientras descargamos datos.
        viewModelScope.launch {
            // Indicamos a la UI que muestre el spinner de carga.
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Llamada al servidor. Esta operación puede tardar, por eso estamos en una corrutina.
                val productoEncontrado = repository.getProductById(productId)

                if (productoEncontrado != null) {
                    // Si encontramos el producto, actualizamos el estado con los datos reales.
                    _uiState.update {
                        it.copy(
                            producto = productoEncontrado,
                            isLoading = false
                        )
                    }
                } else {
                    // El servidor respondió, pero el producto no existe (ej. ID antiguo).
                    _uiState.update {
                        it.copy(
                            error = "El producto no existe o fue eliminado.",
                            isLoading = false
                        )
                    }
                }

            } catch (e: Exception) {
                // Capturamos fallos de red o servidor para que la app no se cierre.
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