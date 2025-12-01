package com.huertohogar.app.viewmodel

import android.app.Application
import com.huertohogar.app.data.local.datastorage.SessionManager
import com.huertohogar.app.data.remote.model.PedidoResponseDto
import com.huertohogar.app.data.repository.OrderRepository
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

// clase de pruebas para el historial de pedidos
// revisamos que cargue la lista y funcionen las cancelaciones sin necesitar el backend real
@OptIn(ExperimentalCoroutinesApi::class)
class OrderViewModelTest {

    private lateinit var viewModel: OrderViewModel

    // mocks para simular las dependencias. relaxed=true ayuda a no configurar cada respuesta pequeña
    private val mockOrderRepository = mockk<OrderRepository>(relaxed = true)
    private val mockSessionManager = mockk<SessionManager>(relaxed = true)
    private val mockApplication = mockk<Application>(relaxed = true)

    // dispatcher necesario para que las corrutinas funcionen bien en los tests
    private val testDispatcher = StandardTestDispatcher()

    // esto corre antes de cada test.
    // por defecto, simulo que el usuario SI tiene sesion (token valido) para ahorrar codigo en los tests normales
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Configuramos comportamiento base de la sesión (Token válido por defecto)
        every { mockSessionManager.authToken } returns flowOf("fake-token-123")

        // Instanciamos el ViewModel para la mayoría de los tests
        viewModel = OrderViewModel(mockApplication)
        viewModel.orderRepository = mockOrderRepository
        viewModel.sessionManager = mockSessionManager
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // prueba feliz: descarga los pedidos y revisa que el mas nuevo (id mayor) quede primero
    @Test
    fun `loadMyOrders carga pedidos exitosamente y los ordena por ID`() = runTest {
        // DADO: Lista de pedidos desordenada
        val pedidoAntiguo = PedidoResponseDto(1L, "Entregado", 5000.0, "Efectivo", "2023-01-01", null)
        val pedidoNuevo = PedidoResponseDto(2L, "Pendiente", 10000.0, "Webpay", "2023-01-02", null)
        val listaDesordenada = listOf(pedidoAntiguo, pedidoNuevo)

        coEvery { mockOrderRepository.getMyOrders(any()) } returns Response.success(listaDesordenada)

        // CUANDO
        // Limpiamos invocaciones previas del init
        io.mockk.clearMocks(mockOrderRepository, answers = false)

        viewModel.loadMyOrders()
        testDispatcher.scheduler.advanceUntilIdle()

        // ENTONCES
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.pedidos.size)

        // Validar ordenamiento (el ID 2 debe ir primero)
        assertEquals(2L, state.pedidos[0].id)
    }

    // prueba de seguridad: si se borro el token, no deberia intentar cargar pedidos
    // creo un viewmodel local aqui para asegurarme que este limpio desde cero y no use el del setup
    @Test
    fun `loadMyOrders muestra error si no hay usuario logueado`() = runTest {
        // DADO: Token vacío
        every { mockSessionManager.authToken } returns flowOf("")

        // SOLUCIÓN: Usamos un repositorio mock NUEVO y EXCLUSIVO para este test.
        // Así evitamos que la llamada del 'viewModel' creado en setUp() interfiera en la verificación.
        val localMockRepository = mockk<OrderRepository>(relaxed = true)

        val localViewModel = OrderViewModel(mockApplication)
        localViewModel.orderRepository = localMockRepository // Inyectamos el mock local
        localViewModel.sessionManager = mockSessionManager

        // CUANDO
        localViewModel.loadMyOrders()
        testDispatcher.scheduler.advanceUntilIdle()

        // ENTONCES
        val state = localViewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("No hay sesión"))

        // Verificamos sobre el mock LOCAL, que nadie más ha tocado.
        coVerify(exactly = 0) { localMockRepository.getMyOrders(any()) }
    }

    // prueba de interaccion: cancelar una orden.
    // verifica que despues de cancelar, se recargue la lista automaticamente para ver los cambios
    @Test
    fun `cancelarPedido exitoso muestra mensaje y recarga lista`() = runTest {
        // DADO: Cancelación exitosa
        val idPedido = 10L
        coEvery { mockOrderRepository.cancelOrder(any(), idPedido) } returns Response.success(null)

        // Simular recarga posterior vacía
        coEvery { mockOrderRepository.getMyOrders(any()) } returns Response.success(emptyList())

        // CUANDO
        viewModel.cancelarPedido(idPedido)
        testDispatcher.scheduler.advanceUntilIdle()

        // ENTONCES
        val state = viewModel.uiState.value
        assertEquals("Pedido cancelado exitosamente", state.message)

        // Verificar flujo completo: Cancelar -> Recargar
        coVerify { mockOrderRepository.cancelOrder(any(), idPedido) }
        // Verificamos al menos 1 llamada (puede haber más por el init, pero importa que se llame)
        coVerify(atLeast = 1) { mockOrderRepository.getMyOrders(any()) }
    }

    // prueba de manejo de errores
    // si el servidor dice que no se puede cancelar (ej: error 400), hay que avisarle al usuario y no crashear
    @Test
    fun `cancelarPedido falla si el backend retorna error`() = runTest {
        // DADO: Error 400 (ej: pedido ya enviado)
        val errorBody = "Error".toResponseBody("text/plain".toMediaTypeOrNull())
        coEvery { mockOrderRepository.cancelOrder(any(), any()) } returns Response.error(400, errorBody)

        // CUANDO
        viewModel.cancelarPedido(10L)
        testDispatcher.scheduler.advanceUntilIdle()

        // ENTONCES
        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("No se puede cancelar"))
    }
}