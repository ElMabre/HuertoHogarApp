package com.huertohogar.app.viewmodel

import android.app.Application
// import android.util.Patterns <-- ELIMINAMOS ESTA LÍNEA
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

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    var sessionManager = SessionManager(application)
    var authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    // --- Actualización de campos ---

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

    fun onRegisterClicked(onRegisterSuccess: () -> Unit) {
        if (!validarFormulario()) return

        _uiState.update { it.copy(isLoading = true, registerErrorGlobal = null) }

        viewModelScope.launch {
            try {
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

        return errors.nombre == null && errors.apellido == null &&
                errors.run == null && errors.region == null &&
                errors.comuna == null && errors.direccion == null &&
                errors.email == null && errors.password == null
    }
}