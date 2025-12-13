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
import retrofit2.HttpException
import java.io.IOException

/**
 * ViewModel para gestionar el inicio de sesión.
 * Incluye manejo de errores específicos (IOException, HttpException).
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // Dependencias para guardar sesión y conectar a internet.
    @VisibleForTesting
    var sessionManager = SessionManager(application)

    @VisibleForTesting
    var authRepository = AuthRepository()

    // Estado de la UI (StateFlow).
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val TAG = "LoginViewModel"

    // Actualiza el email y limpia errores previos.
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

    // Actualiza la contraseña y limpia errores previos.
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

    // Cambia la visibilidad de la contraseña.
    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    // Proceso principal de Login.
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
                    // Manejo de códigos de error específicos del servidor (400, 401, 500)
                    Log.e(TAG, "Error del servidor: ${response.code()} ${response.message()}")
                    val mensajeError = when (response.code()) {
                        401 -> "Credenciales incorrectas. Verifique email y contraseña."
                        404 -> "Usuario no encontrado."
                        500 -> "Error interno del servidor. Intente más tarde."
                        else -> "Error en el inicio de sesión (${response.code()})."
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loginError = mensajeError
                        )
                    }
                }
            } catch (e: IOException) {
                // Captura errores de conexión (sin internet, timeout)
                Log.e(TAG, "Error de Red en Login", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginError = "Sin conexión a internet. Compruebe su red."
                    )
                }
            } catch (e: HttpException) {
                // Captura errores lanzados por Retrofit/OkHttp
                Log.e(TAG, "Error HTTP en Login", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginError = "Error del servidor: ${e.message()}"
                    )
                }
            } catch (e: Exception) {
                // Captura cualquier otro error no previsto (NPE, parseo, etc.)
                Log.e(TAG, "Error CRÍTICO en Login", e)
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginError = "Ocurrió un error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    // Validación local.
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
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return email.isNotBlank() && email.matches(emailRegex)
    }
}