package com.huertohogar.app.viewmodel

import com.huertohogar.app.data.remote.model.RecipeDetailDto
import com.huertohogar.app.data.remote.model.RecipeDto
import com.huertohogar.app.data.repository.RecipeRepository
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

// Clase de pruebas unitarias para RecipeViewModel.
// Su objetivo es asegurar que la lógica de búsqueda y selección de recetas actualice el estado (UI State) correctamente sin depender de la red real.
@OptIn(ExperimentalCoroutinesApi::class)
class RecipeViewModelTest {

    private lateinit var viewModel: RecipeViewModel

    // Dependencias simuladas (Mocks) y configuración de hilos.
    // 'mockRepository' fingirá ser la conexión a internet.
    // 'testDispatcher' nos permite controlar el tiempo de ejecución de las corrutinas en los tests.
    private val mockRepository = mockk<RecipeRepository>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    // Configuración inicial (@Before).
    // Se ejecuta antes de cada test. Aquí configuramos el entorno de corrutinas y preparamos el ViewModel.
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Instanciación y "Truco" de Inyección con Reflexión.
        // Como el ViewModel crea su propio repositorio internamente (private val repository = ...),
        // usamos Reflexión (Java Reflection) para forzar el cambio de esa variable privada por nuestro mock.
        // Esto permite probar el ViewModel aislado, sin usar la red real.
        viewModel = RecipeViewModel()

        val repoField = RecipeViewModel::class.java.getDeclaredField("repository")
        repoField.isAccessible = true
        repoField.set(viewModel, mockRepository)
    }

    // Limpieza (@After).
    // Restablece el Dispatcher principal para evitar conflictos con otros tests que se ejecuten después.
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Test: Búsqueda exitosa ('Happy Path').
    // Verifica que, si el repositorio devuelve datos válidos, el ViewModel actualice la lista en el UI State.
    @Test
    fun `searchRecipes actualiza lista de recetas exitosamente`() = runTest {
        // DADO: Preparamos al mock para devolver una lista ficticia.
        val listaMock = listOf(
            RecipeDto(id = "1", name = "Pollo Asado", imageUrl = "url1"),
            RecipeDto(id = "2", name = "Cazuela", imageUrl = "url2")
        )
        coEvery { mockRepository.getRecipes("Pollo") } returns listaMock

        // CUANDO: El usuario busca "Pollo".
        viewModel.searchRecipes("Pollo")
        testDispatcher.scheduler.advanceUntilIdle()

        // ENTONCES: Verificamos que los datos mockeados estén en el estado y no haya errores.
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.recipes.size)
        assertEquals("Pollo Asado", state.recipes[0].name)
        assertNull(state.error)
    }

    // Test: Manejo de errores de red ('Sad Path').
    // Verifica que la app no se caiga si falla la petición, y que guarde el mensaje de error en el estado.
    @Test
    fun `searchRecipes maneja error de conexion`() = runTest {
        // DADO: Configuramos el mock para lanzar una excepción (simular caída de internet).
        coEvery { mockRepository.getRecipes(any()) } throws Exception("Sin internet")

        // CUANDO: Intentamos buscar.
        viewModel.searchRecipes("Beef")
        testDispatcher.scheduler.advanceUntilIdle()

        // ENTONCES: La lista debe estar vacía y el campo de error debe tener contenido.
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.recipes.isEmpty())
        assertNotNull(state.error)
    }

    // Test: Selección de receta individual.
    // Verifica que la lógica para traer el detalle de un platillo funcione y actualice 'selectedRecipe'.
    @Test
    fun `getRecipeDetail carga detalle correctamente`() = runTest {
        // DADO: Un objeto de detalle ficticio.
        val idPlato = "555"
        val detalleMock = RecipeDetailDto(
            id = idPlato,
            name = "Pizza",
            instructions = "Hornear por 20 min",
            imageUrl = "url_pizza",
            area = "Italian",
            category = "Main"
        )
        coEvery { mockRepository.getRecipeDetail(idPlato) } returns detalleMock

        // CUANDO: Solicitamos el detalle.
        viewModel.getRecipeDetail(idPlato)
        testDispatcher.scheduler.advanceUntilIdle()

        // ENTONCES: El estado debe contener exactamente los datos del mock.
        val state = viewModel.uiState.value
        assertNotNull(state.selectedRecipe)
        assertEquals("Pizza", state.selectedRecipe?.name)
        assertEquals("Italian", state.selectedRecipe?.area)
    }

    // Test: Limpieza de estado.
    // Verifica que la función para limpiar la selección funcione, útil para cuando el usuario vuelve atrás en la navegación.
    @Test
    fun `clearSelectedRecipe limpia el detalle seleccionado`() = runTest {
        // DADO: Pre-cargamos un estado con datos (simulando navegación previa).
        val detalleMock = RecipeDetailDto("1", "A", "B", "C", "D", "E")
        coEvery { mockRepository.getRecipeDetail("1") } returns detalleMock
        viewModel.getRecipeDetail("1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.selectedRecipe) // Verificación de seguridad

        // CUANDO: Ejecutamos la limpieza.
        viewModel.clearSelectedRecipe()

        // ENTONCES: La receta seleccionada debe volver a ser null.
        assertNull(viewModel.uiState.value.selectedRecipe)
    }
}