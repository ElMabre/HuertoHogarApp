package com.huertohogar.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.huertohogar.app.data.local.datastorage.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la UI para el Perfil
data class ProfileUiState(
    val userEmail: String? = null,
    val profileImageUri: Uri? = null,
    val isLoading: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // CORRECCIÓN: Accedemos a 'userEmail' como propiedad (sin paréntesis)
            // y usamos collect para escuchar el flujo de datos.
            sessionManager.userEmail.collect { emailGuardado ->
                _uiState.update {
                    it.copy(
                        userEmail = emailGuardado,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateProfileImage(uri: Uri?) {
        if (uri != null) {
            _uiState.update { it.copy(profileImageUri = uri) }
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            sessionManager.clearUserEmail()
            // Limpiamos el estado local también
            _uiState.value = ProfileUiState()
        }
    }
}