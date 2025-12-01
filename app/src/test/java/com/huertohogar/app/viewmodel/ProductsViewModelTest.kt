package com.huertohogar.app.viewmodel

import android.app.Application
import com.huertohogar.app.data.repository.ProductRepository
import com.huertohogar.app.model.Producto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

// clase de pruebas para la pantalla de productos
// verificamos que la lista cargue bien y que maneje los errores si se cae la red
@OptIn(ExperimentalCoroutinesApi::class)
class ProductsViewModelTest {

    // ViewModel que será probado
    private lateinit var viewModel: ProductsViewModel

    // mocks para simular el backend y la app.
    // el relaxed sirve para que no tire error si llamamos a algo que no configuramos
    private val mockRepository = mockk<ProductRepository>(relaxed = true)

    // Application simulado para inicializar el ViewModel
    private val mockApplication = mockk<Application>(relaxed = true)

    // dispatcher especial para que las corrutinas corran en orden en los tests
    private val testDispatcher = StandardTestDispatcher()

    // configuracion antes de cada test.
    // aqui uso un truco medio sucio con reflexion para inyectar el mock porque el repo es privado
    @Before
    fun setUp() {
        // Reemplaza Dispatchers.Main por uno controlable en pruebas
        Dispatchers.setMain(testDispatcher)

        // Crea el ViewModel usando la aplicación simulada
        viewModel = ProductsViewModel(mockApplication)

        // Inyección manual del repositorio real → reemplazado por el mock
        val repoField = ProductsViewModel::class.java.getDeclaredField("repository")
        repoField.isAccessible = true
        repoField.set(viewModel, mockRepository)
    }

    // limpieza despues de probar
    @After
    fun tearDown() {
        // Restaura el dispatcher original
        Dispatchers.resetMain()
    }

    // test del camino feliz.
    // le decimos al mock que devuelva datos y revisamos que el estado se actualice con esos productos
    @Test
    fun `cargarProductos actualiza el estado con lista de productos al tener exito`() = runTest {
        // Datos simulados devueltos por el repositorio
        val productosFicticios = listOf(
            Producto("P1", 1L, "Tomate", "Rojo", 100.0, 10, "Verdura", "", "Chile", "kg"),
            Producto("P2", 2L, "Lechuga", "Verde", 500.0, 5, "Verdura", "", "Chile", "un")
        )

        // Se configura el mock para que retorne la lista anterior
        coEvery { mockRepository.getAllProducts() } returns productosFicticios

        // Ejecuta la función del ViewModel
        viewModel.cargarProductos()

        // Avanza el dispatcher hasta que todo termine
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificaciones del estado
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.productos.size)
        assertEquals("Tomate", state.productos[0].nombre)
        assertNull(state.errorMessage)
    }

    // test de fallo. si el repo tira excepcion (ej: sin internet)
    // el viewmodel debe capturarlo y poner el mensaje de error en la UI
    @Test
    fun `cargarProductos maneja errores y actualiza mensaje de error`() = runTest {
        // Se simula un error al pedir los productos
        coEvery { mockRepository.getAllProducts() } throws Exception("Error de red simulado")

        // Ejecuta la función
        viewModel.cargarProductos()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verifica que se manejó el error correctamente
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.productos.isEmpty())
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage!!.contains("Error de red"))
    }
}