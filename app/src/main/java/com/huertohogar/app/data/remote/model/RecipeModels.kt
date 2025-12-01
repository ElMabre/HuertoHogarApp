package com.huertohogar.app.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Respuesta del endpoint que devuelve una LISTA de recetas filtradas por ingrediente.
 * El API responde un arreglo dentro del campo "meals".
 */
data class RecipeListResponse(
    @SerializedName("meals") val meals: List<RecipeDto>?
)

/**
 * Modelo simple usado en la lista de recetas.
 * Este objeto viene sin instrucciones, solo datos básicos como:
 * - id
 * - nombre
 * - miniatura
 */
data class RecipeDto(
    @SerializedName("idMeal") val id: String,
    @SerializedName("strMeal") val name: String,
    @SerializedName("strMealThumb") val imageUrl: String
)

// Modelos usados para el DETALLE de la receta ---

/**
 * Respuesta del endpoint de detalle (lookup.php).
 * También devuelve un arreglo bajo el campo "meals", pero solo con una receta.
 */
data class RecipeDetailResponse(
    @SerializedName("meals") val meals: List<RecipeDetailDto>?
)

/**
 * DTO completo de una receta cuando el usuario abre un detalle.
 * Incluye:
 * - Instrucciones completas
 * - Imagen grande
 * - País de origen (area)
 * - Categoría (category)
 */
data class RecipeDetailDto(
    @SerializedName("idMeal") val id: String,
    @SerializedName("strMeal") val name: String,
    @SerializedName("strInstructions") val instructions: String,
    @SerializedName("strMealThumb") val imageUrl: String,
    @SerializedName("strArea") val area: String?,      // Ej: "Italian", "Japanese"
    @SerializedName("strCategory") val category: String? // Ej: "Beef", "Seafood"
)
