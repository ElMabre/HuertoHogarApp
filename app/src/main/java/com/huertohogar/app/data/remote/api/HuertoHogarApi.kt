package com.huertohogar.app.data.remote.api

import com.huertohogar.app.data.remote.model.AuthResponseDto
import com.huertohogar.app.data.remote.model.LoginRequestDto
import com.huertohogar.app.data.remote.model.PedidoRequestDto
import com.huertohogar.app.data.remote.model.PedidoResponseDto
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

    // --- Usuario Controller ---

    @PUT("api/usuarios/perfil")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UserUpdateDto
    ): Response<UsuarioDto>

    // --- Producto Controller ---

    @GET("api/productos")
    suspend fun getAllProductos(): Response<List<ProductDto>>

    @GET("api/productos/{sku}")
    suspend fun getProductoBySku(@Path("sku") sku: String): Response<ProductDto>

    // --- Pedidos Controller ---
    // Endpoint para enviar un nuevo pedido al sistema. Requiere autenticación.
    @POST("api/pedidos")
    suspend fun createPedido(
        @Header("Authorization") token: String,
        @Body request: PedidoRequestDto
    ): Response<PedidoResponseDto>
}