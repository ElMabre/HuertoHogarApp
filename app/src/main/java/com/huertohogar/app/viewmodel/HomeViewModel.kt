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
 * ViewModel para la pantalla principal.
 * Usamos ViewModel estándar porque no necesitamos acceso al Contexto de la App.
 */
class HomeViewModel : ViewModel() {

    // Instancia simple del repositorio para obtener los datos.
    private val repository = ProductRepository()

    // Gestión del Estado:
    // _uiState es privado para editarlo aquí, uiState es público para leerlo en la UI.
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // El bloque init ejecuta la carga de datos automáticamente al abrir la pantalla.
    init {
        cargarProductosDestacados()
    }

    // Función asíncrona que usa una corrutina (viewModelScope) para no congelar la pantalla.
    private fun cargarProductosDestacados() {
        viewModelScope.launch {
            // Activamos el indicador de carga antes de pedir datos.
            _uiState.update { it.copy(isLoading = true) }

            try {
                val listaProductos = repository.getAllProducts()

                // Filtramos solo los primeros 5 productos para la sección "Destacados".
                val destacados = listaProductos.take(5)

                // Actualizamos el estado con la lista lista y apagamos la carga.
                _uiState.update {
                    it.copy(
                        productosDestacados = destacados,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                // Si falla, evitamos que la app se cierre y dejamos la lista vacía.
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
 * Clase de datos que agrupa todo lo que la pantalla necesita mostrar (Lista y estado de carga).
 */
data class HomeUiState(
    val productosDestacados: List<Producto> = emptyList(),
    val isLoading: Boolean = false
)