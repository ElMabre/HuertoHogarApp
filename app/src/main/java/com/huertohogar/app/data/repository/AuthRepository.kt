package com.huertohogar.app.data.repository

import com.huertohogar.app.data.remote.RetrofitClient
import com.huertohogar.app.data.remote.model.AuthResponseDto
import com.huertohogar.app.data.remote.model.LoginRequestDto
import com.huertohogar.app.data.remote.model.RegisterRequestDto
import retrofit2.Response

class AuthRepository {

    // CORRECCIÓN: Usamos 'authApi', que es el nombre nuevo en RetrofitClient
    private val api = RetrofitClient.authApi

    suspend fun login(loginRequest: LoginRequestDto): Response<AuthResponseDto> {
        return api.login(loginRequest)
    }

    suspend fun register(registerRequest: RegisterRequestDto): Response<AuthResponseDto> {
        return api.register(registerRequest)
    }
}