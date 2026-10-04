package com.barriored.app.ui.componentes

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun Cargando(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun PantallaError(mensaje: String, onReintentar: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onReintentar) { Text("Reintentar") }
    }
}

@Composable
fun TextoVacio(texto: String) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(texto, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val formatoHora = DateTimeFormatter.ofPattern("HH:mm")

private fun local(iso: String?) = iso?.let {
    runCatching { OffsetDateTime.parse(it).atZoneSameInstant(ZoneId.systemDefault()) }.getOrNull()
}

/** "2025-03-01T10:20:30+00:00" -> "01/03/2025" */
fun fecha(iso: String?): String = local(iso)?.format(formatoFecha) ?: ""
fun hora(iso: String?): String = local(iso)?.format(formatoHora) ?: ""

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
