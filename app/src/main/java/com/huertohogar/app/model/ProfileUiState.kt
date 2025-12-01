package com.huertohogar.app.model

/**
 * Estado de la UI para la pantalla de Perfil.
 * Contiene los datos actuales del usuario y estados visuales relacionados a la edición.
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
    val successMessage: String? = null,
    val errorMessage: String? = null
)