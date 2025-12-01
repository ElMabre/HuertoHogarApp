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

    // Base URL única (EC2 con Nginx).
    // Nginx distribuye:
    // /api/auth -> 8081
    // /api/productos -> 8082
    // /api/pedidos -> 8083
    private const val BASE_URL = "http://18.211.31.168/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Endpoints para autenticación, productos y pedidos utilizando la misma interfaz.
    val authApi: HuertoHogarApi = retrofit.create(HuertoHogarApi::class.java)
    val productApi: HuertoHogarApi = retrofit.create(HuertoHogarApi::class.java)
    val orderApi: HuertoHogarApi = retrofit.create(HuertoHogarApi::class.java)
}
