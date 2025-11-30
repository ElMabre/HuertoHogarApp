package com.huertohogar.app.viewmodel

import android.app.Application
import com.huertohogar.app.data.local.datastorage.SessionManager
import com.huertohogar.app.data.remote.model.PedidoResponseDto
import com.huertohogar.app.data.repository.OrderRepository
import com.huertohogar.app.model.Producto
import io.mockk.coEvery
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

    @Test
    fun `total se calcula correctamente`() {
        // 2 productos → 2000 + envío 3500 = 5500
        viewModel.addToCart(productoPrueba)
        viewModel.addToCart(productoPrueba)

        assertEquals(5500.0, viewModel.uiState.value.total, 0.0)
    }

    @Test
    fun `realizarPedido exitoso limpia el carrito`() = runTest {
        // Carrito con un producto
        viewModel.addToCart(productoPrueba)

        // Simular token válido
        every { mockSessionManager.authToken } returns flowOf("fake-token-123")

        // Respuesta del backend simulada
        val responseDto = PedidoResponseDto(
            id = 99L,
            fecha = "2023-12-01",
            estado = "PENDIENTE",
            total = 5500.0,
            metodoPago = "EFECTIVO"
        )

        // Mock del createOrder exitoso
        coEvery { mockOrderRepository.createOrder(any(), any(), any()) } returns Response.success(responseDto)

        // Ejecutar pedido
        viewModel.realizarPedido()
        testDispatcher.scheduler.advanceUntilIdle()

        // Debe indicar éxito y vaciar carrito
        assertTrue(viewModel.uiState.value.checkoutSuccess)
        assertTrue(viewModel.uiState.value.items.isEmpty())
    }

    @Test
    fun `realizarPedido falla si no hay usuario logueado`() = runTest {
        // Carrito con producto
        viewModel.addToCart(productoPrueba)

        // Token vacío
        every { mockSessionManager.authToken } returns flowOf("")

        // Intentar pedido
        viewModel.realizarPedido()
        testDispatcher.scheduler.advanceUntilIdle()

        // Debe mostrar error de sesión
        val errorMsg = viewModel.uiState.value.checkoutError
        assertTrue(errorMsg != null && errorMsg.contains("iniciar sesión"))
    }
}
