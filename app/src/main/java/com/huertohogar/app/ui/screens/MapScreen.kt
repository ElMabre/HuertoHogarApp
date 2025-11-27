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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController) {
    // Centrar el mapa inicialmente en Santiago
    val santiagoLocation = LatLng(-33.4489, -70.6693)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(santiagoLocation, 10f)
    }

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
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // Recorremos la lista de tiendas del modelo y creamos un marcador por cada una
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