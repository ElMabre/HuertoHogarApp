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

// tests para el login
// probamos que se guarde el token si todo sale bien y que maneje los errores
// tambien revisamos validaciones locales antes de enviar datos
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel

    // dependencias falsas (mocks) para no usar la red ni la base de datos de verdad
    // el relaxed=true es para que no crashee si llamamos algo que no configuramos
    private val mockRepository = mockk<AuthRepository>(relaxed = true)
    private val mockSessionManager = mockk<SessionManager>(relaxed = true)
    private val mockApplication = mockk<Application>(relaxed = true)

    // controlador de corrutinas para tests, hace que el codigo asincrono corra seguido
    private val testDispatcher = StandardTestDispatcher()

    // configuracion inicial antes de cada test
    // aqui hay un truco importante con el Log de android
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // mockeamos la clase estatica Log porque en los tests unitarios no existe android real
        // si no hacemos esto, cualquier Log.d en el viewmodel haria fallar el test
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        // inyectamos los mocks en el viewmodel
        viewModel = LoginViewModel(mockApplication)
        viewModel.authRepository = mockRepository
        viewModel.sessionManager = mockSessionManager
    }

    // limpieza despues de cada test, importante resetear el dispatcher
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // probamos el camino feliz: si la api responde ok, debemos guardar el token
    // y avisar a la vista que pasamos
    @Test
    fun `Login exitoso debe guardar sesion y ejecutar callback`() = runTest {
        val email = "test@duoc.cl"
        val password = "123"

        // Respuesta simulada del login
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

        // le decimos al mock que responda success cuando llamen a login
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

        // verificamos que SI se haya llamado a guardar el token en sesion
        coVerify { mockSessionManager.saveAuthToken("fake-token") }
    }

    // prueba de error del servidor (ej: contraseña incorrecta 401)
    // el viewmodel tiene que capturar el error y mostrarlo, no crashear
    @Test
    fun `Login fallido debe mostrar mensaje de error`() = runTest {
        val email = "fail@duoc.cl"
        val password = "wrong"

        // creamos un cuerpo de error falso para simular el 401
        val errorBody = "Unauthorized".toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { mockRepository.login(any()) } returns Response.error(401, errorBody)

        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)

        var successCalled = false

        // Intento de login
        viewModel.onLoginClicked { successCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(successCalled)

        // revisamos que el estado tenga el mensaje de error
        val errorMsg = viewModel.uiState.value.loginError
        assertTrue(errorMsg != null)
        assertTrue(errorMsg!!.contains("401"))
    }

    // validacion local para no gastar datos
    // si el campo esta vacio, ni siquiera deberia llamar al repositorio
    @Test
    fun `Validacion local debe rechazar password vacia`() = runTest {
        viewModel.onEmailChange("valid@mail.com")
        viewModel.onPasswordChange("")

        var successCalled = false

        // Intentar login con password vacía
        viewModel.onLoginClicked { successCalled = true }

        // verificamos que NO se llamo al login (exactly = 0)
        assertFalse(successCalled)
        coVerify(exactly = 0) { mockRepository.login(any()) }

        // Debe mostrar error local en el input
        assertTrue(viewModel.uiState.value.errors.password != null)
    }
}