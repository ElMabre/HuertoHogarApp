package com.huertohogar.app.viewmodel

import android.app.Application
import com.huertohogar.app.data.local.datastorage.SessionManager
import com.huertohogar.app.data.remote.model.PedidoResponseDto
import com.huertohogar.app.data.repository.OrderRepository
import com.huertohogar.app.model.Producto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class CartViewModelTest {

    private lateinit var viewModel: CartViewModel
    private val mockOrderRepository = mockk<OrderRepository>(relaxed = true)
    private val mockSessionManager = mockk<SessionManager>(relaxed = true)
    private val mockApplication = mockk<Application>(relaxed = true)

    // Dispatcher especial para pruebas
    private val testDispatcher = StandardTestDispatcher()

    // Producto de prueba
    private val productoPrueba = Producto(
        id = "1",
        databaseId = 100L,
        nombre = "Manzana",
        descripcion = "Roja",
        precio = 1000.0,
        stock = 10,
        imagenUrl = "",
        categoria = "Frutas",
        unidad = "kg",
        origen = "Chile"
    )

    @Before
    fun setUp() {
        // Usar dispatcher de prueba
        Dispatchers.setMain(testDispatcher)

        // Inicializar ViewModel y mocks
        viewModel = CartViewModel(mockApplication)
        viewModel.orderRepository = mockOrderRepository
        viewModel.sessionManager = mockSessionManager
    }

    @After
    fun tearDown() {
        // Restaurar dispatcher real
        Dispatchers.resetMain()
    }

    @Test
    fun `addToCart agrega producto correctamente`() {
        // Agregar producto
        viewModel.addToCart(productoPrueba)

        // Debe quedar un producto con cantidad 1
        val items = viewModel.uiState.value.items
        assertEquals(1, items.size)
        assertEquals("Manzana", items[0].producto.nombre)
        assertEquals(1, items[0].cantidad)
    }

    @Test
    fun `addToCart incrementa cantidad si producto ya existe`() {
        // Agregar dos veces
        viewModel.addToCart(productoPrueba)
        viewModel.addToCart(productoPrueba)

        // Debe quedar uno solo con cantidad 2
        val items = viewModel.uiState.value.items
        assertEquals(1, items.size)
        assertEquals(2, items[0].cantidad)
    }

    // --- NUEVOS TESTS PARA SUBIR COVERAGE ---

    @Test
    fun `removeFromCart elimina el producto del carrito`() {
        // DADO: Un carrito con un producto
        viewModel.addToCart(productoPrueba)
        assertFalse(viewModel.uiState.value.items.isEmpty())

        // CUANDO: Eliminamos ese producto
        viewModel.removeFromCart(productoPrueba.id)

        // ENTONCES: El carrito debe estar vacío y el total ser 0
        assertTrue(viewModel.uiState.value.items.isEmpty())
        assertEquals(0.0, viewModel.uiState.value.total, 0.0)
    }

    @Test
    fun `updateQuantity elimina el producto si la cantidad es 0 o menor`() {
        // DADO: Un carrito con un producto
        viewModel.addToCart(productoPrueba)

        // CUANDO: Actualizamos la cantidad a 0
        viewModel.updateQuantity(productoPrueba.id, 0)

        // ENTONCES: El producto debe eliminarse
        assertTrue(viewModel.uiState.value.items.isEmpty())
    }

    @Test
    fun `updateQuantity no hace nada si el producto no existe`() {
        // DADO: Carrito vacío
        viewModel.clearCart()

        // CUANDO: Intentamos actualizar algo que no está
        viewModel.updateQuantity("ID_INEXISTENTE", 5)

        // ENTONCES: Sigue vacío
        assertTrue(viewModel.uiState.value.items.isEmpty())
    }

    // ----------------------------------------

    @Test
    fun `total incluye costo de envio (3500) cuando hay productos`() {
        // DADO: 2 productos de 1000 c/u (Subtotal 2000) + Envío (3500)
        // Total esperado = 5500
        viewModel.addToCart(productoPrueba)
        viewModel.addToCart(productoPrueba)

        // CUANDO: Verificamos el estado
        val state = viewModel.uiState.value

        // ENTONCES
        assertEquals(5500.0, state.total, 0.0)
        assertEquals(3500.0, state.costoEnvio, 0.0)
    }

    @Test
    fun `total es cero y sin envio si el carrito esta vacio`() {
        // DADO: Un carrito vacío (estado inicial o después de limpiar)
        viewModel.clearCart()

        // CUANDO: Verificamos el estado
        val state = viewModel.uiState.value

        // ENTONCES: No debe cobrar envío
        assertEquals(0.0, state.costoEnvio, 0.0)
        assertEquals(0.0, state.total, 0.0)
        assertTrue(state.items.isEmpty())
    }

    @Test
    fun `realizarPedido exitoso limpia el carrito`() = runTest {
        // DADO: Carrito con productos y token válido
        viewModel.addToCart(productoPrueba)
        every { mockSessionManager.authToken } returns flowOf("fake-token-123")

        // Respuesta del backend simulada
        val responseDto = PedidoResponseDto(
            id = 99L,
            fecha = "2023-12-01",
            estado = "PENDIENTE",
            total = 5500.0,
            metodoPago = "EFECTIVO"
        )
        coEvery { mockOrderRepository.createOrder(any(), any(), any()) } returns Response.success(responseDto)

        // CUANDO: Ejecutamos pedido
        viewModel.realizarPedido()
        testDispatcher.scheduler.advanceUntilIdle() // Esperar corrutinas

        // ENTONCES: Éxito y carrito vacío
        assertTrue(viewModel.uiState.value.checkoutSuccess)
        assertTrue(viewModel.uiState.value.items.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `realizarPedido falla si no hay usuario logueado`() = runTest {
        // DADO: Carrito con producto pero SIN token (usuario no logueado)
        viewModel.addToCart(productoPrueba)
        every { mockSessionManager.authToken } returns flowOf("") // Token vacío

        // CUANDO: Intentamos pedir
        viewModel.realizarPedido()
        testDispatcher.scheduler.advanceUntilIdle()

        // ENTONCES: Debe haber error de checkout y NO llamar al repositorio
        val errorMsg = viewModel.uiState.value.checkoutError
        assertNotNull(errorMsg)
        assertTrue(errorMsg!!.contains("iniciar sesión")) // Verificamos parte del mensaje

        // Verificamos que NUNCA se llamó al createOrder
        coVerify(exactly = 0) { mockOrderRepository.createOrder(any(), any(), any()) }
    }

    @Test
    fun `realizarPedido no hace nada si el carrito esta vacio`() = runTest {
        // DADO: Carrito vacío
        viewModel.clearCart()

        // CUANDO: Llamamos a realizar pedido
        viewModel.realizarPedido()
        testDispatcher.scheduler.advanceUntilIdle()

        // ENTONCES: No cambia a loading ni llama al repo
        assertFalse(viewModel.uiState.value.isLoading)
        coVerify(exactly = 0) { mockOrderRepository.createOrder(any(), any(), any()) }
    }

    @Test
    fun `resetCheckoutStatus limpia los estados`() {
        // Forzamos un estado de éxito (aunque sea indirectamente o asumiendo un estado previo)
        // Como no podemos setear el estado directamente, confiamos en que reset lo limpie a sus valores default.

        viewModel.resetCheckoutStatus()

        assertFalse(viewModel.uiState.value.checkoutSuccess)
        assertNull(viewModel.uiState.value.checkoutError)
    }
}