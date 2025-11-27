package com.huertohogar.app.model

/**
 * Estado de la UI para la pantalla de Login.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errors: LoginErrorState = LoginErrorState(),
    val loginError: String? = null // <--- ¡ESTO FALTABA!
)

/**
 * Estado de errores de validación de campos individuales.
 */
data class LoginErrorState(
    val email: String? = null,
    val password: String? = null
)