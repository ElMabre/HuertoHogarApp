package com.huertohogar.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.huertohogar.app.model.HuertoHogarStores

// Pantalla dedicada a la integración con Google Maps SDK.
// No maneja lógica de negocio compleja (ViewModel), ya que solo visualiza datos estáticos de ubicación.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController) {
    // Coordenada central inicial (Santiago).
    val santiagoLocation = LatLng(-33.4489, -70.6693)

    // Estado de la Cámara del Mapa.
    // Usamos 'rememberCameraPositionState' para que la posición y el zoom sobrevivan a recomposiciones.
    // Si usáramos una variable normal, el mapa se resetearía a la posición inicial con cada cambio en la UI.
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(santiagoLocation, 10f)
    }

    // Estructura base.
    // Provee la barra superior para navegación simple (volver atrás), encapsulando el mapa en el área de contenido.
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuestras Tiendas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Componente Composable oficial de Google Maps.
            // Actúa como contenedor para los elementos del mapa (marcadores, polígonos, etc.).
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // Renderizado dinámico de marcadores.
                // Iteramos sobre la lista de datos (Model) para generar puntos visuales en el mapa.
                // 'Snippet' permite mostrar información adicional (dirección) al hacer clic en el pin.
                HuertoHogarStores.list.forEach { store ->
                    Marker(
                        state = MarkerState(position = store.location),
                        title = store.name,
                        snippet = store.address
                    )
                }
            }
        }
    }
}