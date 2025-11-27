package com.huertohogar.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.huertohogar.app.R
import com.huertohogar.app.ui.components.HuertoTopAppBar
import com.huertohogar.app.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(
    navController: NavController,
    cartViewModel: CartViewModel = viewModel() // Obtenemos el ViewModel del carrito para la barra
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            HuertoTopAppBar(
                title = "Nosotros",
                canNavigateBack = true, // Permitimos volver atrás
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // --- LOGO HUERTO HOGAR ---
            // Usamos AsyncImage para cargar el logo desde el link que me diste
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("https://i.ibb.co/hJZ4vr17/huertohogarlogoconfondo.png")
                    .crossfade(true)
                    .build(),
                contentDescription = "Logo Huerto Hogar",
                modifier = Modifier
                    .size(200.dp) // Tamaño ajustado para que se vea bien
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- SECCIÓN: SOBRE NOSOTROS ---
            Text(
                text = "Sobre HuertoHogar",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "HuertoHogar nace con la misión de conectar a los pequeños agricultores locales directamente con tu mesa. Creemos en la frescura, la calidad y el comercio justo, eliminando intermediarios para ofrecerte lo mejor de nuestra tierra.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- SECCIÓN: NUESTRA VISIÓN ---
            Text(
                text = "Nuestra Visión",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ser la plataforma líder en distribución de productos orgánicos y naturales en la región, fomentando una alimentación saludable, sostenible y apoyando la economía local.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Justify
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}