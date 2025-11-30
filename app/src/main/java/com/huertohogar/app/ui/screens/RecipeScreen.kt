package com.huertohogar.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.huertohogar.app.data.remote.model.RecipeDetailDto
import com.huertohogar.app.data.remote.model.RecipeDto
import com.huertohogar.app.ui.components.HuertoTopAppBar
import com.huertohogar.app.viewmodel.CartViewModel
import com.huertohogar.app.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    navController: NavController,
    recipeViewModel: RecipeViewModel = viewModel(),
    cartViewModel: CartViewModel
) {
    val uiState by recipeViewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // --- LÓGICA DEL DIÁLOGO ---
    // Si 'selectedRecipe' tiene datos, mostramos el Pop-up automáticamente.
    if (uiState.selectedRecipe != null) {
        RecipeDetailDialog(
            recipe = uiState.selectedRecipe!!,
            onDismiss = {
                // Al cerrar, limpiamos el estado para volver a la lista
                recipeViewModel.clearSelectedRecipe()
            }
        )
    }

    Scaffold(
        topBar = {
            HuertoTopAppBar(
                title = "Recetas del Mundo",
                canNavigateBack = true,
                navController = navController,
                cartViewModel = cartViewModel
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar por ingrediente (ej: Chicken)") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        if (searchQuery.isNotBlank()) {
                            recipeViewModel.searchRecipes(searchQuery)
                        }
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    uiState.error != null -> {
                        Text(
                            text = uiState.error ?: "Error",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.recipes.isEmpty() -> {
                        Text(
                            text = "No se encontraron recetas. Prueba con ingredientes en inglés (ej: Potato, Beef, Rice).",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {
                        // Grilla de Resultados
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.recipes) { recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    onClick = {
                                        // Al hacer clic, pedimos el detalle al ViewModel
                                        recipeViewModel.getRecipeDetail(recipe.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeCard(
    recipe: RecipeDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(recipe.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = recipe.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp),
                maxLines = 2
            )
        }
    }
}

/**
 * Componente nuevo: El Pop-up con las instrucciones.
 */
@Composable
fun RecipeDetailDialog(
    recipe: RecipeDetailDto,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f) // Ocupa el 85% de la altura de la pantalla
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()) // Permite scroll si el texto es largo
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(recipe.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = recipe.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                if (!recipe.area.isNullOrBlank()) {
                    Text(text = "Origen: ${recipe.area}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Instrucciones:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = recipe.instructions, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Cerrar")
                }
            }
        }
    }
}