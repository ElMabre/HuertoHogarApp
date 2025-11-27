package com.huertohogar.app.viewmodel

import android.app.Application
import android.util.Patterns
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
import retrofit2.HttpException // Importante para detectar códigos de error

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // --- Inputs de Texto ---
    fun onNombreChange(v: String) = updateState { it.copy(nombre = v, errors = it.errors.copy(nombre = null)) }
    fun onApellidoChange(v: String) = updateState { it.copy(apellido = v, errors = it.errors.copy(apellido = null)) }
    fun onRunChange(v: String) = updateState { it.copy(run = v, errors = it.errors.copy(run = null)) }
    fun onEmailChange(v: String) = updateState { it.copy(email = v, errors = it.errors.copy(email = null)) }
    fun onPasswordChange(v: String) = updateState { it.copy(password = v, errors = it.errors.copy(password = null)) }
    fun onConfirmChange(v: String) = updateState { it.copy(confirmPassword = v, errors = it.errors.copy(confirmPassword = null)) }
    fun onDireccionChange(v: String) = updateState { it.copy(direccion = v, errors = it.errors.copy(direccion = null)) }

    // --- Selectores ---
    fun onRegionSelected(region: String) {
        updateState {
            it.copy(
                region = region,
                comuna = "", // Reseteamos comuna al cambiar región
                errors = it.errors.copy(region = null)
            )
        }
    }

    fun onComunaSelected(comuna: String) {
        updateState { it.copy(comuna = comuna, errors = it.errors.copy(comuna = null)) }
    }

    fun onAceptaTerminosChange(v: Boolean) = updateState { it.copy(aceptaTerminos = v, errors = it.errors.copy(aceptaTerminos = null)) }

    // Helper para limpiar errores globales al escribir
    private fun updateState(update: (RegisterUiState) -> RegisterUiState) {
        _uiState.update {
            update(it).copy(isLoading = false, registerErrorGlobal = null)
        }
    }

    fun onRegisterClicked(onSuccess: () -> Unit) {
        if (!validarLocal()) return

        _uiState.update { it.copy(isLoading = true, registerErrorGlobal = null) }

        viewModelScope.launch {
            try {
                val state = _uiState.value
                val request = RegisterRequestDto(
                    nombre = state.nombre,
                    apellido = state.apellido,
                    run = state.run,
                    email = state.email,
                    password = state.password,
                    region = state.region,
                    comuna = state.comuna,
                    direccion = state.direccion
                )

                val response = authRepository.register(request)

                if (response.isSuccessful && response.body() != null) {
                    sessionManager.saveUserEmail(response.body()!!.usuario.email)
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                } else {
                    // Manejo de errores HTTP (400, 403, 500) que no lanzan excepción en Retrofit normal
                    val errorMsg = if (response.code() == 400 || response.code() == 403) {
                        "El correo electrónico ya está registrado."
                    } else {
                        "Error del servidor (${response.code()}). Intente más tarde."
                    }
                    _uiState.update { it.copy(isLoading = false, registerErrorGlobal = errorMsg) }
                }
            } catch (e: Exception) {
                // Errores de red (sin internet, servidor apagado)
                _uiState.update { it.copy(isLoading = false, registerErrorGlobal = "Error de conexión. Revise su internet.") }
            }
        }
    }

    private fun validarLocal(): Boolean {
        val s = _uiState.value
        val err = RegisterErrorState(
            nombre = if (s.nombre.isBlank()) "Falta nombre" else null,
            apellido = if (s.apellido.isBlank()) "Falta apellido" else null,
            run = if (s.run.isBlank()) "Falta RUN" else null,
            email = if (!Patterns.EMAIL_ADDRESS.matcher(s.email).matches()) "Correo inválido" else null,
            password = if (s.password.length < 4) "Mínimo 4 caracteres" else null,
            confirmPassword = if (s.password != s.confirmPassword) "No coinciden" else null,
            region = if (s.region.isBlank()) "Seleccione región" else null,
            comuna = if (s.comuna.isBlank()) "Seleccione comuna" else null,
            direccion = if (s.direccion.isBlank()) "Falta dirección" else null,
            aceptaTerminos = if (!s.aceptaTerminos) "Debe aceptar" else null
        )
        _uiState.update { it.copy(errors = err) }
        return err.hasNoErrors()
    }
}