package com.huertohogar.app.data.repository

import com.huertohogar.app.data.remote.RetrofitClient
import com.huertohogar.app.data.remote.model.AuthResponseDto
import com.huertohogar.app.data.remote.model.LoginRequestDto
import com.huertohogar.app.data.remote.model.RegisterRequestDto
import com.huertohogar.app.data.remote.model.UserUpdateDto
import com.huertohogar.app.data.remote.model.UsuarioDto
import retrofit2.Response

class AuthRepository {

    private val api = RetrofitClient.authApi

    suspend fun login(request: LoginRequestDto): Response<AuthResponseDto> {
        return api.login(request)
    }

    suspend fun register(request: RegisterRequestDto): Response<AuthResponseDto> {
        return api.register(request)
    }

    suspend fun updateProfile(token: String, request: UserUpdateDto): Response<UsuarioDto> {
        return api.updateProfile("Bearer $token", request)
    }
}