package com.huertohogar.app.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.huertohogar.app.ui.components.HuertoTopAppBar
import com.huertohogar.app.utils.ChileLocations
import com.huertohogar.app.viewmodel.ProfileViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Pantalla de Perfil de Usuario.
// Gestiona la visualización y edición de datos personales, incluyendo la lógica compleja
// de interacción con el sistema operativo para capturar fotos (Cámara/Galería).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Estados locales para controlar la visibilidad de diálogos y menús desplegables (Dropdowns).
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var regionExpanded by remember { mutableStateOf(false) }
    var comunaExpanded by remember { mutableStateOf(false) }

    // --- INTEGRACIÓN CON ACTIVITY RESULT API ---
    // Reemplaza al antiguo 'startActivityForResult'.
    // Permite obtener resultados de otras apps (Galería/Cámara) de forma asíncrona dentro de un Composable.



    // 1. Contrato para Galería (Photo Picker nativo de Android).
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { viewModel.updateProfileImage(it.toString()) }
        }
    )

    // 2. Contrato para Cámara.
    // Requiere pasarle una URI donde se guardará la foto antes de tomarla.
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempCameraUri != null) {
                viewModel.updateProfileImage(tempCameraUri.toString())
            }
        }
    )

    // Manejo de Feedback (Toasts).
    // Se usa LaunchedEffect para mostrar mensajes solo cuando el estado cambia,
    // evitando que el Toast se repita en cada recomposición de la pantalla.
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        if (uiState.successMessage != null) {
            Toast.makeText(context, uiState.successMessage, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
        if (uiState.errorMessage != null) {
            Toast.makeText(context, uiState.errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            HuertoTopAppBar(
                title = "Mi Perfil",
                canNavigateBack = false,
                navController = navController,
                cartViewModel = viewModel(modelClass = com.huertohogar.app.viewmodel.CartViewModel::class.java)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()) // Scroll vital para evitar ocultar campos con el teclado
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- AVATAR DE USUARIO ---
            // Muestra la imagen actual o un placeholder si no existe.
            // El Box permite superponer el botón de edición sobre la imagen.
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.size(140.dp)
            ) {
                if (uiState.profileImageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(uiState.profileImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .clickable { showImageSourceDialog = true }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.LightGray)
                            .clickable { showImageSourceDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.White
                        )
                    }
                }
                // Botón flotante pequeño para indicar "Editar"
                SmallFloatingActionButton(
                    onClick = { showImageSourceDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(18.dp))
                }
            }

            Text(
                text = uiState.email,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 12.dp, bottom = 24.dp)
            )

            // --- FORMULARIO DE DATOS ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Información Personal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.nombre,
                            onValueChange = { viewModel.onNombreChange(it) },
                            label = { Text("Nombre") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = uiState.apellido,
                            onValueChange = { viewModel.onApellidoChange(it) },
                            label = { Text("Apellido") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // --- DROPDOWNS DEPENDIENTES (Región -> Comuna) ---
                    // Se usa ExposedDropdownMenuBox para seguir los lineamientos de Material Design 3.

                    // Selector de Región
                    ExposedDropdownMenuBox(
                        expanded = regionExpanded,
                        onExpandedChange = { regionExpanded = !regionExpanded }
                    ) {
                        OutlinedTextField(
                            value = uiState.region,
                            onValueChange = {},
                            readOnly = true, // Es solo selección, no escritura
                            label = { Text("Región") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = regionExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
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

                    // Selector de Comuna
                    // La lista 'comunasDisponibles' se recalcula dinámicamente según la región seleccionada.
                    val comunasDisponibles = ChileLocations.regionesYComunas[uiState.region] ?: emptyList()
                    ExposedDropdownMenuBox(
                        expanded = comunaExpanded,
                        onExpandedChange = { if (uiState.region.isNotEmpty()) comunaExpanded = !comunaExpanded }
                    ) {
                        OutlinedTextField(
                            value = uiState.comuna,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Comuna") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = comunaExpanded) },
                            // Se deshabilita si no hay región seleccionada para evitar inconsistencias
                            enabled = uiState.region.isNotEmpty(),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
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

                    OutlinedTextField(
                        value = uiState.direccion,
                        onValueChange = { viewModel.onDireccionChange(it) },
                        label = { Text("Dirección (Calle y Número)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.saveChanges() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar Cambios")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Lógica de Logout
            // Limpia el stack de navegación para que el usuario no pueda volver atrás con el botón "Back".
            OutlinedButton(
                onClick = {
                    viewModel.onLogout {
                        navController.navigate(com.huertohogar.app.navigation.AppScreens.LoginScreen.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión")
            }
        }

        // Modal para decidir origen de la imagen
        if (showImageSourceDialog) {
            AlertDialog(
                onDismissRequest = { showImageSourceDialog = false },
                title = { Text("Cambiar foto de perfil") },
                text = { Text("Elige una opción:") },
                confirmButton = {
                    // Acción CÁMARA
                    TextButton(onClick = {
                        showImageSourceDialog = false
                        try {
                            // Creamos URI temporal y lanzamos cámara
                            val uri = context.createImageUri()
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error al abrir cámara: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cámara")
                    }
                },
                dismissButton = {
                    // Acción GALERÍA
                    TextButton(onClick = {
                        showImageSourceDialog = false
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Galería")
                    }
                }
            )
        }
    }
}

// Función de extensión utilitaria.
// Crea un archivo temporal seguro y genera una URI usando 'FileProvider'.
// Esto es obligatorio desde Android 7 (Nougat) para compartir archivos entre aplicaciones (tu app -> app de cámara).
fun Context.createImageUri(): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName,
        ".jpg",
        cacheDir
    )

    return FileProvider.getUriForFile(
        this,
        "com.huertohogar.app.provider", // Debe coincidir con el 'authorities' en AndroidManifest.xml
        image
    )
}