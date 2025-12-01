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

/**
 * ViewModel para la pantalla de Perfil de Usuario.
 * Gestiona la visualización de datos personales y la actualización de los mismos.
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    // Dependencias para persistencia local (DataStore) y remota (API).
    private val sessionManager = SessionManager(application)
    private val authRepository = AuthRepository()

    // Estado UI (StateFlow).
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Inicialización: Empezamos a escuchar cambios en la sesión inmediatamente.
    init {
        startSessionObservation()
    }

    // Patrón Reactivo:
    // En lugar de pedir los datos una sola vez, "observamos" (collect) el DataStore.
    // Si algo cambia en el almacenamiento local (ej: foto nueva), la UI se actualiza automáticamente.
    private fun startSessionObservation() {
        viewModelScope.launch {
            // 1. Observar cambios en el Email
            launch {
                sessionManager.userEmail.collect { email ->
                    _uiState.update { it.copy(email = email ?: "") }
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
    // Métodos simples para que la Vista actualice el estado mientras el usuario escribe.

    fun onNombreChange(text: String) {
        _uiState.update { it.copy(nombre = text) }
    }

    fun onApellidoChange(text: String) {
        _uiState.update { it.copy(apellido = text) }
    }

    fun onRegionSelected(text: String) {
        // Al cambiar región, reseteamos la comuna para evitar inconsistencias.
        _uiState.update { it.copy(region = text, comuna = "") }
    }

    fun onComunaSelected(text: String) {
        _uiState.update { it.copy(comuna = text) }
    }

    fun onDireccionChange(text: String) {
        _uiState.update { it.copy(direccion = text) }
    }

    // --- Acciones Principales ---

    // Envía los cambios al servidor.
    fun saveChanges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            try {
                // Aquí usamos .first() en lugar de .collect() porque solo necesitamos
                // el valor del token *en este preciso momento* para hacer la llamada.
                val token = sessionManager.authToken.first()

                if (token.isNullOrEmpty()) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Sesión expirada. Inicie sesión nuevamente.") }
                    return@launch
                }

                // Preparamos el objeto para enviar al backend.
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

    // Guarda la nueva foto localmente.
    // NOTA: No actualizamos _uiState manualmente aquí. Al guardar en sessionManager,
    // el observador del bloque 'init' detectará el cambio y actualizará la UI solo.
    fun updateProfileImage(uri: String) {
        viewModelScope.launch {
            sessionManager.saveProfileImage(uri)
        }
    }

    // Cierre de sesión completo.
    fun onLogout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            // Borramos datos de disco. Esto disparará los observers con valores nulos.
            sessionManager.clearSession()
            // Reseteamos el estado visual por seguridad.
            _uiState.update { ProfileUiState() }
            onLogoutSuccess()
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}