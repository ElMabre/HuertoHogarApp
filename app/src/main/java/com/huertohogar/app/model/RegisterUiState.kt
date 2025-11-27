package com.huertohogar.app.model

/**
 * Estado de la UI para la pantalla de Registro.
 * Contiene los valores de los campos y el estado de carga/error.
 */
data class RegisterUiState(
    val nombre: String = "",
    val apellido: String = "",
    val run: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",

    // --- Nuevos Campos ---
    val region: String = "",
    val comuna: String = "",
    val direccion: String = "",

    val aceptaTerminos: Boolean = false,
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,

    val errors: RegisterErrorState = RegisterErrorState(),

    // Campo para el mensaje de error general (rojo) en la parte superior
    val registerErrorGlobal: String? = null
)

/**
 * Estado de errores de validación para el formulario de registro.
 */
data class RegisterErrorState(
    val nombre: String? = null,
    val apellido: String? = null,
    val run: String? = null,
    val email: String? = null,
    val password: String? = null,
    val confirmPassword: String? = null,

    // --- Errores para Nuevos Campos ---
    val region: String? = null,
    val comuna: String? = null,
    val direccion: String? = null,

    val aceptaTerminos: String? = null
) {
    /**
     * Función auxiliar para verificar rápidamente si el formulario es válido.
     * Retorna true si todos los campos de error son nulos.
     */
    fun hasNoErrors(): Boolean {
        return nombre == null && apellido == null && run == null && email == null &&
                password == null && confirmPassword == null && region == null &&
                comuna == null && direccion == null && aceptaTerminos == null
    }
}