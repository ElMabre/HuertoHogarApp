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
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Interfaz que define todos los endpoints de la API usando Retrofit.
// Aquí se describen las rutas, métodos HTTP y los tipos de datos enviados/recibidos.
interface HuertoHogarApi {

    // --- Auth Controller ---
    // Endpoint para iniciar sesión.
    // Envia el correo/contraseña y recibe un token + información del usuario.
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    // Endpoint para registrar un usuario nuevo.
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    // --- Usuario Controller ---
    // Actualiza los datos del perfil del usuario logueado.
    // Requiere token en el header para autorización.
    @PUT("api/usuarios/perfil")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UserUpdateDto
    ): Response<UsuarioDto>

    // --- Producto Controller ---
    // Obtiene todos los productos del catálogo.
    @GET("api/productos")
    suspend fun getAllProductos(): Response<List<ProductDto>>

    // Obtiene un producto por su SKU (identificador único).
    @GET("api/productos/{sku}")
    suspend fun getProductoBySku(@Path("sku") sku: String): Response<ProductDto>

    // --- Pedidos Controller ---
    // Crea un pedido nuevo. Requiere token para saber qué usuario lo hace.
    @POST("api/pedidos")
    suspend fun createPedido(
        @Header("Authorization") token: String,
        @Body request: PedidoRequestDto
    ): Response<PedidoResponseDto>

    // Obtiene todos los pedidos realizados por el usuario logueado.
    @GET("api/pedidos/mis-pedidos")
    suspend fun getMyPedidos(
        @Header("Authorization") token: String
    ): Response<List<PedidoResponseDto>>

    // Cancela un pedido según ID. También requiere token para autorización.
    @DELETE("api/pedidos/{id}")
    suspend fun cancelPedido(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Void>
}
