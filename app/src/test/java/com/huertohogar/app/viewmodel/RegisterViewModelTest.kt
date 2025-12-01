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

// Clase de pruebas para RegisterViewModel.
// Verifica que la lógica del formulario de registro, validaciones y llamadas a la API funcionen correctamente sin conectar al servidor real.
@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private lateinit var viewModel: RegisterViewModel

    // Dependencias simuladas (Mocks).
    // Usamos 'mockk' para crear versiones falsas del Repositorio y SessionManager.
    // Esto aísla el ViewModel: si el test falla, sabemos que es culpa del ViewModel y no de la base de datos o internet.
    private val mockRepository = mockk<AuthRepository>(relaxed = true)
    private val mockSessionManager = mockk<SessionManager>(relaxed = true)
    private val mockApplication = mockk<Application>(relaxed = true)

    // Despachador de pruebas para Corrutinas.
    // Permite que el código asíncrono (suspend functions) se ejecute de manera controlada y predecible en los tests.
    private val testDispatcher = StandardTestDispatcher()

    // Configuración inicial (@Before).
    // Se ejecuta antes de cada test para asegurar un estado limpio.
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RegisterViewModel(mockApplication)

        // Inyección de dependencias mediante Reflexión.
        // Como el ViewModel tiene propiedades privadas para el repositorio y la sesión,
        // usamos Java Reflection para "forzar" la entrada de nuestros mocks en esas variables privadas.
        val authRepoField = RegisterViewModel::class.java.getDeclaredField("authRepository")
        authRepoField.isAccessible = true
        authRepoField.set(viewModel, mockRepository)

        val sessionField = RegisterViewModel::class.java.getDeclaredField("sessionManager")
        sessionField.isAccessible = true
        sessionField.set(viewModel, mockSessionManager)
    }

    // Limpieza (@After).
    // Restablece el hilo principal (Main dispatcher) para no afectar otras pruebas que corran después.
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Test: Validación de formulario (Lógica de negocio local).
    // Verifica que NO se intente contactar al servidor si los datos ingresados son inválidos. Ahorra recursos y datos.
    @Test
    fun `onRegisterClicked con formulario invalido no llama al repositorio`() = runTest {
        // DADO: Un formulario con datos vacíos o incorrectos.
        viewModel.onNombreChange("")
        viewModel.onEmailChange("correo-invalido")

        var successCalled = false

        // CUANDO: El usuario presiona registrar.
        viewModel.onRegisterClicked { successCalled = true }

        // ENTONCES:
        // 1. Verificamos que el repositorio NUNCA fue llamado (exactly = 0).
        coVerify(exactly = 0) { mockRepository.register(any()) }
        // 2. Verificamos que se generaron errores en el estado de la UI.
        assertNotNull(viewModel.uiState.value.errors.nombre)
        assertNotNull(viewModel.uiState.value.errors.email)
    }

    // Test: Flujo de éxito ("Happy Path").
    // Verifica que, con datos correctos, el ViewModel llame al repositorio y guarde la sesión si la API responde bien.
    @Test
    fun `onRegisterClicked con datos validos llama al repositorio y guarda sesion`() = runTest {
        // DADO: Un formulario completo y válido.
        viewModel.onNombreChange("Juan")
        viewModel.onApellidoChange("Perez")
        viewModel.onRunChange("12345678-9")
        viewModel.onEmailChange("juan@test.com")
        viewModel.onPasswordChange("123456")
        viewModel.onRegionSelected("Metropolitana")
        viewModel.onComunaSelected("Santiago")
        viewModel.onDireccionChange("Calle Falsa 123")

        // Simulamos una respuesta exitosa del servidor (HTTP 200).
        val mockUser = UsuarioDto(1, "Juan", "Perez", "juan@test.com", "CLIENTE")
        val mockAuthResponse = AuthResponseDto("fake-token", mockUser)
        coEvery { mockRepository.register(any()) } returns Response.success(mockAuthResponse)

        // CUANDO: Se hace clic en registrar.
        var successCalled = false
        viewModel.onRegisterClicked { successCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        // ENTONCES: Confirmamos que se llamó a la API y se guardó el email localmente.
        coVerify { mockRepository.register(any()) }
        coVerify { mockSessionManager.saveUserEmail("juan@test.com") }
        assertTrue(successCalled)
    }

    // Test: Manejo de errores del servidor ("Sad Path").
    // Verifica cómo reacciona la app si el servidor rechaza la solicitud (ej. Error 400 Bad Request).
    @Test
    fun `onRegisterClicked si falla el servidor muestra error global`() = runTest {
        // DADO: Un formulario válido pero un servidor que falla.
        viewModel.onNombreChange("Juan") // ... (llenado de datos mínimos para pasar validación local)
        viewModel.onApellidoChange("Perez")
        viewModel.onRunChange("12345678-9")
        viewModel.onEmailChange("juan@test.com")
        viewModel.onPasswordChange("123456")
        viewModel.onRegionSelected("Biobío")
        viewModel.onComunaSelected("Concepción")
        viewModel.onDireccionChange("Avda Siempre Viva")

        // Simulamos un error HTTP 400.
        val errorBody = "Bad Request".toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { mockRepository.register(any()) } returns Response.error(400, errorBody)

        // CUANDO: Se intenta registrar.
        var successCalled = false
        viewModel.onRegisterClicked { successCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        // ENTONCES: No debe navegar al éxito, y debe mostrar un mensaje de error global en la UI.
        assertFalse(successCalled)
        assertNotNull(viewModel.uiState.value.registerErrorGlobal)
    }

    // Test: Regla específica de validación.
    // Prueba unitaria pura de lógica de validación (Regex de email) para asegurar que detecta formatos erróneos.
    @Test
    fun `Validacion de email detecta formatos incorrectos`() {
        // DADO: Un email sin @ o dominio.
        viewModel.onEmailChange("juan.perez")

        // Llenamos el resto con datos ficticios para aislar el error del email.
        viewModel.onPasswordChange("123456")
        viewModel.onNombreChange("A")
        viewModel.onApellidoChange("B")
        viewModel.onRunChange("1-9")
        viewModel.onRegionSelected("R")
        viewModel.onComunaSelected("C")
        viewModel.onDireccionChange("D")

        // CUANDO: Se valida el formulario.
        viewModel.onRegisterClicked {}

        // ENTONCES: El campo email debe reportar error.
        assertNotNull(viewModel.uiState.value.errors.email)
    }
}