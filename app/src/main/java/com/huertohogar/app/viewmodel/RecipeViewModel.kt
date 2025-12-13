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
import retrofit2.HttpException
import java.io.IOException

/**
 * Estado completo de la pantalla de Recetas.
 * Agrupa tanto la lista de resultados (vitrina) como el detalle de una receta seleccionada.
 */
data class RecipeUiState(
    val recipes: List<RecipeDto> = emptyList(),       // Lista de resumen para la búsqueda
    val selectedRecipe: RecipeDetailDto? = null,      // Detalle completo para la vista individual
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para el buscador de recetas.
 * Incluye manejo robusto de errores de red y HTTP.
 */
class RecipeViewModel : ViewModel() {

    // Repositorio encargado de la comunicación con la API de recetas.
    private val repository = RecipeRepository()

    // Gestión de Estado UI reactivo.
    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    // Carga inicial al abrir la pantalla.
    init {
        searchRecipes("Chicken")
    }

    /**
     * Busca recetas generales por ingrediente.
     * Maneja excepciones específicas para feedback preciso al usuario.
     */
    fun searchRecipes(ingredient: String) {
        viewModelScope.launch {
            // Limpiamos errores y selección previa al iniciar una nueva búsqueda.
            _uiState.update { it.copy(isLoading = true, error = null, selectedRecipe = null) }

            try {
                // Ahora repository.getRecipes lanzará excepciones si algo falla
                val result = repository.getRecipes(ingredient)
                _uiState.update {
                    it.copy(
                        recipes = result,
                        isLoading = false
                    )
                }
            } catch (e: IOException) {
                // Error de conexión (Sin internet, DNS, Timeout)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Sin conexión a internet. Verifique su red."
                    )
                }
            } catch (e: HttpException) {
                // Error del servidor (404, 500, etc.)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error del servidor (${e.code()}): ${e.message()}"
                    )
                }
            } catch (e: Exception) {
                // Error genérico
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Ocurrió un error inesperado al cargar recetas."
                    )
                }
            }
        }
    }

    /**
     * Obtiene los datos extendidos de una receta específica.
     */
    fun getRecipeDetail(id: String) {
        viewModelScope.launch {
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
                        it.copy(isLoading = false, error = "No se pudo encontrar el detalle de la receta.")
                    }
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Sin conexión a internet."
                    )
                }
            } catch (e: HttpException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al obtener detalle (${e.code()})."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Resetea la selección activa.
     */
    fun clearSelectedRecipe() {
        _uiState.update { it.copy(selectedRecipe = null) }
    }
}