package com.huertohogar.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.huertohogar.app.ui.components.HuertoTopAppBar
import com.huertohogar.app.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    navController: NavController,
    cartViewModel: CartViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            HuertoTopAppBar(
                title = "Contacto",
                canNavigateBack = true, // Permite volver
                navController = navController,
                cartViewModel = cartViewModel
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "¡Estamos aquí para ayudarte!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Si tienes dudas sobre tus pedidos, nuestros productos o quieres trabajar con nosotros, no dudes en contactarnos.",
                style = MaterialTheme.typography.bodyLarge
            )

            HorizontalDivider()

            // Sección de Datos de Contacto
            ContactItem(
                icon = Icons.Default.Email,
                title = "Correo Electrónico",
                detail = "contacto@huertohogar.cl"
            )

            ContactItem(
                icon = Icons.Default.Phone,
                title = "Teléfono",
                detail = "+56 9 1234 5678"
            )

            ContactItem(
                icon = Icons.Default.LocationOn,
                title = "Oficina Central",
                detail = "Av. Providencia 1234, Santiago"
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ContactItem(icon: ImageVector, title: String, detail: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = detail, style = MaterialTheme.typography.bodyMedium)
        }
    }
}