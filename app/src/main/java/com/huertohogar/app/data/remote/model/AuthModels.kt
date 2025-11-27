package com.huertohogar.app.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo para enviar las credenciales de Login.
 */
data class LoginRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

/**
 * Modelo para enviar los datos de Registro.
 * Actualizado con Región, Comuna y Dirección.
 */
data class RegisterRequestDto(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("run") val run: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,

    // --- Nuevos Campos ---
    @SerializedName("region") val region: String,
    @SerializedName("comuna") val comuna: String,
    @SerializedName("direccion") val direccion: String
)

/**
 * Modelo de respuesta de autenticación.
 */
data class AuthResponseDto(
    @SerializedName("token") val token: String,
    @SerializedName("usuario") val usuario: UsuarioDto
)

/**
 * Modelo de usuario que viene dentro de AuthResponse.
 */
data class UsuarioDto(
    @SerializedName("id") val id: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("email") val email: String,
    @SerializedName("rol") val rol: String
)