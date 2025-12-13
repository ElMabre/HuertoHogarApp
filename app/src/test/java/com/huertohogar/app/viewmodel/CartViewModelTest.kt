package com.huertohogar.app.viewmodel

import android.app.Application
import com.huertohogar.app.data.local.datastorage.SessionManager
import com.huertohogar.app.data.remote.model.PedidoResponseDto
import com.huertohogar.app.data.repository.CartRepository
import com.huertohogar.app.data.repository.OrderRepository
import com.huertohogar.app.model.CartItem
import com.huertohogar.app.model.Producto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    private lateinit var viewModel: CartViewModel

    private val mockCartRepository = mockk<CartRepository>(relaxed = true)
    private val mockOrderRepository = mockk<OrderRepository>(relaxed = true)
    private val mockSessionManager = mockk<SessionManager>(relaxed = true)
    private val mockApplication = mockk<Application>(relaxed = true)

    private val dbFlow = MutableStateFlow<List<CartItem>>(emptyList())
    private val testDispatcher = StandardTestDispatcher()

    // Definición de Producto
    private val productoPrueba = Producto(
        id = "1",
        databaseId = 100L,
        nombre = "Manzana",
        descripcion = "Roja y dulce",
        precio = 1000.0,
        stock = 10,
        categoria = "Frutas",
        imagenUrl = "http://img.com/a.png",
        origen = "Chile",
        unidad = "Kg"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Simulamos usuario logueado con ID 10
        every { mockSessionManager.userId } returns flowOf(10L)
        // Simulamos el flujo del carrito
        every { mockCartRepository.getCartItems(any()) } returns dbFlow

        // Inyección de dependencias en el constructor
        viewModel = CartViewModel(
            mockApplication,
            mockSessionManager,
            mockCartRepository,
            mockOrderRepository
        )

        // Ejecutar tareas iniciales (el init del ViewModel)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addToCart llama a insertCartItem en el repositorio`() = runTest {
        viewModel.addToCart(productoPrueba)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { mockCartRepository.addToCart(10L, productoPrueba) }
    }

    @Test
    fun `removeFromCart llama a deleteCartItem en el repositorio`() = runTest {
        viewModel.removeFromCart(productoPrueba.id)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { mockCartRepository.removeFromCart(10L, productoPrueba.id) }
    }

    @Test
    fun `clearCart llama a clearCart en el repositorio`() = runTest {
        viewModel.clearCart()
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { mockCartRepository.clearCart(10L) }
    }

    @Test
    fun `total incluye costo de envio (3500) cuando hay productos`() = runTest {
        val itemsDesdeBD = listOf(CartItem(productoPrueba, 2))
        dbFlow.emit(itemsDesdeBD)
        // IMPORTANTE: Dejar que el ViewModel procese el emit antes de verificar
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(5500.0, state.total, 0.0)
        assertEquals(3500.0, state.costoEnvio, 0.0)
        assertEquals(1, state.items.size)
    }

    @Test
    fun `total es cero y sin envio si el carrito esta vacio`() = runTest {
        dbFlow.emit(emptyList())
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0.0, state.total, 0.0)
        assertEquals(0.0, state.costoEnvio, 0.0)
        assertTrue(state.items.isEmpty())
    }

    @Test
    fun `realizarPedido exitoso limpia el carrito`() = runTest {
        // 1. Emitir items al carrito
        dbFlow.emit(listOf(CartItem(productoPrueba, 1)))

        // 2. CORRECCIÓN CLAVE: Esperar a que el ViewModel reciba los items y actualice el estado
        testDispatcher.scheduler.advanceUntilIdle()

        every { mockSessionManager.authToken } returns flowOf("fake-token")

        val responseDto = PedidoResponseDto(
            id = 99L,
            fecha = "2023",
            estado = "PENDIENTE",
            total = 5500.0,
            metodoPago = "EFECTIVO"
        )
        coEvery { mockOrderRepository.createOrder(any(), any(), any()) } returns Response.success(responseDto)

        // 3. Ahora sí llamamos a realizarPedido, sabiendo que items no está vacío
        viewModel.realizarPedido()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.checkoutSuccess)
        coVerify { mockCartRepository.clearCart(10L) }
    }

    @Test
    fun `realizarPedido falla si no hay usuario logueado`() = runTest {
        // 1. Emitir items
        dbFlow.emit(listOf(CartItem(productoPrueba, 1)))

        // 2. CORRECCIÓN CLAVE: Esperar actualización de estado
        testDispatcher.scheduler.advanceUntilIdle()

        every { mockSessionManager.authToken } returns flowOf("") // Token vacío simula error sesión

        viewModel.realizarPedido()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.checkoutError)
        coVerify(exactly = 0) { mockOrderRepository.createOrder(any(), any(), any()) }
    }
}