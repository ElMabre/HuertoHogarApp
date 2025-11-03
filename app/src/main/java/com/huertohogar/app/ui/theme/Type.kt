package com.huertohogar.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.huertohogar.app.R

val Montserrat = FontFamily(
    Font(R.font.montserrat_variable, FontWeight.Normal),
    Font(R.font.montserrat_variable, FontWeight.SemiBold),
    Font(R.font.montserrat_variable, FontWeight.Bold)
)
val PlayfairDisplay = FontFamily(
    Font(R.font.playfair_display_variable, FontWeight.Normal),
    Font(R.font.playfair_display_variable, FontWeight.Bold)
)

/**
 * `AppTypography` es el objeto que define todos los estilos de texto para la aplicación.
 * Es como una guía de estilo para las letras. Cada vez que queramos poner un título
 * o un párrafo, usaremos uno de estos estilos para que todo se vea igual.
 * Estos estilos se conectan al tema general de la app en `Theme.kt`.
 */
val AppTypography = Typography(
    // Estilo para los títulos más grandes y llamativos de la app.
    displayLarge = TextStyle(
        fontFamily = PlayfairDisplay, // Usa la fuente Playfair Display.
        fontWeight = FontWeight.Bold, // En negrita.
        fontSize = 57.sp,             // Tamaño de la letra.
        lineHeight = 64.sp,           // Espacio vertical entre líneas.
        letterSpacing = (-0.25).sp    // Espacio horizontal entre letras (un poco más juntas).
    ),
    // Estilo para títulos de secciones o pantallas.
    titleLarge = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Normal, // Grosor normal.
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // Estilo para el texto principal de la aplicación, como párrafos o descripciones.
    bodyLarge = TextStyle(
        fontFamily = Montserrat, // Usa la fuente Montserrat.
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // Estilo para textos que necesitan destacar un poco, como botones o etiquetas.
    labelLarge = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.SemiBold, // Un poco más grueso que normal (semi-negrita).
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)
// TODO: Se podrían definir más estilos de texto (como bodyMedium, titleSmall, etc.)
// para tener un control más fino sobre la tipografía en diferentes partes de la app.
// Esto ayuda a mantener la consistencia y seguir las guías de Material Design.
