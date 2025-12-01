package com.huertohogar.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huertohogar.app.data.remote.model.RecipeDetailDto
import com.huertohogar.app.data.remote.model.RecipeDto
import com.huertohogar.app.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado completo de la pantalla de Recetas.
 * Agrupa tanto la lista de resultados (vitrina) como el detalle de una receta seleccionada.
 * Esto permite manejar toda la navegación y datos de esta sección en un solo lugar.
 */
data class RecipeUiState(
    val recipes: List<RecipeDto> = emptyList(),       // Lista de resumen para la búsqueda
    val selectedRecipe: RecipeDetailDto? = null,      // Detalle completo para la vista individual
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para el buscador de recetas.
 * Hereda de ViewModel estándar (no AndroidViewModel) porque no requiere Contexto.
 */
class RecipeViewModel : ViewModel() {

    // Repositorio encargado de la comunicación con la API de recetas (ej. TheMealDB).
    private val repository = RecipeRepository()

    // Gestión de Estado UI reactivo.
    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    // Carga inicial:
    // Al abrir la pantalla, buscamos algo por defecto para que no aparezca vacía.
    init {
        searchRecipes("Chicken")
    }

    /**
     * Busca recetas generales por ingrediente.
     * Actualiza la lista 'recipes' del estado para mostrar la vitrina.
     */
    fun searchRecipes(ingredient: String) {
        viewModelScope.launch {
            // Limpiamos errores y selección previa al iniciar una nueva búsqueda.
            _uiState.update { it.copy(isLoading = true, error = null, selectedRecipe = null) }

            try {
                val result = repository.getRecipes(ingredient)
                _uiState.update {
                    it.copy(
                        recipes = result,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar recetas: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Obtiene los datos extendidos de una receta específica (Instrucciones, medidas, etc.).
     * Se invoca al hacer clic en una tarjeta de la lista.
     */
    fun getRecipeDetail(id: String) {
        viewModelScope.launch {
            // Mantenemos la lista de fondo visible, pero mostramos carga.
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val detail = repository.getRecipeDetail(id)

                if (detail != null) {
                    _uiState.update {
                        it.copy(
                            selectedRecipe = detail,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "No se pudo cargar el detalle.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Error de conexión: ${e.message}")
                }
            }
        }
    }

    /**
     * Resetea la selección activa.
     * Vital para la navegación: al volver de la pantalla de detalle a la lista,
     * limpiamos el 'selectedRecipe' para evitar que se muestre brevemente al entrar a otra receta.
     */
    fun clearSelectedRecipe() {
        _uiState.update { it.copy(selectedRecipe = null) }
    }
}