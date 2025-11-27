package com.huertohogar.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.huertohogar.app.data.local.datastorage.SessionManager
import com.huertohogar.app.data.remote.model.UserUpdateDto
import com.huertohogar.app.data.repository.AuthRepository
import com.huertohogar.app.model.ProfileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Iniciamos la observación constante de los datos de sesión
        startSessionObservation()
    }

    private fun startSessionObservation() {
        viewModelScope.launch {
            // 1. Observar cambios en el Email (Usuario actual)
            // Al usar 'collect', si cambias de cuenta, esto se ejecuta de nuevo automáticamente.
            launch {
                sessionManager.userEmail.collect { email ->
                    _uiState.update { it.copy(email = email ?: "") }
                    // Opcional: Si quisieras recargar datos del servidor al detectar cambio de email, podrías hacerlo aquí.
                }
            }

            // 2. Observar cambios en la Foto de Perfil
            launch {
                sessionManager.profileImage.collect { uri ->
                    _uiState.update { it.copy(profileImageUri = uri) }
                }
            }
        }
    }

    // --- Actualización de campos del formulario (UI) ---

    fun onNombreChange(text: String) {
        _uiState.update { it.copy(nombre = text) }
    }

    fun onApellidoChange(text: String) {
        _uiState.update { it.copy(apellido = text) }
    }

    fun onRegionSelected(text: String) {
        _uiState.update { it.copy(region = text, comuna = "") }
    }

    fun onComunaSelected(text: String) {
        _uiState.update { it.copy(comuna = text) }
    }

    fun onDireccionChange(text: String) {
        _uiState.update { it.copy(direccion = text) }
    }

    // --- Acciones Principales ---

    fun saveChanges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            try {
                // Obtenemos el token actual (sin 'collect', solo el valor actual para la petición)
                val token = sessionManager.authToken.first()

                if (token.isNullOrEmpty()) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Sesión expirada. Inicie sesión nuevamente.") }
                    return@launch
                }

                val updateDto = UserUpdateDto(
                    region = _uiState.value.region,
                    comuna = _uiState.value.comuna,
                    direccion = _uiState.value.direccion
                )

                val response = authRepository.updateProfile(token, updateDto)

                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(isLoading = false, successMessage = "¡Datos actualizados correctamente!")
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Error del servidor: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Error de conexión: ${e.message}")
                }
            }
        }
    }

    fun updateProfileImage(uri: String) {
        viewModelScope.launch {
            // Guardamos en DataStore. El 'collect' de arriba actualizará la UI automáticamente.
            sessionManager.saveProfileImage(uri)
        }
    }

    fun onLogout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            // Limpiamos toda la sesión. Esto disparará los 'collect' con valores nulos, limpiando la UI.
            sessionManager.clearSession()
            // Reseteamos el estado de la UI manualmente también por seguridad visual
            _uiState.update { ProfileUiState() }
            onLogoutSuccess()
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}