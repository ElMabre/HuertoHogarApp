package com.huertohogar.app.data.repository

import com.huertohogar.app.data.remote.RecipeRetrofitClient
import com.huertohogar.app.data.remote.model.RecipeDetailDto
import com.huertohogar.app.data.remote.model.RecipeDto
import retrofit2.HttpException

/**
 * Repositorio encargado de obtener recetas desde la API externa.
 * Refactorizado para propagar excepciones al ViewModel en lugar de silenciarlas.
 */
class RecipeRepository {

    // Cliente Retrofit configurado para la API de recetas
    private val api = RecipeRetrofitClient.api

    /**
     * Obtiene una lista de recetas basadas en un ingrediente.
     * Si la red falla, lanzará IOException automáticamente.
     * Si el servidor devuelve error, lanzamos HttpException manualmente.
     */
    suspend fun getRecipes(ingredient: String): List<RecipeDto> {
        val response = api.getRecipesByIngredient(ingredient)

        if (response.isSuccessful) {
            // Si es exitoso, retornamos la lista o una vacía si es null
            return response.body()?.meals ?: emptyList()
        } else {
            // Si la respuesta no es 2xx, lanzamos la excepción para que el ViewModel la maneje
            throw HttpException(response)
        }
    }

    /**
     * Obtiene el detalle completo de una receta específica usando su ID.
     */
    suspend fun getRecipeDetail(id: String): RecipeDetailDto? {
        val response = api.getRecipeById(id)

        if (response.isSuccessful && response.body() != null) {
            val meals = response.body()!!.meals
            return if (!meals.isNullOrEmpty()) {
                meals[0] // Retorna la primera receta de la lista
            } else {
                null
            }
        } else {
            // Convertimos el error de respuesta en excepción
            throw HttpException(response)
        }
    }
}