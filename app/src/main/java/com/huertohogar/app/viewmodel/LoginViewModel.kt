package com.huertohogar.app.viewmodel

import android.app.Application
import android.util.Log
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
import org.jetbrains.annotations.VisibleForTesting

/**
 * ViewModel para gestionar el inicio de sesión.
 * Hereda de AndroidViewModel porque necesitamos el "Contexto" (Application)
 * para poder guardar los datos de sesión en el móvil.
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // Dependencias para guardar sesión y conectar a internet.
    // Son 'var' y visibles para testing para poder sustituirlas por versiones falsas en las pruebas.
    @VisibleForTesting
    var sessionManager = SessionManager(application)

    @VisibleForTesting
    var authRepository = AuthRepository()

    // Estado de la UI (StateFlow).
    // _uiState es privado para modificarlo y uiState es público solo para lectura desde la Vista.
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val TAG = "LoginViewModel"

    // Actualiza el email mientras el usuario escribe y limpia errores previos para mejorar la UX.
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

    // Actualiza la contraseña y resetea cualquier error relacionado.
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

    // Cambia el estado de visibilidad de la contraseña (ojo ver/ocultar).
    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    // Proceso principal de Login.
    // 1. Valida datos locales.
    // 2. Lanza una corrutina (hilo secundario) para conectar con la API.
    // 3. Si es exitoso, guarda la sesión y navega. Si falla, muestra el error.
    fun onLoginClicked(onLoginSuccess: () -> Unit) {
        if (!validarFormularioLocal()) {
            return
        }

        _uiState.update { it.copy(isLoading = true, loginError = null) }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Intentando login con: ${_uiState.value.email}")

                val request = LoginRequestDto(
                    email = _uiState.value.email,
                    password = _uiState.value.password
                )
                val response = authRepository.login(request)

                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!

                    Log.i(TAG, "Login exitoso. Guardando sesión...")

                    sessionManager.saveUserEmail(authData.usuario.email)
                    sessionManager.saveAuthToken(authData.token)
                    sessionManager.saveUserId(authData.usuario.id)

                    _uiState.update { it.copy(isLoading = false) }
                    onLoginSuccess()
                } else {
                    Log.e(TAG, "Error del servidor: ${response.code()} ${response.message()}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loginError = "Credenciales incorrectas o error en el servidor (${response.code()})."
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error CRÍTICO en Login", e)
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginError = "Error de conexión: ${e.message}"
                    )
                }
            }
        }
    }

    // Comprueba formato de email y campos vacíos antes de molestar al servidor.
    // Devuelve true si todo está correcto.
    private fun validarFormularioLocal(): Boolean {
        val state = _uiState.value
        val newErrors = LoginErrorState(
            email = if (!isValidEmail(state.email)) "El correo no es válido" else null,
            password = if (state.password.isBlank()) "La contraseña es obligatoria" else null
        )
        _uiState.update { it.copy(errors = newErrors) }
        return newErrors.email == null && newErrors.password == null
    }

    // Helper simple con Regex para asegurar que parece un correo real.
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return email.isNotBlank() && email.matches(emailRegex)
    }
}