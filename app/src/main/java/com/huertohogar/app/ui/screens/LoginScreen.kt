package com.huertohogar.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.huertohogar.app.navigation.AppScreens
import com.huertohogar.app.ui.theme.HuertoHogarAppTheme
import com.huertohogar.app.viewmodel.LoginViewModel

// Pantalla de Autenticación.
// Implementa el patrón MVVM: La UI observa el estado (LoginUiState) y envía eventos al ViewModel.
// Se mantiene "limpia" de lógica de negocio (como validar correos o llamar APIs).
@Composable
fun LoginScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = viewModel()
) {
    // Suscripción reactiva al estado (StateFlow).
    // Cualquier cambio en 'uiState' (error, carga, visibilidad password) recompondrá la UI automáticamente.
    val uiState by loginViewModel.uiState.collectAsState()

    Scaffold { innerPadding ->
        // Contenedor principal con Scroll Vertical.
        // El 'verticalScroll' es crítico aquí: cuando el teclado virtual sube, reduce el espacio de pantalla.
        // Sin scroll, los campos inferiores o el botón de ingreso quedarían ocultos o inaccesibles.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Ingresa a tu cuenta para continuar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Campos de Texto (Inputs) Controlados.
            // Siguen el principio de "Single Source of Truth": el valor lo dicta el ViewModel, no el componente.
            // Se manejan errores visuales (rojo) y se deshabilitan durante la carga para evitar ediciones concurrentes.
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { loginViewModel.onEmailChange(it) },
                label = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.errors.email != null,
                supportingText = { if (uiState.errors.email != null) Text(uiState.errors.email!!) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                enabled = !uiState.isLoading
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.password,
                onValueChange = { loginViewModel.onPasswordChange(it) },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.errors.password != null,
                supportingText = { if (uiState.errors.password != null) Text(uiState.errors.password!!) },
                // Lógica de UI para ocultar/mostrar contraseña
                visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { loginViewModel.onTogglePasswordVisibility() }) {
                        Icon(
                            imageVector = if (uiState.passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                enabled = !uiState.isLoading
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Botón de Acción Principal con Callback de Navegación.
            // Separación de responsabilidades:
            // 1. ViewModel: Valida y autentica (Lógica).
            // 2. UI: Navega SOLAMENTE si el ViewModel confirma el éxito mediante la lambda.
            Button(
                onClick = {
                    loginViewModel.onLoginClicked {
                        navController.navigate(AppScreens.HomeScreen.route) {
                            // Limpiamos la pila de navegación para que al volver atrás no regrese al Login.
                            popUpTo(AppScreens.LoginScreen.route) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                // UX: Prevenir múltiples clics bloqueando el botón mientras carga.
                enabled = !uiState.isLoading
            ) {
                // Feedback visual: Reemplazar texto por loader mejora la experiencia de usuario.
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Ingresar")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Navegación secundaria hacia Registro.
            TextButton(
                onClick = { navController.navigate(AppScreens.RegisterScreen.route) },
                enabled = !uiState.isLoading
            ) {
                Text("¿No tienes una cuenta? Crear Cuenta")
            }
        }
    }
}

// Preview para diseño.
// Permite visualizar cambios en la UI sin necesidad de ejecutar la app completa en el emulador.
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    HuertoHogarAppTheme {
        LoginScreen(navController = NavController(LocalContext.current))
    }
}