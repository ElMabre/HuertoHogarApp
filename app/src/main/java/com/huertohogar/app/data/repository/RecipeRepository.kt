package com.huertohogar.app.data.repository

import com.huertohogar.app.data.remote.RecipeRetrofitClient
import com.huertohogar.app.data.remote.model.RecipeDetailDto
import com.huertohogar.app.data.remote.model.RecipeDto

/**
 * Repositorio encargado de obtener recetas desde la API externa.
 * Provee funciones para buscar por ingrediente y obtener detalles completos.
 */
class RecipeRepository {

    // Cliente Retrofit configurado para la API de recetas
    private val api = RecipeRetrofitClient.api

    /**
     * Obtiene una lista de recetas basadas en un ingrediente.
     * Retorna una lista vacía si ocurre algún error o no hay resultados.
     */
    suspend fun getRecipes(ingredient: String): List<RecipeDto> {
        return try {
            val response = api.getRecipesByIngredient(ingredient)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.meals ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Obtiene el detalle completo de una receta específica usando su ID.
     * La API devuelve una lista, pero solo se considera la primera receta encontrada.
     * Retorna null si falla la conexión o si no se encuentra la receta.
     */
    suspend fun getRecipeDetail(id: String): RecipeDetailDto? {
        return try {
            val response = api.getRecipeById(id)

            if (response.isSuccessful && response.body() != null) {
                val meals = response.body()!!.meals
                if (!meals.isNullOrEmpty()) {
                    meals[0] // Retorna la primera receta de la lista
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
