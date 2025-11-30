package com.huertohogar.app.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Respuesta para el LISTADO (filtro por ingrediente).
 */
data class RecipeListResponse(
    @SerializedName("meals") val meals: List<RecipeDto>?
)

/**
 * Objeto ligero para la lista (solo foto y nombre).
 */
data class RecipeDto(
    @SerializedName("idMeal") val id: String,
    @SerializedName("strMeal") val name: String,
    @SerializedName("strMealThumb") val imageUrl: String
)

// --- NUEVO: Modelos para el DETALLE ---

/**
 * Respuesta para el DETALLE (búsqueda por ID).
 */
data class RecipeDetailResponse(
    @SerializedName("meals") val meals: List<RecipeDetailDto>?
)

/**
 * Objeto completo con instrucciones.
 */
data class RecipeDetailDto(
    @SerializedName("idMeal") val id: String,
    @SerializedName("strMeal") val name: String,
    @SerializedName("strInstructions") val instructions: String,
    @SerializedName("strMealThumb") val imageUrl: String,
    @SerializedName("strArea") val area: String?, // Ej: "Chilean" (si hubiera), "Italian"
    @SerializedName("strCategory") val category: String? // Ej: "Beef"
)