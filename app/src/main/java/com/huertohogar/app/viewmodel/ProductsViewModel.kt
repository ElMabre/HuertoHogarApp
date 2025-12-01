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
 * Estado que define lo que se muestra en la pantalla de Catálogo (Lista, Carga, Error).
 */
data class ProductsUiState(
    val productos: List<Producto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel para el Catálogo de Productos.
 * Gestiona la descarga y visualización de la lista completa.
 */
class ProductsViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorio para acceder a los datos de productos.
    private val repository = ProductRepository()

    // Estado UI reactivo (Privado para escribir, Público para leer).
    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    // Carga inicial automática al crear el ViewModel.
    init {
        cargarProductos()
    }

    // Función asíncrona para obtener la lista desde el servidor.
    fun cargarProductos() {
        viewModelScope.launch {
            // Mostramos 'Cargando' y limpiamos errores previos.
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Llamada al repositorio (Backend).
                // Las URLs de imágenes ya vienen limpias desde el repositorio.
                val lista = repository.getAllProducts()

                // Actualizamos el estado con la lista recibida.
                _uiState.update {
                    it.copy(
                        productos = lista,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                // Manejo de errores para no cerrar la app si falla la red.
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar productos: ${e.message}"
                    )
                }
            }
        }
    }
}