package com.huertohogar.app.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.huertohogar.app.data.local.datastorage.SessionManager
import com.huertohogar.app.data.remote.model.LoginRequestDto
import com.huertohogar.app.data.repository.AuthRepository
import com.huertohogar.app.model.LoginErrorState
import com.huertohogar.app.model.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // Se instancia con el contexto de la aplicación
    private val sessionManager = SessionManager(application)
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { currentState ->
            currentState.copy(
                email = email,
                errors = currentState.errors.copy(email = null),
                isLoading = false,
                loginError = null
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { currentState ->
            currentState.copy(
                password = password,
                errors = currentState.errors.copy(password = null),
                isLoading = false,
                loginError = null
            )
        }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun onLoginClicked(onLoginSuccess: () -> Unit) {
        // 1. Validar campos locales
        if (!validarFormularioLocal()) {
            return
        }

        // 2. Iniciar carga
        _uiState.update { it.copy(isLoading = true, loginError = null) }

        viewModelScope.launch {
            try {
                // 3. Llamada al Backend
                val request = LoginRequestDto(
                    email = _uiState.value.email,
                    password = _uiState.value.password
                )
                val response = authRepository.login(request)

                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!

                    // 4. Guardar sesión COMPLETA (Email, Token e ID)
                    sessionManager.saveUserEmail(authData.usuario.email)
                    sessionManager.saveAuthToken(authData.token)
                    sessionManager.saveUserId(authData.usuario.id)

                    // También podrías guardar la imagen si viniera del backend
                    // sessionManager.saveProfileImage(...)

                    _uiState.update { it.copy(isLoading = false) }
                    onLoginSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loginError = "Credenciales incorrectas o error en el servidor."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginError = "Error de conexión: ${e.message}"
                    )
                }
            }
        }
    }

    private fun validarFormularioLocal(): Boolean {
        val state = _uiState.value
        val newErrors = LoginErrorState(
            email = if (!isValidEmail(state.email)) "El correo no es válido" else null,
            password = if (state.password.isBlank()) "La contraseña es obligatoria" else null
        )
        _uiState.update { it.copy(errors = newErrors) }
        return newErrors.email == null && newErrors.password == null
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}