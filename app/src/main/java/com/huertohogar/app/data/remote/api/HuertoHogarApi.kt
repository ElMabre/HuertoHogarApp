package com.huertohogar.app.data.remote.api

import com.huertohogar.app.data.remote.model.AuthResponseDto
import com.huertohogar.app.data.remote.model.LoginRequestDto
import com.huertohogar.app.data.remote.model.ProductDto
import com.huertohogar.app.data.remote.model.RegisterRequestDto
import com.huertohogar.app.data.remote.model.UserUpdateDto
import com.huertohogar.app.data.remote.model.UsuarioDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface HuertoHogarApi {

    // --- Auth Controller ---

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    // --- Usuario Controller (Nuevo Endpoint para actualizar perfil) ---

    @PUT("api/usuarios/perfil")
    suspend fun updateProfile(
        @Header("Authorization") token: String, // El token va en la cabecera (Header)
        @Body request: UserUpdateDto
    ): Response<UsuarioDto>

    // --- Producto Controller ---

    @GET("api/productos")
    suspend fun getAllProductos(): Response<List<ProductDto>>

    @GET("api/productos/{sku}")
    suspend fun getProductoBySku(@Path("sku") sku: String): Response<ProductDto>
}