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
 * Estado de la UI actualizado.
 * Ahora incluye 'selectedRecipe' para cuando el usuario hace clic en una foto.
 */
data class RecipeUiState(
    val recipes: List<RecipeDto> = emptyList(),       // Lista para la vitrina
    val selectedRecipe: RecipeDetailDto? = null,      // El detalle del plato seleccionado
    val isLoading: Boolean = false,
    val error: String? = null
)

class RecipeViewModel : ViewModel() {

    private val repository = RecipeRepository()

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    init {
        // Carga inicial por defecto
        searchRecipes("Chicken")
    }

    /**
     * Busca lista de recetas (Vitrina).
     */
    fun searchRecipes(ingredient: String) {
        viewModelScope.launch {
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
     * Busca el detalle de UNA receta específica por su ID.
     * Se llama cuando el usuario hace clic en una tarjeta.
     */
    fun getRecipeDetail(id: String) {
        viewModelScope.launch {
            // Mantenemos la lista de fondo, pero activamos carga
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
     * Limpia la receta seleccionada.
     * Útil para cuando volvemos de la pantalla de detalle a la lista, para que no se quede "pegada" la anterior.
     */
    fun clearSelectedRecipe() {
        _uiState.update { it.copy(selectedRecipe = null) }
    }
}