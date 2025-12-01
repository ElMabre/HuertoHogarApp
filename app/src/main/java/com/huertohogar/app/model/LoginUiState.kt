package com.huertohogar.app.model

/**
 * Estado que representa todos los datos necesarios para manejar la pantalla de Login.
 * Incluye los campos del formulario, estados visuales y mensajes de error.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errors: LoginErrorState = LoginErrorState(),
    val loginError: String? = null
)

/**
 * Representa errores específicos de los campos del formulario.
 * Se usa para mostrar mensajes debajo de inputs individuales.
 */
data class LoginErrorState(
    val email: String? = null,
    val password: String? = null
)