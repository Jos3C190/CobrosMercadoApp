package com.example.cobrosmercadoapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// PALETA PASTEL – Soft UI / Glassmorphism
private val PrimaryPurple = Color(0xFF6C63FF)        // Morado suave (color base)
private val SecondaryPurple = Color(0xFF9C92FF)      // Morado pastel claro
private val BackgroundSoft = Color(0xFFF5F7FA)       // Gris azul pastel (fondos glass)
private val SurfaceGlass = Color(0xFFFFFFFF).copy(alpha = 0.55f)
private val OnSurfaceGlass = Color(0xFF3A3A3A).copy(alpha = 0.75f)
private val OutlineSoft = Color(0x33000000)          // Bordes suaves
private val ErrorSoft = Color(0xFFE57373)

private val LightColors = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,

    secondary = SecondaryPurple,
    onSecondary = Color.White,

    background = BackgroundSoft,
    onBackground = Color(0xFF333333),

    surface = SurfaceGlass,
    onSurface = OnSurfaceGlass,

    surfaceVariant = Color.White.copy(alpha = 0.65f),
    onSurfaceVariant = Color(0xFF4A4A4A),

    outline = OutlineSoft,
    outlineVariant = Color(0x22000000),

    error = ErrorSoft,
    onError = Color.White,

    surfaceTint = PrimaryPurple
)

@Composable
fun CobrosMercadoAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
