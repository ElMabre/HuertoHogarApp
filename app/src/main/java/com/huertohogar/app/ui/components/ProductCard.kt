package com.huertohogar.app.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.huertohogar.app.R
import com.huertohogar.app.model.Producto
import com.huertohogar.app.ui.theme.HuertoHogarAppTheme
import java.util.Locale

// Definimos el Locale para Chile
private val chileLocale: Locale = Locale.forLanguageTag("es-CL")

/**
 * Un Composable reutilizable que muestra la información de un producto en formato de tarjeta.
 */
@SuppressLint("DefaultLocale")
@Composable
fun ProductCard(
    producto: Producto,
    onProductClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .widthIn(min = 160.dp, max = 200.dp)
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .clickable { onProductClick(producto.id) }, // Usamos el SKU (String) para la navegación
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(producto.imagenUrl)
                    .crossfade(true)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_error_image)
                    .build(),
                contentDescription = "Imagen de ${producto.nombre}",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    // Usamos el Locale de Chile para el formato de moneda
                    text = "$${String.format(chileLocale, "%,.0f", producto.precio)} CLP / ${producto.unidad}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 200)
@Composable
fun ProductCardPreview() {
    // Datos de prueba actualizados con el nuevo campo databaseId
    val productoDeEjemplo = Producto(
        id = "FR001",          // SKU (String)
        databaseId = 100L,     // ID Numérico (Long) - ¡Nuevo!
        nombre = "Manzanas Fuji",
        descripcion = "Deliciosas manzanas.",
        precio = 1200.0,
        stock = 150,
        categoria = "frutas",
        imagenUrl = "url_invalida",
        origen = "Valle del Maule",
        unidad = "Kg"
    )
    HuertoHogarAppTheme {
        ProductCard(producto = productoDeEjemplo, onProductClick = {})
    }
}