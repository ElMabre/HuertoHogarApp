package com.huertohogar.app.viewmodel

import android.app.Application
import com.huertohogar.app.data.local.datastorage.SessionManager
import com.huertohogar.app.data.remote.model.UsuarioDto
import com.huertohogar.app.data.repository.AuthRepository
import com.huertohogar.app.data.remote.model.UserUpdateDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

// Clase de pruebas unitarias para ProfileViewModel.
// Usa 'ExperimentalCoroutinesApi' para controlar los hilos de ejecución (Dispatchers) durante los tests.
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var viewModel: ProfileViewModel

    // Mocks: Objetos simulados que imitan el comportamiento de las dependencias reales (Repositorio, Sesión, App).
    // 'relaxed = true' permite que los mocks devuelvan valores por defecto si no se configuran explícitamente.
    private val mockRepository = mockk<AuthRepository>(relaxed = true)
    private val mockSessionManager = mockk<SessionManager>(relaxed = true)
    private val mockApplication = mockk<Application>(relaxed = true)

    // Despachador de pruebas: Permite controlar el tiempo de las corrutinas (avanzar, pausar) para validar estados asíncronos de forma síncrona.
    private val testDispatcher = StandardTestDispatcher()

    // Configuración inicial que se ejecuta antes de CADA test (@Before).
    // Prepara el entorno, configura el despachador principal y simula los datos necesarios.
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Definimos el comportamiento de los Mocks ANTES de crear el ViewModel.
        // Esto es crítico porque el bloque 'init' del ViewModel consume estos datos inmediatamente al instanciarse.
        every { mockSessionManager.userEmail } returns MutableStateFlow("test@duoc.cl")
        every { mockSessionManager.profileImage } returns MutableStateFlow("content://img.jpg")
        every { mockSessionManager.authToken } returns MutableStateFlow("fake-token")

        viewModel = ProfileViewModel(mockApplication)

        // Inyección de dependencias manual mediante Reflexión (Reflection).
        // Se usa aquí porque las propiedades en el ViewModel son privadas y no tienen constructor público para inyectarlas en el test.
        val authRepoField = ProfileViewModel::class.java.getDeclaredField("authRepository")
        authRepoField.isAccessible = true
        authRepoField.set(viewModel, mockRepository)

        val sessionField = ProfileViewModel::class.java.getDeclaredField("sessionManager")
        sessionField.isAccessible = true
        sessionField.set(viewModel, mockSessionManager)
    }

    // Limpieza después de cada test (@After).
    // Restablece el despachador principal para no afectar a otras pruebas.
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Test: Inicialización de datos.
    // Verifica que el ViewModel lea correctamente los datos del SessionManager apenas se crea.
    @Test
    fun `init carga datos de sesion correctamente`() = runTest {
        // 'advanceUntilIdle': Ejecuta todas las tareas pendientes en las corrutinas antes de verificar.
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("test@duoc.cl", state.email)
        assertEquals("content://img.jpg", state.profileImageUri)
    }

    // Test: Flujo exitoso de guardar cambios ("Happy Path").
    // Simula la interacción del usuario y verifica que se llame al repositorio con los datos correctos.
    @Test
    fun `saveChanges llama al repositorio con datos correctos`() = runTest {
        // DADO: Simulamos entradas del usuario en la UI.
        viewModel.onRegionSelected("Valparaiso")
        viewModel.onComunaSelected("Viña del Mar")
        viewModel.onDireccionChange("Calle 123")

        // Simulamos que el repositorio responde exitosamente.
        val mockUser = UsuarioDto(1, "Test", "User", "test@duoc.cl", "CLIENTE")
        coEvery { mockRepository.updateProfile(any(), any()) } returns Response.success(mockUser)

        // CUANDO: Ejecutamos la acción de guardar.
        viewModel.saveChanges()
        testDispatcher.scheduler.advanceUntilIdle()

        // ENTONCES: Verificamos con 'coVerify' que el método del repositorio fue llamado con los parámetros esperados.
        coVerify {
            mockRepository.updateProfile(
                token = "fake-token",
                request = match { it.region == "Valparaiso" && it.comuna == "Viña del Mar" }
            )
        }
        // Aseguramos que el estado de la UI refleje éxito y no siga cargando.
        assertNotNull(viewModel.uiState.value.successMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // Test: Manejo de errores ("Sad Path").
    // Verifica que la app no colapse y muestre un error si el servidor falla (ej. error 500).
    @Test
    fun `saveChanges maneja error del servidor`() = runTest {
        // DADO: Simulamos que el repositorio lanza una excepción.
        coEvery { mockRepository.updateProfile(any(), any()) } throws Exception("Error 500")

        // CUANDO
        viewModel.saveChanges()
        testDispatcher.scheduler.advanceUntilIdle()

        // ENTONCES: El estado debe contener un mensaje de error.
        assertNotNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // Test: Funcionalidad de Cerrar Sesión.
    // Verifica que se limpie el almacenamiento local y se dispare el evento de navegación (callback).
    @Test
    fun `onLogout limpia la sesion y ejecuta callback`() = runTest {
        var logoutCallbackCalled = false

        // CUANDO
        viewModel.onLogout { logoutCallbackCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        // ENTONCES
        coVerify { mockSessionManager.clearSession() }
        assertTrue(logoutCallbackCalled)
    }

    // Test: Actualización local de imagen.
    // Verifica que al seleccionar una nueva foto, esta se persista en el SessionManager.
    @Test
    fun `updateProfileImage guarda la uri en sessionManager`() = runTest {
        val nuevaUri = "file://nueva_foto.jpg"

        viewModel.updateProfileImage(nuevaUri)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockSessionManager.saveProfileImage(nuevaUri) }
    }
}