package com.huertohogar.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huertohogar.app.data.repository.ProductRepository
import com.huertohogar.app.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de inicio (HomeScreen).
 * Se encarga de la lógica de negocio y de exponer el estado a la UI.
 */
class HomeViewModel : ViewModel() {

    // Instanciamos el repositorio que ya sabe cómo hablar con la API (Puerto 8082)
    private val repository = ProductRepository()

    // _uiState es un flujo de datos que guarda el estado actual de la pantalla de inicio.
    private val _uiState = MutableStateFlow(HomeUiState())

    // uiState es la versión pública y de solo lectura del estado.
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * El bloque `init` se ejecuta automáticamente cuando se crea el ViewModel.
     */
    init {
        cargarProductosDestacados()
    }

    /**
     * Carga los productos desde el Backend y toma algunos como destacados.
     */
    private fun cargarProductosDestacados() {
        viewModelScope.launch {
            // Indicamos que estamos cargando
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // 1. Llamada al Repositorio (Backend)
                val listaDto = repository.getAllProductos()

                // 2. Mapeo: Convertimos de DTO (Red) a Producto (UI)
                val listaProductosUi = listaDto.map { dto ->
                    Producto(
                        id = dto.sku, // Usamos SKU para la navegación
                        nombre = dto.nombre,
                        descripcion = dto.descripcion,
                        precio = dto.precio.toDouble(),
                        stock = dto.stock,
                        categoria = dto.categoria ?: "General",
                        imagenUrl = dto.imagenUrl ?: "",
                        // Valores por defecto para campos que no vienen en el endpoint general
                        origen = "Chile",
                        unidad = "Unidad"
                    )
                }

                // 3. Lógica de Negocio: Tomamos los primeros 5 como "Destacados"
                val destacados = listaProductosUi.take(5)

                // 4. Actualizamos la UI
                _uiState.value = HomeUiState(
                    productosDestacados = destacados,
                    isLoading = false
                )

            } catch (e: Exception) {
                // En caso de error, dejamos la lista vacía y quitamos el loader
                e.printStackTrace()
                _uiState.value = HomeUiState(
                    productosDestacados = emptyList(),
                    isLoading = false
                )
            }
        }
    }
}

/**
 * Data class que representa el estado completo de la UI para HomeScreen.
 */
data class HomeUiState(
    val productosDestacados: List<Producto> = emptyList(),
    val isLoading: Boolean = false
)