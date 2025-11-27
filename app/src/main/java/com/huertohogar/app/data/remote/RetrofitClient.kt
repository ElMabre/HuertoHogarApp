package com.huertohogar.app.data.remote

import com.huertohogar.app.data.remote.api.HuertoHogarApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Cliente HTTP base con tiempos de espera
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // --- Instancia para AUTH (Usuarios) en Puerto 8081 ---
    private val retrofitAuth: Retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8081/") // Puerto del ms-usuarios
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // --- Instancia para CATÁLOGO (Productos) en Puerto 8082 ---
    private val retrofitCatalogo: Retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8082/") // Puerto del ms-catalogo
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Exponemos las APIs correspondientes
    // 'authApi' se usará en AuthRepository
    val authApi: HuertoHogarApi = retrofitAuth.create(HuertoHogarApi::class.java)

    // 'productApi' se usará en ProductRepository (¡Esto es lo que te faltaba!)
    val productApi: HuertoHogarApi = retrofitCatalogo.create(HuertoHogarApi::class.java)
}