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

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeViewModelTest {

    private lateinit var viewModel: RecipeViewModel
    private val mockRepository = mockk<RecipeRepository>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Instanciamos el ViewModel
        // OJO: RecipeViewModel tiene un init { searchRecipes("Chicken") },
        // así que apenas se cree, intentará llamar al repositorio real si no lo interceptamos antes o justo después.
        // Como en tu código el repositorio se crea dentro (private val repository = RecipeRepository()),
        // lo ideal es usar reflexión inmediatamente después de construirlo para reemplazarlo por el mock.
        viewModel = RecipeViewModel()

        val repoField = RecipeViewModel::class.java.getDeclaredField("repository")
        repoField.isAccessible = true
        repoField.set(viewModel, mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchRecipes actualiza lista de recetas exitosamente`() = runTest {
        // DADO: Una lista simulada de recetas
        val listaMock = listOf(
            RecipeDto(id = "1", name = "Pollo Asado", imageUrl = "url1"),
            RecipeDto(id = "2", name = "Cazuela", imageUrl = "url2")
        )
        coEvery { mockRepository.getRecipes("Pollo") } returns listaMock

        // CUANDO: Buscamos "Pollo"
        viewModel.searchRecipes("Pollo")
        testDispatcher.scheduler.advanceUntilIdle()

        // ENTONCES
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.recipes.size)
        assertEquals("Pollo Asado", state.recipes[0].name)
        assertNull(state.error)
    }

    @Test
    fun `searchRecipes maneja error de conexion`() = runTest {
        // DADO: El repositorio falla
        coEvery { mockRepository.getRecipes(any()) } throws Exception("Sin internet")

        // CUANDO
        viewModel.searchRecipes("Beef")
        testDispatcher.scheduler.advanceUntilIdle()

        // ENTONCES
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.recipes.isEmpty())
        assertNotNull(state.error)
    }

    @Test
    fun `getRecipeDetail carga detalle correctamente`() = runTest {
        // DADO: Un detalle simulado
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

        // CUANDO
        viewModel.getRecipeDetail(idPlato)
        testDispatcher.scheduler.advanceUntilIdle()

        // ENTONCES
        val state = viewModel.uiState.value
        assertNotNull(state.selectedRecipe)
        assertEquals("Pizza", state.selectedRecipe?.name)
        assertEquals("Italian", state.selectedRecipe?.area)
    }

    @Test
    fun `clearSelectedRecipe limpia el detalle seleccionado`() = runTest {
        // DADO: Un estado con un detalle ya cargado (simulamos que ya se cargó uno)
        val detalleMock = RecipeDetailDto("1", "A", "B", "C", "D", "E")

        // Forzamos el estado inicial para este test (o llamamos a getRecipeDetail primero)
        coEvery { mockRepository.getRecipeDetail("1") } returns detalleMock
        viewModel.getRecipeDetail("1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.selectedRecipe) // Pre-condición

        // CUANDO: Llamamos a limpiar
        viewModel.clearSelectedRecipe()

        // ENTONCES: Debe ser nulo
        assertNull(viewModel.uiState.value.selectedRecipe)
    }
}