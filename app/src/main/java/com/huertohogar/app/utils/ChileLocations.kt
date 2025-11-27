package com.huertohogar.app.utils

/**
 * Objeto utilitario que contiene la información estática de Regiones y Comunas de Chile.
 * Se utiliza para poblar los selectores en el formulario de registro.
 */
object ChileLocations {

    // Mapa que asocia el nombre de la Región con una lista de sus Comunas principales.
    val regionesYComunas = mapOf(
        "Región Metropolitana" to listOf(
            "Santiago", "Providencia", "Las Condes", "Maipú", "Puente Alto", "La Florida", "Ñuñoa", "Vitacura"
        ),
        "Región de Valparaíso" to listOf(
            "Valparaíso", "Viña del Mar", "Quilpué", "Villa Alemana", "San Antonio", "Concón"
        ),
        "Región del Biobío" to listOf(
            "Concepción", "Talcahuano", "Los Ángeles", "San Pedro de la Paz", "Nacimiento", "Chiguayante"
        ),
        "Región de La Araucanía" to listOf(
            "Temuco", "Villarrica", "Pucón", "Angol", "Padre Las Casas"
        ),
        "Región de Los Lagos" to listOf(
            "Puerto Montt", "Osorno", "Puerto Varas", "Castro", "Frutillar"
        ),
        "Región del Maule" to listOf(
            "Talca", "Curicó", "Linares", "Constitución", "Cauquenes"
        ),
        "Región de O'Higgins" to listOf(
            "Rancagua", "San Fernando", "Rengo", "Machalí", "Pichilemu"
        )
    )

    // Lista solo con los nombres de las regiones (para el primer dropdown)
    val regiones = regionesYComunas.keys.toList()
}