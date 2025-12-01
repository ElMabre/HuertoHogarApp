package com.huertohogar.app.data.remote

import com.huertohogar.app.data.remote.api.RecipeApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente Retrofit dedicado a la API externa de recetas.
 * Mantiene la configuración separada del backend principal de HuertoHogar.
 *
 * Base URL: https://www.themealdb.com/api/json/v1/1/
 */
object RecipeRetrofitClient {

    private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"

    // Instancia de la API creada de forma diferida para inicializarse solo cuando se utilice.
    val api: RecipeApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecipeApi::class.java)
    }
}
