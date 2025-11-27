package com.huertohogar.app.model

/**
 * Estado de la UI para la pantalla de Perfil.
 * Ahora incluye los datos del usuario para poder editarlos.
 */
data class ProfileUiState(
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val region: String = "",
    val comuna: String = "",
    val direccion: String = "",
    val profileImageUri: String? = null,

    val isLoading: Boolean = false,
    val successMessage: String? = null, // Para mostrar "Datos guardados correctamente"
    val errorMessage: String? = null
)