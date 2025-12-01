package com.huertohogar.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.huertohogar.app.data.local.datastorage.SessionManager
import com.huertohogar.app.data.remote.model.RegisterRequestDto
import com.huertohogar.app.data.repository.AuthRepository
import com.huertohogar.app.model.RegisterErrorState
import com.huertohogar.app.model.RegisterUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar el registro de nuevos usuarios.
 * Hereda de AndroidViewModel para tener acceso al Contexto y guardar la sesión tras un registro exitoso.
 */
class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    // Dependencias necesarias para guardar datos locales y conectar a la API.
    // Definidas como 'var' para permitir inyección de Mocks en tests.
    var sessionManager = SessionManager(application)
    var authRepository = AuthRepository()

    // Gestión del Estado de la UI (Formulario, Errores, Loading).
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // Regex simple para validar formato de correo.
    private val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    // --- Actualización de campos del formulario ---

    // Métodos para actualizar el estado conforme el usuario escribe.
    // Además de guardar el valor, limpiamos el error específico de ese campo para mejorar la UX.

    fun onNombreChange(nombre: String) {
        _uiState.update { it.copy(nombre = nombre, errors = it.errors.copy(nombre = null)) }
    }

    fun onApellidoChange(apellido: String) {
        _uiState.update { it.copy(apellido = apellido, errors = it.errors.copy(apellido = null)) }
    }

    fun onRunChange(run: String) {
        _uiState.update { it.copy(run = run, errors = it.errors.copy(run = null)) }
    }

    fun onRegionSelected(region: String) {
        // Al cambiar región, reseteamos comuna para mantener consistencia.
        _uiState.update { it.copy(region = region, comuna = "", errors = it.errors.copy(region = null)) }
    }

    fun onComunaSelected(comuna: String) {
        _uiState.update { it.copy(comuna = comuna, errors = it.errors.copy(comuna = null)) }
    }

    fun onDireccionChange(direccion: String) {
        _uiState.update { it.copy(direccion = direccion, errors = it.errors.copy(direccion = null)) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errors = it.errors.copy(email = null)) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errors = it.errors.copy(password = null)) }
    }

    // --- Lógica de Registro ---

    // Proceso principal: Valida -> Carga -> Llama a API -> Guarda Sesión.
    fun onRegisterClicked(onRegisterSuccess: () -> Unit) {
        // Validación local primero para evitar llamadas innecesarias al servidor.
        if (!validarFormulario()) return

        _uiState.update { it.copy(isLoading = true, registerErrorGlobal = null) }

        viewModelScope.launch {
            try {
                // Preparamos el objeto DTO con los datos actuales del estado.
                val currentState = _uiState.value
                val request = RegisterRequestDto(
                    nombre = currentState.nombre,
                    apellido = currentState.apellido,
                    run = currentState.run,
                    email = currentState.email,
                    password = currentState.password,
                    region = currentState.region,
                    comuna = currentState.comuna,
                    direccion = currentState.direccion
                )

                val response = authRepository.register(request)

                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!
                    // Si el registro devuelve token, iniciamos sesión automáticamente.
                    sessionManager.saveUserEmail(authData.usuario.email)

                    _uiState.update { it.copy(isLoading = false) }
                    onRegisterSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            registerErrorGlobal = "Error en el registro. Verifique sus datos o intente más tarde."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        registerErrorGlobal = "Error de conexión: ${e.message}"
                    )
                }
            }
        }
    }

    // Comprueba reglas de negocio básicas (campos vacíos, longitud de contraseña).
    // Actualiza el estado de errores para mostrar advertencias en rojo en la UI.
    private fun validarFormulario(): Boolean {
        val state = _uiState.value

        val isEmailValid = state.email.isNotBlank() && emailPattern.matches(state.email)

        val errors = RegisterErrorState(
            nombre = if (state.nombre.isBlank()) "El nombre es obligatorio" else null,
            apellido = if (state.apellido.isBlank()) "El apellido es obligatorio" else null,
            run = if (state.run.isBlank()) "El RUN es obligatorio" else null,
            region = if (state.region.isBlank()) "Seleccione una región" else null,
            comuna = if (state.comuna.isBlank()) "Seleccione una comuna" else null,
            direccion = if (state.direccion.isBlank()) "La dirección es obligatoria" else null,
            email = if (!isEmailValid) "Email inválido" else null,
            password = if (state.password.length < 6) "Mínimo 6 caracteres" else null
        )

        _uiState.update { it.copy(errors = errors) }

        // Retorna true solo si todos los campos de error son nulos.
        return errors.nombre == null && errors.apellido == null &&
                errors.run == null && errors.region == null &&
                errors.comuna == null && errors.direccion == null &&
                errors.email == null && errors.password == null
    }
}