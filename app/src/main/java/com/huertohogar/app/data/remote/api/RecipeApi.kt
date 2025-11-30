package com.huertohogar.app.data.remote.api

import com.huertohogar.app.data.remote.model.RecipeDetailResponse
import com.huertohogar.app.data.remote.model.RecipeListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interfaz actualizada con el endpoint para obtener detalles.
 */
interface RecipeApi {

    // 1. LISTA: Buscar por ingrediente (Ya lo teníamos)
    @GET("filter.php")
    suspend fun getRecipesByIngredient(
        @Query("i") ingredient: String
    ): Response<RecipeListResponse>

    // 2. DETALLE: Buscar receta por ID (NUEVO)
    // Usamos este endpoint para obtener las instrucciones completas cuando el usuario hace clic.
    // Ejemplo: .../lookup.php?i=52772
    @GET("lookup.php")
    suspend fun getRecipeById(
        @Query("i") id: String
    ): Response<RecipeDetailResponse>
}