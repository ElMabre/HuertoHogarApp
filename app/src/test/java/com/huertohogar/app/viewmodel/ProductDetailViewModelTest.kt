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
import org.junit.Before
import org.junit.Test

// pruebas para ver el detalle del producto
// revisamos que cargue la info bien y que no explote si el id esta mal o no hay internet
@OptIn(ExperimentalCoroutinesApi::class)
class ProductDetailViewModelTest {

    private lateinit var viewModel: ProductDetailViewModel

    // mocks basicos. usamos relaxed=true para que no molesten con errores si no configuramos todo
    private val mockRepository = mockk<ProductRepository>(relaxed = true)
    private val mockApplication = mockk<Application>(relaxed = true)

    // dispatcher para que las corrutinas no den problemas en los tests y corran sincronizadas
    private val testDispatcher = StandardTestDispatcher()

    // configuracion inicial. aqui hay un truco importante con reflexion
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ProductDetailViewModel(mockApplication)

        // inyeccion "a la fuerza" usando reflexion porque la variable repository es privada en el viewmodel
        // es un poco sucio pero necesario si no queremos cambiar el codigo original solo para testear
        val repoField = ProductDetailViewModel::class.java.getDeclaredField("repository")
        repoField.isAccessible = true
        repoField.set(viewModel, mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // caso feliz: simulamos que el repo encuentra el producto
    // revisamos que la UI muestre los datos y quite el loading
    @Test
    fun `loadProductDetails carga producto exitosamente`() = runTest {
        // DADO: Un ID válido y un repositorio que devuelve un producto simulado
        val idPrueba = "TOM-01"
        val productoEsperado = Producto(
            id = idPrueba,
            databaseId = 55L,
            nombre = "Tomate Limachino",
            descripcion = "Fresco",
            precio = 1500.0,
            stock = 20,
            categoria = "Verduras",
            imagenUrl = "",
            origen = "Limache",
            unidad = "kg"
        )

        // Entrenamos al mock para que devuelva nuestro producto
        coEvery { mockRepository.getProductById(idPrueba) } returns productoEsperado

        // CUANDO: Solicitamos el detalle al ViewModel
        viewModel.loadProductDetails(idPrueba)

        // Avanzamos el tiempo para que la corrutina termine
        testDispatcher.scheduler.advanceUntilIdle()

        // ENTONCES: Verificamos que el estado de la UI sea el correcto
        val state = viewModel.uiState.value
        assertFalse(state.isLoading) // Ya no debería estar cargando
        assertNotNull(state.producto) // Debería haber un producto
        assertEquals("Tomate Limachino", state.producto?.nombre) // El nombre debe coincidir
        assertNull(state.error) // No debe haber errores
    }

    // probamos que pasa si buscamos un id que no existe
    // el repo devuelve null y nosotros esperamos un mensaje de error en la pantalla
    @Test
    fun `loadProductDetails muestra error si el producto no existe`() = runTest {
        // DADO: Un ID que el repositorio no encuentra (devuelve null)
        val idInexistente = "FANTASMA"
        coEvery { mockRepository.getProductById(idInexistente) } returns null

        // CUANDO
        viewModel.loadProductDetails(idInexistente)
        testDispatcher.scheduler.advanceUntilIdle()

        // ENTONCES
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.producto)
        assertNotNull(state.error) // Debe haber un mensaje de error
        // Verificamos que el mensaje contenga palabras clave esperadas
        assert(state.error!!.contains("no existe") || state.error!!.contains("eliminado"))
    }

    // simulacion de fallo de red. si el repo tira excepcion
    // el viewmodel tiene que atraparla y mostrar mensaje en vez de cerrarse
    @Test
    fun `loadProductDetails maneja excepciones de red`() = runTest {
        // DADO: El repositorio falla lanzando una excepción (simulando error de internet)
        val idPrueba = "TOM-01"
        coEvery { mockRepository.getProductById(idPrueba) } throws Exception("Fallo de conexión")

        // CUANDO
        viewModel.loadProductDetails(idPrueba)
        testDispatcher.scheduler.advanceUntilIdle()

        // ENTONCES
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.producto)
        assertNotNull(state.error)
        assert(state.error!!.contains("Fallo de conexión"))
    }

    // test extra para coverage. si llega un id nulo
    // tiene que validar antes de intentar buscar y gastar recursos
    @Test
    fun `loadProductDetails con ID nulo muestra error`() = runTest {
        // ESTE ES EL TEST NUEVO PARA MEJORAR EL COVERAGE

        // CUANDO: Llamamos a la función pasando null
        viewModel.loadProductDetails(null)

        // No es necesario advanceUntilIdle() porque la validación de null ocurre antes de lanzar la corrutina

        // ENTONCES
        val state = viewModel.uiState.value
        assertFalse(state.isLoading) // No debe quedar cargando
        assertNotNull(state.error)   // Debe haber error
        assertEquals("ID inválido", state.error) // El mensaje exacto del ViewModel
    }
}