package com.barriored.app.ui.componentes

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.barriored.app.ui.theme.Acento
import com.barriored.app.ui.theme.BR
import com.barriored.app.ui.theme.Rojo
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

val FormaTarjeta = RoundedCornerShape(14.dp)

/** Tarjeta redondeada con sombra suave, como las "cards" de la web. */
@Composable
fun Tarjeta(
    modifier: Modifier = Modifier,
    relleno: Dp = 18.dp,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .shadow(4.dp, FormaTarjeta)
            .clip(FormaTarjeta)
            .background(BR.c.tarjeta)
            .padding(relleno),
        content = contenido,
    )
}

/** Título grande de página + subtítulo ("Muro Comunitario", "Incidencias"…). */
@Composable
fun TituloPagina(titulo: String, subtitulo: String) {
    Column {
        Text(titulo, color = BR.c.texto, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 34.sp)
        Spacer(Modifier.height(4.dp))
        Text(subtitulo, color = BR.c.textoSecundario, fontSize = 16.sp)
    }
}

/** Etiqueta redondeada de color (estado de incidencia, rol…). */
@Composable
fun Pastilla(
    texto: String,
    color: Color,
    modifier: Modifier = Modifier,
    tamano: TextUnit = 12.sp,
    mayusculas: Boolean = false,
) {
    Text(
        if (mayusculas) texto.uppercase() else texto,
        color = color,
        fontSize = tamano,
        fontWeight = FontWeight.Bold,
        letterSpacing = if (mayusculas) 0.8.sp else 0.sp,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 5.dp),
    )
}

/** Círculo con las iniciales del usuario. */
@Composable
fun Avatar(
    nombre: String,
    tamano: Dp = 44.dp,
    fondo: Color = Acento.copy(alpha = 0.18f),
    colorTexto: Color = Acento,
    forma: Shape = CircleShape,
    modificadorFondo: Brush? = null,
) {
    Box(
        Modifier
            .size(tamano)
            .clip(forma)
            .then(if (modificadorFondo != null) Modifier.background(modificadorFondo) else Modifier.background(fondo)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            iniciales(nombre),
            color = colorTexto,
            fontWeight = FontWeight.Bold,
            fontSize = (tamano.value * 0.36f).sp,
        )
    }
}

fun iniciales(nombre: String): String =
    nombre.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

/** Botón principal azul redondeado. */
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    cargando: Boolean = false,
    color: Color = Acento,
) {
    Button(
        onClick = onClick,
        enabled = habilitado && !cargando,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White,
            disabledContainerColor = color.copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.8f),
        ),
        modifier = modifier.height(50.dp),
    ) {
        if (cargando) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
        else Text(texto, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

/** Campo de texto con el estilo de los formularios de la web. */
@Composable
fun CampoTexto(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    unaLinea: Boolean = true,
    lineasMin: Int = 1,
    finalIcono: @Composable (() -> Unit)? = null,
    transformacion: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None,
    teclado: androidx.compose.foundation.text.KeyboardOptions =
        androidx.compose.foundation.text.KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        placeholder = { Text(etiqueta, color = BR.c.textoSecundario) },
        singleLine = unaLinea,
        minLines = lineasMin,
        trailingIcon = finalIcono,
        visualTransformation = transformacion,
        keyboardOptions = teclado,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = BR.c.interior.copy(alpha = 0.5f),
            unfocusedContainerColor = BR.c.interior.copy(alpha = 0.5f),
            focusedBorderColor = Acento,
            unfocusedBorderColor = BR.c.borde,
            focusedTextColor = BR.c.texto,
            unfocusedTextColor = BR.c.texto,
            cursorColor = Acento,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun Cargando(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Acento, strokeWidth = 4.dp, modifier = Modifier.size(44.dp))
    }
}

@Composable
fun PantallaError(mensaje: String, onReintentar: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(mensaje, color = Rojo, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        BotonPrincipal("Reintentar", onReintentar)
    }
}

@Composable
fun TextoVacio(texto: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(texto, color = BR.c.textoSecundario, textAlign = TextAlign.Center)
    }
}

private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val formatoFechaCorta = DateTimeFormatter.ofPattern("dd/MM/yy · HH:mm")
private val formatoHora = DateTimeFormatter.ofPattern("HH:mm")
private val formatoMesAnio = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", java.util.Locale.forLanguageTag("es-ES"))

// Acepta "timestamptz" (con zona, p. ej. +00:00) y "timestamp" (sin zona, se asume UTC)
private fun local(iso: String?) = iso?.let {
    runCatching { OffsetDateTime.parse(it).atZoneSameInstant(ZoneId.systemDefault()) }.getOrNull()
        ?: runCatching { LocalDateTime.parse(it).atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.systemDefault()) }.getOrNull()
}

/** "2025-03-01T10:20:30+00:00" -> "01/03/2025" */
fun fecha(iso: String?): String = local(iso)?.format(formatoFecha) ?: ""
/** Formato de los mensajes del chat de la web: "04/10/26 · 15:29" */
fun fechaHoraCorta(iso: String?): String = local(iso)?.format(formatoFechaCorta) ?: ""
fun hora(iso: String?): String = local(iso)?.format(formatoHora) ?: ""
/** "julio de 2026" (Miembro desde…) */
fun mesAnio(iso: String?): String = local(iso)?.format(formatoMesAnio) ?: "—"

/** "hace 3d", "hace 2h"… como la actividad reciente de la web. */
fun hace(iso: String?): String {
    val t = local(iso) ?: return ""
    val min = java.time.Duration.between(t, java.time.ZonedDateTime.now()).toMinutes()
    return when {
        min < 1 -> "ahora"
        min < 60 -> "hace ${min}m"
        min < 60 * 24 -> "hace ${min / 60}h"
        else -> "hace ${min / (60 * 24)}d"
    }
}

/** Mensaje legible para el usuario a partir de una excepción (el error completo sale en Logcat). */
fun Throwable.mensajeUsuario(): String {
    Log.e("BarrioRed", "Error", this)
    val texto = message.orEmpty()
    return when {
        texto.contains("Invalid login credentials", ignoreCase = true) -> "Correo o contraseña incorrectos."
        texto.contains("Unable to resolve host", ignoreCase = true) -> "Sin conexión a internet."
        else -> "Ha ocurrido un error. Inténtalo de nuevo."
    }
}
