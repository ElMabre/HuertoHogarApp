package com.huertohogar.app.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo enviado al backend para realizar el login.
 * Contiene email y contraseña del usuario.
 */
data class LoginRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

/**
 * Modelo enviado al backend para registrar un nuevo usuario.
 * Incluye todos los datos solicitados en el formulario de registro.
 */
data class RegisterRequestDto(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("run") val run: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("region") val region: String,
    @SerializedName("comuna") val comuna: String,
    @SerializedName("direccion") val direccion: String
)

/**
 * Modelo enviado al backend cuando el usuario desea actualizar su dirección.
 * Solo incluye los campos editables de la dirección.
 */
data class UserUpdateDto(
    @SerializedName("region") val region: String,
    @SerializedName("comuna") val comuna: String,
    @SerializedName("direccion") val direccion: String
)

/**
 * Modelo recibido como respuesta del backend en el login/register.
 * Contiene:
 * - El token JWT para autenticación.
 * - El usuario completo asociado al token.
 */
data class AuthResponseDto(
    @SerializedName("token") val token: String,
    @SerializedName("usuario") val usuario: UsuarioDto
)

/**
 * Modelo que representa un usuario de la aplicación.
 * Contiene información general y opcionalmente los datos de dirección.
 */
data class UsuarioDto(
    @SerializedName("id") val id: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("email") val email: String,
    @SerializedName("rol") val rol: String,

    // Campos opcionales porque pueden venir vacíos si el usuario aún no ha actualizado su perfil
    @SerializedName("region") val region: String? = "",
    @SerializedName("comuna") val comuna: String? = "",
    @SerializedName("direccion") val direccion: String? = ""
)
