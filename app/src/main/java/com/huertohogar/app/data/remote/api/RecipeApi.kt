package com.huertohogar.app.data.remote.api

import com.huertohogar.app.data.remote.model.RecipeDetailResponse
import com.huertohogar.app.data.remote.model.RecipeListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interfaz que define los endpoints del API de recetas (TheMealDB).
 * Usamos Retrofit para hacer las peticiones HTTP.
 */
interface RecipeApi {

    // 1. LISTA: Buscar recetas según un ingrediente.
    @GET("filter.php")
    suspend fun getRecipesByIngredient(
        @Query("i") ingredient: String
    ): Response<RecipeListResponse>

    // 2. DETALLE: Obtiene los datos completos de una receta por su ID.
    // Este endpoint devuelve instrucciones, ingredientes detallados, imágenes, etc.
    @GET("lookup.php")
    suspend fun getRecipeById(
        @Query("i") id: String
    ): Response<RecipeDetailResponse>
}
