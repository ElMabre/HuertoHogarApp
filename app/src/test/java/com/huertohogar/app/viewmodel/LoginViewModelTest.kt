package com.huertohogar.app.viewmodel

import android.app.Application
import android.util.Log
import com.huertohogar.app.data.local.datastorage.SessionManager
import com.huertohogar.app.data.remote.model.AuthResponseDto
import com.huertohogar.app.data.remote.model.UsuarioDto
import com.huertohogar.app.data.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel

    // Dependencias mocks
    private val mockRepository = mockk<AuthRepository>(relaxed = true)
    private val mockSessionManager = mockk<SessionManager>(relaxed = true)
    private val mockApplication = mockk<Application>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Mock de Log para evitar errores "Method not mocked"
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        viewModel = LoginViewModel(mockApplication)
        // Inyección de propiedades para el test
        viewModel.authRepository = mockRepository
        viewModel.sessionManager = mockSessionManager
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Login exitoso debe guardar sesion y ejecutar callback`() = runTest {
        val email = "test@duoc.cl"
        val password = "123"

        val mockResponse = AuthResponseDto(
            token = "fake-token",
            usuario = UsuarioDto(
                id = 1,
                nombre = "Test",
                apellido = "Usuario",
                email = email,
                rol = "CLIENTE"
            )
        )

        coEvery { mockRepository.login(any()) } returns Response.success(mockResponse)

        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)

        var successCalled = false

        // Ejecutar login
        viewModel.onLoginClicked { successCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificaciones
        assertTrue(successCalled)
        assertFalse(viewModel.uiState.value.isLoading)
        coVerify { mockSessionManager.saveAuthToken("fake-token") }
    }

    @Test
    fun `Login fallido debe mostrar mensaje de error`() = runTest {
        val email = "fail@duoc.cl"
        val password = "wrong"

        // Simulamos error 401 (Unauthorized)
        val errorBody = "Unauthorized".toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { mockRepository.login(any()) } returns Response.error(401, errorBody)

        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)

        var successCalled = false

        viewModel.onLoginClicked { successCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(successCalled)

        val errorMsg = viewModel.uiState.value.loginError
        assertTrue("El mensaje de error no debe ser nulo", errorMsg != null)

        // CORRECCIÓN: Verificamos el mensaje amigable definido en el ViewModel para el código 401,
        // en lugar de buscar el string "401".
        assertTrue(
            "Se esperaba mensaje de credenciales incorrectas, pero fue: $errorMsg",
            errorMsg!!.contains("Credenciales incorrectas") || errorMsg.contains("401")
        )
    }

    @Test
    fun `Validacion local debe rechazar password vacia`() = runTest {
        viewModel.onEmailChange("valid@mail.com")
        viewModel.onPasswordChange("")

        var successCalled = false

        viewModel.onLoginClicked { successCalled = true }

        assertFalse(successCalled)
        coVerify(exactly = 0) { mockRepository.login(any()) }

        assertTrue(viewModel.uiState.value.errors.password != null)
    }
}