package com.barriored.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Mismos colores que la web (SideBar.css, Dashboard.css, ChatbotAyuda.css)
val Acento = Color(0xFF3B82F6)
val AcentoOscuro = Color(0xFF2563EB)
val Ambar = Color(0xFFF59E0B)
val Verde = Color(0xFF10B981)
val Morado = Color(0xFFA78BFA)
val Rojo = Color(0xFFEF4444)
val Cian = Color(0xFF22D3EE)
val AzulBrand = Color(0xFF1D4ED8)
val NegroBrand = Color(0xFF111827)

/** Paleta del tema actual: las variables CSS --bg-body, --bg-card, etc. de la web. */
data class ColoresBR(
    val fondo: Color,           // --bg-body
    val tarjeta: Color,         // --bg-sidebar / --bg-card
    val interior: Color,        // --bg-sidebar-hover (elementos dentro de tarjetas)
    val texto: Color,           // --text-primary
    val textoSecundario: Color, // --text-secondary
    val borde: Color,
    val oscuro: Boolean,
)

private val PaletaOscura = ColoresBR(
    fondo = Color(0xFF0D1B2A),
    tarjeta = Color(0xFF1E293B),
    interior = Color(0xFF2D3F55),
    texto = Color(0xFFF1F5F9),
    textoSecundario = Color(0xFF94A3B8),
    borde = Color(0x1FFFFFFF),
    oscuro = true,
)

private val PaletaClara = ColoresBR(
    fondo = Color(0xFFF8FAFC),
    tarjeta = Color(0xFFFFFFFF),
    interior = Color(0xFFE2E8F0),
    texto = Color(0xFF1E293B),
    textoSecundario = Color(0xFF64748B),
    borde = Color(0x14000000),
    oscuro = false,
)

val LocalColoresBR = staticCompositionLocalOf { PaletaOscura }

/** Acceso corto a la paleta: BR.c.tarjeta, BR.c.texto… */
object BR {
    val c: ColoresBR
        @Composable get() = LocalColoresBR.current
}

/** Oscuro por defecto, como la web; se cambia desde Perfil. */
@Composable
fun BarrioRedTema(oscuro: Boolean = true, content: @Composable () -> Unit) {
    val p = if (oscuro) PaletaOscura else PaletaClara
    val esquema = if (oscuro) {
        darkColorScheme(
            primary = Acento, onPrimary = Color.White, secondary = AcentoOscuro,
            background = p.fondo, onBackground = p.texto,
            surface = p.tarjeta, onSurface = p.texto,
            surfaceVariant = p.interior, onSurfaceVariant = p.textoSecundario,
            surfaceContainer = p.tarjeta, surfaceContainerHigh = p.tarjeta,
            outline = p.interior, error = Rojo,
        )
    } else {
        lightColorScheme(
            primary = Acento, onPrimary = Color.White, secondary = AcentoOscuro,
            background = p.fondo, onBackground = p.texto,
            surface = p.tarjeta, onSurface = p.texto,
            surfaceVariant = p.interior, onSurfaceVariant = p.textoSecundario,
            surfaceContainer = p.tarjeta, surfaceContainerHigh = p.tarjeta,
            outline = p.interior, error = Rojo,
        )
    }
    CompositionLocalProvider(LocalColoresBR provides p) {
        MaterialTheme(colorScheme = esquema, content = content)
    }
}
