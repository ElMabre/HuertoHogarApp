package com.huertohogar.app.model

/**
 * Estado de la UI para la pantalla de Registro.
 * Contiene los valores ingresados en cada campo del formulario,
 * además de estados de visibilidad, carga y errores.
 */
data class RegisterUiState(
    val nombre: String = "",
    val apellido: String = "",
    val run: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",

    // --- Nuevos campos ---
    val region: String = "",
    val comuna: String = "",
    val direccion: String = "",

    val aceptaTerminos: Boolean = false,
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,

    val errors: RegisterErrorState = RegisterErrorState(),

    val registerErrorGlobal: String? = null
)

/**
 * Estado que almacena mensajes de error para cada campo del registro.
 * Se usa para mostrar advertencias específicas bajo cada input.
 */
data class RegisterErrorState(
    val nombre: String? = null,
    val apellido: String? = null,
    val run: String? = null,
    val email: String? = null,
    val password: String? = null,
    val confirmPassword: String? = null,
    val region: String? = null,
    val comuna: String? = null,
    val direccion: String? = null,

    val aceptaTerminos: String? = null
) {
    /**
     * Función que indica si el formulario está libre de errores.
     * Retorna true cuando todos los campos de error son nulos.
     */
    fun hasNoErrors(): Boolean {
        return nombre == null && apellido == null && run == null && email == null &&
                password == null && confirmPassword == null && region == null &&
                comuna == null && direccion == null && aceptaTerminos == null
    }
}