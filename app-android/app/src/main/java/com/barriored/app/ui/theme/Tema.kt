package com.barriored.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colores sacados del CSS de la web (azules y grises "slate")
val Azul = Color(0xFF2563EB)
val AzulClaro = Color(0xFF3B82F6)
val Pizarra = Color(0xFF1E293B)
val PizarraOscura = Color(0xFF0F172A)
val Rojo = Color(0xFFEF4444)
val Ambar = Color(0xFFF59E0B)
val Verde = Color(0xFF22C55E)

private val Claro = lightColorScheme(
    primary = Azul,
    secondary = AzulClaro,
    background = Color(0xFFF1F5F9),
    surface = Color.White,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF64748B),
    error = Rojo,
)

private val Oscuro = darkColorScheme(
    primary = AzulClaro,
    secondary = Azul,
    background = PizarraOscura,
    surface = Pizarra,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = Rojo,
)

/** Sigue el modo claro/oscuro del sistema, como el selector de tema de la web. */
@Composable
fun BarrioRedTema(oscuro: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (oscuro) Oscuro else Claro, content = content)
}
