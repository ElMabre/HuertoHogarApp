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

// Pantalla principal donde buscamos recetas.
// Aquí junto todo: la barra de búsqueda, la lista de resultados y el aviso de carga.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    navController: NavController,
    recipeViewModel: RecipeViewModel = viewModel(),
    cartViewModel: CartViewModel
) {
    // Saco el estado del ViewModel para saber si está cargando o si ya hay recetas listas.
    val uiState by recipeViewModel.uiState.collectAsState()

    // Variable para guardar lo que escribo en el buscador antes de darle al botón.
    var searchQuery by remember { mutableStateOf("") }

    // Lógica para mostrar la ventana flotante (Pop-up).
    // Si hay una receta seleccionada en el estado, dibujo el diálogo. Si no, no se ve nada.
    if (uiState.selectedRecipe != null) {
        RecipeDetailDialog(
            recipe = uiState.selectedRecipe!!,
            onDismiss = {
                // Cuando cierro el diálogo, limpio la receta seleccionada para que desaparezca la ventana.
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
            // Campo de texto para buscar.
            // Cuando le doy a la lupa, llamo al ViewModel para que busque los datos.
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

            // Aquí decido qué mostrar según cómo va la carga:
            // Un círculo girando, un mensaje de error, o la lista si todo salió bien.
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
                        // Lista de recetas en forma de rejilla (cuadritos).
                        // Uso 'Adaptive' para que el tamaño de las columnas se ajuste solo al móvil.
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.recipes) { recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    onClick = {
                                        // Si toco una tarjeta, pido los detalles para abrir el diálogo.
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

// Diseño de cada tarjetita de la lista.
// Es simple: solo muestra la foto y el nombre de la receta.
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

// Ventana flotante (Dialog) con los detalles.
// Le puse un scroll porque a veces las instrucciones son muy largas y no caben en la pantalla.
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
                .fillMaxHeight(0.85f) // Que ocupe casi toda la altura para que se vea bien
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()) // Esto permite bajar si hay mucho texto
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