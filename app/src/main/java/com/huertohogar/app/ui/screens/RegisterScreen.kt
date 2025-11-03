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
import com.huertohogar.app.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    registerViewModel: RegisterViewModel = viewModel()
) {
    val uiState by registerViewModel.uiState.collectAsState()
    Scaffold { innerPadding ->
        RegisterForm(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            viewModel = registerViewModel,
            onRegisterClick = {
                // 1. Llamamos a la nueva función del ViewModel
                registerViewModel.onRegisterClicked {
                    // 2. Esta es la lambda 'onRegisterSuccess'.
                    // Se ejecuta solo si el registro es exitoso.
                    navController.navigate(AppScreens.HomeScreen.route) {
                        popUpTo(AppScreens.LoginScreen.route) { inclusive = true }
                    }
                }
            },
            onLoginClick = {
                navController.navigate(AppScreens.LoginScreen.route)
            }
        )
    }
}

@Composable
private fun RegisterForm(
    modifier: Modifier = Modifier,
    uiState: com.huertohogar.app.model.RegisterUiState,
    viewModel: RegisterViewModel,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    // Deshabilitamos todos los campos si está cargando
    val isEnabled = !uiState.isLoading

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Crear Cuenta", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Regístrate para disfrutar de nuestros servicios",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.nombre,
            onValueChange = viewModel::onNombreChange,
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.errors.nombre != null,
            supportingText = { if (uiState.errors.nombre != null) Text(uiState.errors.nombre) },
            singleLine = true,
            enabled = isEnabled
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.apellido,
            onValueChange = viewModel::onApellidoChange,
            label = { Text("Apellido") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.errors.apellido != null,
            supportingText = { if (uiState.errors.apellido != null) Text(uiState.errors.apellido) },
            singleLine = true,
            enabled = isEnabled
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.run,
            onValueChange = viewModel::onRunChange,
            label = { Text("RUN (sin puntos, con guión)") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.errors.run != null,
            supportingText = { if (uiState.errors.run != null) Text(uiState.errors.run) },
            placeholder = { Text("12345678-9") },
            singleLine = true,
            enabled = isEnabled
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.errors.email != null,
            supportingText = { if (uiState.errors.email != null) Text(uiState.errors.email) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            enabled = isEnabled
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.errors.password != null,
            supportingText = { if (uiState.errors.password != null) Text(uiState.errors.password) },
            visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = viewModel::onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (uiState.passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            enabled = isEnabled
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
            label = { Text("Confirmar Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.errors.confirmPassword != null,
            supportingText = { if (uiState.errors.confirmPassword != null) Text(uiState.errors.confirmPassword) },
            visualTransformation = if (uiState.confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = viewModel::onToggleConfirmPasswordVisibility) {
                    Icon(
                        imageVector = if (uiState.confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle confirm password visibility"
                    )
                }
            },
            enabled = isEnabled
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = uiState.aceptaTerminos,
                onCheckedChange = viewModel::onAceptaTerminosChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                ),
                enabled = isEnabled
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Acepto los términos y condiciones", style = MaterialTheme.typography.bodyMedium)
        }
        if (uiState.errors.aceptaTerminos != null) {
            Text(
                text = uiState.errors.aceptaTerminos,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRegisterClick, // Usamos la función del parámetro
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = isEnabled // Deshabilitamos si está cargando
        ) {
            // Mostramos loader o texto
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Crear Cuenta")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onLoginClick,
            enabled = isEnabled 
        ) {
            Text("¿Ya tienes una cuenta? Inicia Sesión")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    HuertoHogarAppTheme {
        RegisterScreen(navController = NavController(LocalContext.current))
    }
}
