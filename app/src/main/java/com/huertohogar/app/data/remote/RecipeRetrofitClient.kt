package com.huertohogar.app.data.remote

import com.huertohogar.app.data.remote.api.RecipeApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente Retrofit dedicado exclusivamente a la API externa de Recetas.
 * Mantiene la configuración separada del backend principal de HuertoHogar.
 *
 * URL Base: https://www.themealdb.com/api/json/v1/1/
 */
object RecipeRetrofitClient {

    private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"

    // Creamos la instancia de la API de forma perezosa (lazy)
    // para que solo se inicialice cuando se necesite por primera vez.
    val api: RecipeApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecipeApi::class.java)
    }
}