package com.huertohogar.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.huertohogar.app.navigation.AppScreens
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

    // Variable local para visibilidad de la contraseña
    var passwordVisible by remember { mutableStateOf(false) }

    // Obtenemos comunas según la región seleccionada
    val comunasDisponibles = ChileLocations.regionesYComunas[state.region] ?: emptyList()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Scroll si el contenido es largo
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Crear Cuenta", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)

            // --- ERROR GLOBAL ---
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

            // --- NOMBRE ---
            OutlinedTextField(
                value = state.nombre,
                onValueChange = { viewModel.onNombreChange(it) },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.errors.nombre != null,
                supportingText = { state.errors.nombre?.let { error -> Text(error) } },
                singleLine = true
            )

            // --- APELLIDO ---
            OutlinedTextField(
                value = state.apellido,
                onValueChange = { viewModel.onApellidoChange(it) },
                label = { Text("Apellido") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.errors.apellido != null,
                supportingText = { state.errors.apellido?.let { error -> Text(error) } },
                singleLine = true
            )

            // --- RUN ---
            OutlinedTextField(
                value = state.run,
                onValueChange = { viewModel.onRunChange(it) },
                label = { Text("RUN") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.errors.run != null,
                supportingText = { state.errors.run?.let { error -> Text(error) } },
                singleLine = true
            )

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
                    isError = state.errors.region != null,
                    supportingText = { state.errors.region?.let { error -> Text(error) } }
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

            // --- SELECTOR DE COMUNA ---
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
                    isError = state.errors.comuna != null,
                    supportingText = { state.errors.comuna?.let { error -> Text(error) } }
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

            // --- DIRECCIÓN ---
            OutlinedTextField(
                value = state.direccion,
                onValueChange = { viewModel.onDireccionChange(it) },
                label = { Text("Dirección (Calle y Número)") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.errors.direccion != null,
                supportingText = { state.errors.direccion?.let { error -> Text(error) } },
                singleLine = true
            )

            // --- EMAIL ---
            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = state.errors.email != null,
                supportingText = { state.errors.email?.let { error -> Text(error) } },
                singleLine = true
            )

            // --- PASSWORD ---
            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = state.errors.password != null,
                supportingText = { state.errors.password?.let { error -> Text(error) } },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- BOTÓN DE REGISTRO ---
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        viewModel.onRegisterClicked {
                            // Al ser exitoso, navegamos al Home
                            navController.navigate(AppScreens.HomeScreen.route) {
                                popUpTo(AppScreens.LoginScreen.route) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Registrarse")
                }
            }

            // Link para volver al Login
            TextButton(onClick = { navController.navigate(AppScreens.LoginScreen.route) }) {
                Text("¿Ya tienes cuenta? Iniciar Sesión")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}