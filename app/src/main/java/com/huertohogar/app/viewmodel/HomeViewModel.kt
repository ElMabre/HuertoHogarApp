package com.huertohogar.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huertohogar.app.data.repository.ProductRepository
import com.huertohogar.app.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de inicio (HomeScreen).
 */
class HomeViewModel : ViewModel() {

    private val repository = ProductRepository()

    // Estado de la UI
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        cargarProductosDestacados()
    }

    private fun cargarProductosDestacados() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 1. Llamada al Repositorio
                // CORRECCIÓN: Usamos getAllProducts() que ya devuelve la lista limpia (con .trim() en las URLs)
                val listaProductos = repository.getAllProducts()

                // 2. Lógica de Negocio: Tomamos los primeros 5 como "Destacados"
                val destacados = listaProductos.take(5)

                // 3. Actualizamos la UI
                _uiState.update {
                    it.copy(
                        productosDestacados = destacados,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        productosDestacados = emptyList(),
                        isLoading = false
                    )
                }
            }
        }
    }
}

/**
 * Estado de la UI para HomeScreen.
 */
data class HomeUiState(
    val productosDestacados: List<Producto> = emptyList(),
    val isLoading: Boolean = false
)