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

// Locale específico para formatear precios según la convención chilena.
private val chileLocale: Locale = Locale.forLanguageTag("es-CL")

// Tarjeta reutilizable que encapsula la presentación visual de un producto.
@SuppressLint("DefaultLocale")
@Composable
fun ProductCard(
    producto: Producto,
    onProductClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Contenedor visual principal: tarjeta con padding y clic para navegar usando el ID del producto.
    Card(
        modifier = modifier
            .widthIn(min = 160.dp, max = 200.dp)
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .clickable { onProductClick(producto.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            // Imagen del producto usando Coil: carga asíncrona con placeholder y manejo de errores.
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
                    .aspectRatio(1f), // Mantiene formato cuadrado para todas las tarjetas.
                contentScale = ContentScale.Crop
            )

            // Contenido textual: nombre + precio formateado según CLP.
            Column(modifier = Modifier.padding(12.dp)) {
                // Nombre del producto con estilo destacado.
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Precio usando formato chileno y mostrando la unidad del producto.
                Text(
                    text = "$${String.format(chileLocale, "%,.0f", producto.precio)} CLP / ${producto.unidad}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// Vista previa para Composable Preview: facilita validar la UI durante el desarrollo.
@Preview(showBackground = true, widthDp = 200)
@Composable
fun ProductCardPreview() {
    val productoDeEjemplo = Producto(
        id = "FR001",
        databaseId = 100L,
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