package com.huertohogar.app.data.repository

import com.huertohogar.app.data.remote.RetrofitClient
import com.huertohogar.app.data.remote.model.AuthResponseDto
import com.huertohogar.app.data.remote.model.LoginRequestDto
import com.huertohogar.app.data.remote.model.RegisterRequestDto
import com.huertohogar.app.data.remote.model.UserUpdateDto
import com.huertohogar.app.data.remote.model.UsuarioDto
import retrofit2.Response

/**
 * Repositorio encargado de gestionar las operaciones de autenticación
 * y administración de perfil del usuario.
 *
 * Se comunica con el backend a través del cliente Retrofit.
 */
class AuthRepository {

    // API dedicada a las operaciones de autenticación.
    private val api = RetrofitClient.authApi

    /**
     * Inicia sesión con las credenciales proporcionadas.
     * Retorna un AuthResponseDto con el token y datos básicos del usuario.
     */
    suspend fun login(request: LoginRequestDto): Response<AuthResponseDto> {
        return api.login(request)
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * Devuelve un AuthResponseDto con el token generado tras el registro.
     */
    suspend fun register(request: RegisterRequestDto): Response<AuthResponseDto> {
        return api.register(request)
    }
    /**
     * Actualiza la información del perfil del usuario.
     * Requiere un token válido para autorización.
     */
    suspend fun updateProfile(token: String, request: UserUpdateDto): Response<UsuarioDto> {
        return api.updateProfile("Bearer $token", request)
    }
}
