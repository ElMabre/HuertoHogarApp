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

// Pantalla de Registro.
// Aquí está todo el formulario para crear usuarios nuevos.
// Usamos el ViewModel para guardar lo que escribe el usuario y no perderlo si gira la pantalla.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = viewModel()
) {
    // Conectamos con el ViewModel.
    // 'collectAsState' hace que la pantalla se vuelva a dibujar sola si cambian los datos (ej: sale un error).
    val state by viewModel.uiState.collectAsState()

    // Estados solo visuales (UI).
    // Estos no van al ViewModel porque solo controlan si un menú está abierto o cerrado.
    var regionExpanded by remember { mutableStateOf(false) }
    var comunaExpanded by remember { mutableStateOf(false) }

    // Control para mostrar u ocultar la contraseña (los puntitos).
    var passwordVisible by remember { mutableStateOf(false) }

    // Lógica para filtrar comunas.
    // Esta lista cambia automáticamente cuando el usuario elige una región diferente en 'state.region'.
    val comunasDisponibles = ChileLocations.regionesYComunas[state.region] ?: emptyList()

    Scaffold { innerPadding ->
        // Contenedor principal con Scroll.
        // Es MUY importante poner 'verticalScroll' porque en móviles pequeños o cuando sale el teclado,
        // el botón de "Registrar" se quedaría fuera de la pantalla.
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Crear Cuenta", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)

            // Mensaje de Error Animado.
            // Si hay un error global (como "El usuario ya existe"), aparece suavemente en vez de saltar de golpe.
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

            // --- CAMPOS DE TEXTO ---
            // Todos funcionan igual: muestran el valor del estado y avisan al ViewModel cuando escribes algo.

            // Nombre
            OutlinedTextField(
                value = state.nombre,
                onValueChange = { viewModel.onNombreChange(it) },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.errors.nombre != null,
                supportingText = { state.errors.nombre?.let { error -> Text(error) } },
                singleLine = true
            )

            // Apellido
            OutlinedTextField(
                value = state.apellido,
                onValueChange = { viewModel.onApellidoChange(it) },
                label = { Text("Apellido") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.errors.apellido != null,
                supportingText = { state.errors.apellido?.let { error -> Text(error) } },
                singleLine = true
            )

            // RUN
            OutlinedTextField(
                value = state.run,
                onValueChange = { viewModel.onRunChange(it) },
                label = { Text("RUN") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.errors.run != null,
                supportingText = { state.errors.run?.let { error -> Text(error) } },
                singleLine = true
            )

            // --- MENÚS DESPLEGABLES (DROPDOWNS) ---
            // Usamos 'ExposedDropdownMenuBox' que es el estándar moderno de Android.

            // Selector de Región
            ExposedDropdownMenuBox(
                expanded = regionExpanded,
                onExpandedChange = { regionExpanded = !regionExpanded }
            ) {
                OutlinedTextField(
                    value = state.region,
                    onValueChange = {}, // Vacío porque el usuario no escribe, solo selecciona
                    readOnly = true,    // Para que no salga el teclado
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

            // Selector de Comuna (Dependiente)
            // Este menú es "listo": se bloquea (enabled = false) si no has elegido región primero.
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
                    enabled = state.region.isNotEmpty(), // Aquí está el truco para bloquearlo
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

            // Dirección
            OutlinedTextField(
                value = state.direccion,
                onValueChange = { viewModel.onDireccionChange(it) },
                label = { Text("Dirección (Calle y Número)") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.errors.direccion != null,
                supportingText = { state.errors.direccion?.let { error -> Text(error) } },
                singleLine = true
            )

            // Email (con teclado especial de correo)
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

            // Contraseña (con el ojito para ver/ocultar)
            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                // Aquí cambiamos entre ver texto normal o puntitos
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

            // Botón de Registrarse.
            // Si está cargando muestra el círculo, si no, muestra el botón.
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        // Enviamos la función de navegar como "callback".
                        // Así solo cambiamos de pantalla si el registro funcionó de verdad.
                        viewModel.onRegisterClicked {
                            navController.navigate(AppScreens.HomeScreen.route) {
                                // Esto borra el historial para que no puedas volver al registro con "atrás"
                                popUpTo(AppScreens.LoginScreen.route) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Registrarse")
                }
            }

            // Link simple para ir al Login si te equivocaste de pantalla
            TextButton(onClick = { navController.navigate(AppScreens.LoginScreen.route) }) {
                Text("¿Ya tienes cuenta? Iniciar Sesión")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}