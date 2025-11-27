package com.huertohogar.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.huertohogar.app.utils.ChileLocations
import com.huertohogar.app.viewmodel.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Variables para controlar los dropdowns
    var regionExpanded by remember { mutableStateOf(false) }
    var comunaExpanded by remember { mutableStateOf(false) }

    // Obtenemos comunas según la región seleccionada
    val comunasDisponibles = ChileLocations.regionesYComunas[state.region] ?: emptyList()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Permite scroll si el teclado tapa
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Crear Cuenta", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)

            // --- ERROR GLOBAL (ARRIBITA) ---
            AnimatedVisibility(visible = state.registerErrorGlobal != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.registerErrorGlobal ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Campos Básicos
            OutlinedTextField(
                value = state.nombre,
                onValueChange = { viewModel.onNombreChange(it) },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.errors.nombre != null
            )
            // ... (Apellido, RUN igual que antes) ...

            // --- SELECTOR DE REGIÓN ---
            ExposedDropdownMenuBox(
                expanded = regionExpanded,
                onExpandedChange = { regionExpanded = !regionExpanded }
            ) {
                OutlinedTextField(
                    value = state.region,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Región") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = regionExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    isError = state.errors.region != null
                )
                ExposedDropdownMenu(
                    expanded = regionExpanded,
                    onDismissRequest = { regionExpanded = false }
                ) {
                    ChileLocations.regiones.forEach { region ->
                        DropdownMenuItem(
                            text = { Text(region) },
                            onClick = {
                                viewModel.onRegionSelected(region)
                                regionExpanded = false
                            }
                        )
                    }
                }
            }

            // --- SELECTOR DE COMUNA (Activo solo si hay región) ---
            ExposedDropdownMenuBox(
                expanded = comunaExpanded,
                onExpandedChange = { if (state.region.isNotEmpty()) comunaExpanded = !comunaExpanded }
            ) {
                OutlinedTextField(
                    value = state.comuna,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Comuna") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = comunaExpanded) },
                    enabled = state.region.isNotEmpty(), // Deshabilitado si no hay región
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    isError = state.errors.comuna != null
                )
                ExposedDropdownMenu(
                    expanded = comunaExpanded,
                    onDismissRequest = { comunaExpanded = false }
                ) {
                    comunasDisponibles.forEach { comuna ->
                        DropdownMenuItem(
                            text = { Text(comuna) },
                            onClick = {
                                viewModel.onComunaSelected(comuna)
                                comunaExpanded = false
                            }
                        )
                    }
                }
            }

            // --- CAMPO DIRECCIÓN ---
            OutlinedTextField(
                value = state.direccion,
                onValueChange = { viewModel.onDireccionChange(it) },
                label = { Text("Dirección (Calle y Número)") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.errors.direccion != null
            )

            // ... (Email, Passwords, Botón Registrar igual que antes) ...

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        viewModel.onRegisterClicked {
                            navController.navigate("home_screen") { popUpTo("login_screen") { inclusive = true } }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Registrarse")
                }
            }
        }
    }
}