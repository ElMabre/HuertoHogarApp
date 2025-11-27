package com.huertohogar.app.data.remote.api

import com.huertohogar.app.data.remote.model.AuthResponseDto
import com.huertohogar.app.data.remote.model.LoginRequestDto
import com.huertohogar.app.data.remote.model.ProductDto
import com.huertohogar.app.data.remote.model.RegisterRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface HuertoHogarApi {

    // --- Auth Controller ---

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    // --- Producto Controller ---

    @GET("api/productos")
    suspend fun getAllProductos(): Response<List<ProductDto>>

    @GET("api/productos/{sku}")
    suspend fun getProductoBySku(@Path("sku") sku: String): Response<ProductDto>
}