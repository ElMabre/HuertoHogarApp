package com.huertohogar.app.data.repository

import com.huertohogar.app.data.remote.RecipeRetrofitClient
import com.huertohogar.app.data.remote.model.RecipeDetailDto
import com.huertohogar.app.data.remote.model.RecipeDto

class RecipeRepository {

    private val api = RecipeRetrofitClient.api

    /**
     * Obtiene una lista de recetas basadas en un ingrediente.
     * (Ya teníamos esta función, la mantenemos igual).
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
     * Retorna null si falla la conexión o si la receta no existe.
     */
    suspend fun getRecipeDetail(id: String): RecipeDetailDto? {
        return try {
            // Llamamos al nuevo endpoint de "lookup"
            val response = api.getRecipeById(id)

            if (response.isSuccessful && response.body() != null) {
                // La API devuelve una lista, pero como buscamos por ID,
                // solo nos interesa el primer elemento (si existe).
                val meals = response.body()!!.meals
                if (!meals.isNullOrEmpty()) {
                    meals[0] // Retornamos la primera y única receta encontrada
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