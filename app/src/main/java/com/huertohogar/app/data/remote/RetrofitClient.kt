package com.huertohogar.app.data.remote

import com.huertohogar.app.data.remote.api.HuertoHogarApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Cliente HTTP con timeout extendido
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // --- BASE URL ÚNICA (EC2 con Nginx) ---
    // Nginx recibe todo en el puerto 80 y redirige:
    // /api/auth -> Puerto 8081
    // /api/productos -> Puerto 8082
    // /api/pedidos -> Puerto 8083
    private const val BASE_URL = "http://18.211.31.168/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // --- Exposición de APIs ---
    // Como definimos todos los endpoints (Auth, Productos, Pedidos) en la misma interfaz HuertoHogarApi,
    // podemos reutilizar la misma instancia de Retrofit.

    val authApi: HuertoHogarApi = retrofit.create(HuertoHogarApi::class.java)

    val productApi: HuertoHogarApi = retrofit.create(HuertoHogarApi::class.java)

    // Descomentamos y habilitamos la API de pedidos
    val orderApi: HuertoHogarApi = retrofit.create(HuertoHogarApi::class.java)
}