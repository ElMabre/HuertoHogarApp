package com.huertohogar.app.model

import com.google.android.gms.maps.model.LatLng

/**
 * Modelo para representar una tienda física en el mapa.
 */
data class StoreLocation(
    val name: String,
    val location: LatLng,
    val address: String
)

/**
 * Lista estática de tiendas según el caso HuertoHogar.
 * (Santiago, Puerto Montt, Villarrica, Nacimiento, Viña del Mar, Valparaíso, Concepción)
 */
object HuertoHogarStores {
    val list = listOf(
        StoreLocation("Tienda Santiago", LatLng(-33.4489, -70.6693), "Av. Principal 123, Santiago"),
        StoreLocation("Tienda Viña del Mar", LatLng(-33.0246, -71.5518), "Calle Valparaíso 456, Viña"),
        StoreLocation("Tienda Valparaíso", LatLng(-33.0472, -71.6127), "Av. Pedro Montt 789, Valpo"),
        StoreLocation("Tienda Concepción", LatLng(-36.8201, -73.0444), "Barros Arana 101, Conce"),
        StoreLocation("Tienda Nacimiento", LatLng(-37.5025, -72.6725), "Calle Estación 55, Nacimiento"),
        StoreLocation("Tienda Villarrica", LatLng(-39.2827, -72.2263), "Camilo Henríquez 300, Villarrica"),
        StoreLocation("Tienda Puerto Montt", LatLng(-41.4689, -72.9411), "Costanera 900, Pto Montt")
    )
}