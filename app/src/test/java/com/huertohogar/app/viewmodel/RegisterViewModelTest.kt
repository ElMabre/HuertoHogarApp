package com.huertohogar.app.viewmodel

import android.app.Application
import com.huertohogar.app.data.local.datastorage.SessionManager
import com.huertohogar.app.data.remote.model.AuthResponseDto
import com.huertohogar.app.data.remote.model.UsuarioDto
import com.huertohogar.app.data.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    // ViewModel bajo prueba
    private lateinit var viewModel: RegisterViewModel

    // Dependencias simuladas
    private val mockRepository = mockk<AuthRepository>(relaxed = true)
    private val mockSessionManager = mockk<SessionManager>(relaxed = true)
    private val mockApplication = mockk<Application>(relaxed = true)

    // Dispatcher especial para tests
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // Reemplaza Dispatchers.Main por uno controlable
        Dispatchers.setMain(testDispatcher)

        // Crea el VM con Application mock
        viewModel = RegisterViewModel(mockApplication)

        // Inyección forzada mediante reflexión (repositorio y session manager)
        val authRepoField = RegisterViewModel::class.java.getDeclaredField("authRepository")
        authRepoField.isAccessible = true
        authRepoField.set(viewModel, mockRepository)

        val sessionField = RegisterViewModel::class.java.getDeclaredField("sessionManager")
        sessionField.isAccessible = true
        sessionField.set(viewModel, mockSessionManager)
    }

    @After
    fun tearDown() {
        // Restaura el dispatcher original
        Dispatchers.resetMain()
    }

    @Test
    fun `onRegisterClicked con formulario invalido no llama al repositorio`() = runTest {
        // Datos incorrectos
        viewModel.onNombreChange("")
        viewModel.onEmailChange("correo-invalido")

        var successCalled = false
        viewModel.onRegisterClicked { successCalled = true }

        // No debe llamarse la API
        coVerify(exactly = 0) { mockRepository.register(any()) }
        assertFalse(successCalled)

        // Debe mostrar errores
        assertNotNull(viewModel.uiState.value.errors.nombre)
        assertNotNull(viewModel.uiState.value.errors.email)
    }

    @Test
    fun `onRegisterClicked con datos validos llama al repositorio y guarda sesion`() = runTest {
        // Campos del formulario correctos
        viewModel.onNombreChange("Juan")
        viewModel.onApellidoChange("Perez")
        viewModel.onRunChange("12345678-9")
        viewModel.onEmailChange("juan@test.com")
        viewModel.onPasswordChange("123456")
        viewModel.onRegionSelected("Metropolitana")
        viewModel.onComunaSelected("Santiago")
        viewModel.onDireccionChange("Calle Falsa 123")

        // Respuesta simulada exitosa
        val mockUser = UsuarioDto(1, "Juan", "Perez", "juan@test.com", "CLIENTE")
        val mockAuthResponse = AuthResponseDto("fake-token", mockUser)
        coEvery { mockRepository.register(any()) } returns Response.success(mockAuthResponse)

        var successCalled = false
        viewModel.onRegisterClicked { successCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificaciones clave
        coVerify { mockRepository.register(any()) }
        coVerify { mockSessionManager.saveUserEmail("juan@test.com") }

        assertTrue(successCalled)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `onRegisterClicked si falla el servidor muestra error global`() = runTest {
        // Formulario válido
        viewModel.onNombreChange("Juan")
        viewModel.onApellidoChange("Perez")
        viewModel.onRunChange("12345678-9")
        viewModel.onEmailChange("juan@test.com")
        viewModel.onPasswordChange("123456")
        viewModel.onRegionSelected("Biobío")
        viewModel.onComunaSelected("Concepción")
        viewModel.onDireccionChange("Avda Siempre Viva")

        // Error 400 simulado
        val errorBody = "Bad Request".toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { mockRepository.register(any()) } returns Response.error(400, errorBody)

        var successCalled = false
        viewModel.onRegisterClicked { successCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        // Debe mostrar error global
        assertFalse(successCalled)
        assertNotNull(viewModel.uiState.value.registerErrorGlobal)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Validacion de email detecta formatos incorrectos`() {
        // Email incorrecto
        viewModel.onEmailChange("juan.perez")

        // Otros campos válidos mínimos
        viewModel.onPasswordChange("123456")
        viewModel.onNombreChange("A")
        viewModel.onApellidoChange("B")
        viewModel.onRunChange("1-9")
        viewModel.onRegionSelected("R")
        viewModel.onComunaSelected("C")
        viewModel.onDireccionChange("D")

        viewModel.onRegisterClicked {}

        // Debe marcar error en email
        assertNotNull(viewModel.uiState.value.errors.email)
    }
}
