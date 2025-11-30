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

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var viewModel: ProfileViewModel
    private val mockRepository = mockk<AuthRepository>(relaxed = true)
    private val mockSessionManager = mockk<SessionManager>(relaxed = true)
    private val mockApplication = mockk<Application>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // IMPORTANTE: ProfileViewModel observa flujos (Flows) en su init block.
        // Debemos configurar los mocks de los flows ANTES de instanciar el ViewModel.
        every { mockSessionManager.userEmail } returns MutableStateFlow("test@duoc.cl")
        every { mockSessionManager.profileImage } returns MutableStateFlow("content://img.jpg")
        every { mockSessionManager.authToken } returns MutableStateFlow("fake-token")

        viewModel = ProfileViewModel(mockApplication)

        // Inyección de dependencias por reflexión
        val authRepoField = ProfileViewModel::class.java.getDeclaredField("authRepository")
        authRepoField.isAccessible = true
        authRepoField.set(viewModel, mockRepository)

        val sessionField = ProfileViewModel::class.java.getDeclaredField("sessionManager")
        sessionField.isAccessible = true
        sessionField.set(viewModel, mockSessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init carga datos de sesion correctamente`() = runTest {
        // Al iniciar el ViewModel, debería recolectar los flujos del SessionManager
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("test@duoc.cl", state.email)
        assertEquals("content://img.jpg", state.profileImageUri)
    }

    @Test
    fun `saveChanges llama al repositorio con datos correctos`() = runTest {
        // DADO: Datos modificados en la UI
        viewModel.onRegionSelected("Valparaiso")
        viewModel.onComunaSelected("Viña del Mar")
        viewModel.onDireccionChange("Calle 123")

        // Mock respuesta exitosa
        val mockUser = UsuarioDto(1, "Test", "User", "test@duoc.cl", "CLIENTE")
        coEvery { mockRepository.updateProfile(any(), any()) } returns Response.success(mockUser)

        // CUANDO: Guardamos cambios
        viewModel.saveChanges()
        testDispatcher.scheduler.advanceUntilIdle()

        // ENTONCES
        // Verificamos que se llamó al repositorio con el token y el objeto DTO correcto
        coVerify {
            mockRepository.updateProfile(
                token = "fake-token",
                request = match { it.region == "Valparaiso" && it.comuna == "Viña del Mar" }
            )
        }
        // Debe haber mensaje de éxito
        assertNotNull(viewModel.uiState.value.successMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `saveChanges maneja error del servidor`() = runTest {
        // DADO: El repositorio falla
        coEvery { mockRepository.updateProfile(any(), any()) } throws Exception("Error 500")

        // CUANDO
        viewModel.saveChanges()
        testDispatcher.scheduler.advanceUntilIdle()

        // ENTONCES
        assertNotNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

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

    @Test
    fun `updateProfileImage guarda la uri en sessionManager`() = runTest {
        val nuevaUri = "file://nueva_foto.jpg"

        viewModel.updateProfileImage(nuevaUri)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockSessionManager.saveProfileImage(nuevaUri) }
    }
}