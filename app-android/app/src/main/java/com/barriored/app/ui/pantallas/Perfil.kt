package com.barriored.app.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barriored.app.data.Preferencias
import com.barriored.app.data.model.Actividad
import com.barriored.app.data.model.Comunidad
import com.barriored.app.data.model.Perfil
import com.barriored.app.data.repo.AuthRepositorio
import com.barriored.app.data.repo.PerfilRepositorio
import com.barriored.app.ui.componentes.Avatar
import com.barriored.app.ui.componentes.CampoTexto
import com.barriored.app.ui.componentes.Cargando
import com.barriored.app.ui.componentes.PantallaError
import com.barriored.app.ui.componentes.Tarjeta
import com.barriored.app.ui.componentes.hace
import com.barriored.app.ui.componentes.mensajeUsuario
import com.barriored.app.ui.componentes.mesAnio
import com.barriored.app.ui.theme.Acento
import com.barriored.app.ui.theme.Ambar
import com.barriored.app.ui.theme.AzulBrand
import com.barriored.app.ui.theme.BR
import com.barriored.app.ui.theme.Morado
import com.barriored.app.ui.theme.Rojo
import com.barriored.app.ui.theme.Verde
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val Cian = Color(0xFF0891B2)

// Mismos textos que rolConfig en MiPerfil.jsx
private fun etiquetaRol(rol: String?) = when (rol) {
    "presidente" -> "Presidente de Comunidad"
    "admin" -> "Administrador"
    "ayuntamiento" -> "Ayuntamiento"
    "conserje" -> "Conserje"
    else -> "Vecino"
}

data class EstadoPerfil(
    val cargando: Boolean = true,
    val error: String? = null,
    val perfil: Perfil? = null,
    val comunidad: Comunidad? = null,
    val numIncidencias: Long = 0,
    val actividad: List<Actividad> = emptyList(),
    val aviso: String? = null,
)

class PerfilViewModel : ViewModel() {
    private val perfiles = PerfilRepositorio()
    private val auth = AuthRepositorio()

    private val _estado = MutableStateFlow(EstadoPerfil())
    val estado = _estado.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = it.perfil == null, error = null) }
            try {
                val p = perfiles.miPerfil()
                _estado.update {
                    it.copy(
                        cargando = false,
                        perfil = p,
                        comunidad = p.comunidadId?.let { id -> perfiles.comunidad(id) },
                        numIncidencias = perfiles.numIncidenciasCreadas(),
                        actividad = runCatching { perfiles.actividadReciente() }.getOrDefault(emptyList()),
                    )
                }
            } catch (e: Exception) {
                _estado.update { it.copy(cargando = false, error = e.mensajeUsuario()) }
            }
        }
    }

    fun guardar(nombre: String, telefono: String, piso: String) {
        viewModelScope.launch {
            try {
                perfiles.actualizarDatos(nombre.trim(), telefono.trim(), piso.trim())
                _estado.update { it.copy(aviso = "Datos guardados correctamente.") }
                cargar()
            } catch (e: Exception) {
                _estado.update { it.copy(aviso = e.mensajeUsuario()) }
            }
        }
    }

    /** alTerminar(null) = todo bien; si no, el mensaje de error. */
    fun cambiarContrasena(actual: String, nueva: String, alTerminar: (String?) -> Unit) {
        viewModelScope.launch {
            val error = try {
                if (auth.cambiarContrasena(actual, nueva)) null else "La contraseña actual no es correcta."
            } catch (e: Exception) {
                e.mensajeUsuario()
            }
            if (error == null) _estado.update { it.copy(aviso = "Contraseña actualizada correctamente.") }
            alTerminar(error)
        }
    }

    fun avisoVisto() = _estado.update { it.copy(aviso = null) }

    // Al cerrar sesión, RaizApp vuelve sola al Login
    fun cerrarSesion() {
        viewModelScope.launch { runCatching { auth.cerrarSesion() } }
    }
}

@Composable
fun PerfilPantalla(vm: PerfilViewModel = viewModel()) {
    val e by vm.estado.collectAsState()
    val oscuro by Preferencias.modoOscuro.collectAsState()
    var editando by rememberSaveable { mutableStateOf(false) }
    var cambiandoClave by rememberSaveable { mutableStateOf(false) }
    val abrirEnlace = LocalUriHandler.current

    when {
        e.cargando -> Cargando()
        e.error != null -> PantallaError(e.error!!, vm::cargar)
        else -> {
            val p = e.perfil ?: return
            val c = e.comunidad
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Cabecera(p, c)
                BarraEstadisticas(e.numIncidencias)

                e.aviso?.let { aviso ->
                    Text(
                        aviso,
                        color = Verde,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Verde.copy(alpha = 0.12f))
                            .clickable { vm.avisoVisto() }
                            .padding(12.dp),
                    )
                }

                TarjetaSeccion("Información personal") {
                    DatoConIcono(Icons.Filled.Email, "Correo electrónico", p.email)
                    DatoConIcono(Icons.Filled.Phone, "Teléfono", p.telefono)
                    DatoConIcono(Icons.Filled.Apartment, "Comunidad", c?.nombre)
                    DatoConIcono(Icons.Filled.Home, "Piso / Unidad", p.piso)
                    DatoConIcono(Icons.Filled.CalendarMonth, "Miembro desde", mesAnio(p.creadoEn))
                    DatoConIcono(Icons.Filled.LocationOn, "Ciudad", c?.ciudad)
                }

                c?.let { TarjetaComunidad(it) }

                TarjetaSeccion("Actividad reciente") {
                    if (e.actividad.isEmpty()) {
                        Text("Sin actividad reciente.", color = BR.c.textoSecundario, fontSize = 14.sp)
                    } else {
                        e.actividad.forEach { FilaActividad(it) }
                    }
                }

                TarjetaSeccion("Participación") {
                    val rellenos = listOf(p.nombreCompleto, p.telefono, p.piso, p.username, p.avatarUrl).count { !it.isNullOrBlank() }
                    BarraProgreso("Perfil completado", rellenos * 20)
                    BarraProgreso("Actividad este mes", (e.numIncidencias * 10).toInt().coerceAtMost(100))
                    BarraProgreso("Interacción vecinal", (e.numIncidencias * 15).toInt().coerceAtMost(100))
                }

                TarjetaSeccion("Ajustes") {
                    c?.driveUrl?.takeIf { it.isNotBlank() }?.let { url ->
                        // Sustituye a la página Documentos de la web: abre la carpeta de Drive
                        FilaAjuste(Icons.Filled.FolderOpen, "Documentos de la comunidad", Acento) { abrirEnlace.openUri(url) }
                    }
                    FilaAjuste(Icons.Filled.Edit, "Editar mis datos", Acento) { editando = true }
                    FilaAjuste(Icons.Filled.Key, "Cambiar contraseña", Ambar) { cambiandoClave = true }
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconoCuadrado(Icons.Filled.DarkMode, Morado)
                        Spacer(Modifier.width(14.dp))
                        Text(if (oscuro) "Modo oscuro" else "Modo claro", color = BR.c.texto, fontSize = 15.sp, modifier = Modifier.weight(1f))
                        Switch(
                            checked = oscuro,
                            onCheckedChange = { Preferencias.cambiarModoOscuro(it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = Acento, checkedThumbColor = Color.White),
                        )
                    }
                    HorizontalDivider(color = BR.c.borde, modifier = Modifier.padding(vertical = 6.dp))
                    FilaAjuste(Icons.AutoMirrored.Filled.Logout, "Cerrar sesión", Rojo, colorTexto = Rojo, flecha = false) { vm.cerrarSesion() }
                }
                Spacer(Modifier.height(72.dp)) // hueco para el botón del asistente
            }

            if (editando) {
                DialogoEditar(p, onGuardar = { n, t, pi -> vm.guardar(n, t, pi); editando = false }, onCerrar = { editando = false })
            }
            if (cambiandoClave) {
                DialogoContrasena(vm, onCerrar = { cambiandoClave = false })
            }
        }
    }
}

@Composable
private fun Cabecera(p: Perfil, c: Comunidad?) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Avatar(
            p.nombreVisible,
            tamano = 104.dp,
            forma = RoundedCornerShape(22.dp),
            fondo = Color.Transparent,
            colorTexto = Color.White,
            modificadorFondo = Brush.linearGradient(listOf(AzulBrand, Cian)),
        )
        Spacer(Modifier.height(14.dp))
        Text(p.nombreVisible, color = if (BR.c.oscuro) Color(0xFF93C5FD) else Acento, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(Acento.copy(alpha = 0.12f))
                .border(1.dp, Acento.copy(alpha = 0.4f), RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Acento, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(etiquetaRol(p.role), color = Acento, fontSize = 13.sp)
        }
        Spacer(Modifier.height(10.dp))
        c?.ciudad?.let { MetaCabecera(Icons.Filled.LocationOn, it) }
        p.email?.let { MetaCabecera(Icons.Filled.Email, it) }
        MetaCabecera(Icons.Filled.CalendarMonth, "Miembro desde ${mesAnio(p.creadoEn)}")
    }
}

@Composable
private fun MetaCabecera(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
        Icon(icono, contentDescription = null, tint = Acento, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(8.dp))
        Text(texto, color = BR.c.textoSecundario, fontSize = 14.sp)
    }
}

@Composable
private fun BarraEstadisticas(incidencias: Long) {
    Tarjeta {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$incidencias", color = Acento, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("INCIDENCIAS REPORTADAS", color = BR.c.textoSecundario, fontSize = 12.sp, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun TarjetaSeccion(titulo: String, contenido: @Composable () -> Unit) {
    Tarjeta(relleno = 0.dp) {
        Text(titulo, color = BR.c.texto, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(20.dp))
        HorizontalDivider(color = BR.c.borde)
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { contenido() }
    }
}

@Composable
private fun IconoCuadrado(icono: ImageVector, color: Color) {
    Box(
        Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) { Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(19.dp)) }
}

@Composable
private fun DatoConIcono(icono: ImageVector, etiqueta: String, valor: String?) {
    if (valor.isNullOrBlank()) return
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconoCuadrado(icono, Acento)
        Spacer(Modifier.width(14.dp))
        Column {
            Text(etiqueta.uppercase(), color = BR.c.textoSecundario, fontSize = 11.sp, letterSpacing = 0.8.sp)
            Text(valor, color = BR.c.texto, fontSize = 15.sp)
        }
    }
}

@Composable
private fun TarjetaComunidad(c: Comunidad) {
    Tarjeta(relleno = 0.dp) {
        Box(
            Modifier.fillMaxWidth().height(80.dp).background(Brush.linearGradient(listOf(AzulBrand, Cian))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Apartment, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(42.dp))
        }
        Column(Modifier.padding(20.dp)) {
            Text(c.nombre.orEmpty(), color = BR.c.texto, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(listOfNotNull(c.tipo, c.ciudad).joinToString(" · "), color = BR.c.textoSecundario, fontSize = 14.sp)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                DatoComunidad(c.numVecinos, "VECINOS")
                DatoComunidad(c.numBloques, "BLOQUES")
                DatoComunidad(c.numZonasComunes, "ZONAS COMUNES")
            }
        }
    }
}

@Composable
private fun DatoComunidad(valor: Int?, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("${valor ?: 0}", color = Acento, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(etiqueta, color = BR.c.textoSecundario, fontSize = 11.sp, letterSpacing = 0.6.sp)
    }
}

@Composable
private fun FilaActividad(a: Actividad) {
    val (icono, color) = when (a.tipo) {
        "incidencia" -> Icons.Filled.Warning to Ambar
        "documento" -> Icons.Filled.FolderOpen to Acento
        "reserva" -> Icons.Filled.CalendarMonth to Verde
        else -> Icons.Filled.Email to Morado
    }
    Row(verticalAlignment = Alignment.Top) {
        IconoCuadrado(icono, color)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(a.titulo, color = BR.c.texto, fontSize = 15.sp)
            a.descripcion?.let { Text(it, color = BR.c.textoSecundario, fontSize = 13.sp) }
        }
        Text(hace(a.creadoEn), color = BR.c.textoSecundario, fontSize = 12.sp)
    }
}

@Composable
private fun BarraProgreso(etiqueta: String, valor: Int) {
    Column {
        Row(Modifier.fillMaxWidth()) {
            Text(etiqueta, color = BR.c.textoSecundario, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text("$valor%", color = BR.c.texto, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { valor / 100f },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = Acento,
            trackColor = BR.c.interior,
            drawStopIndicator = {},
        )
    }
}

@Composable
private fun FilaAjuste(
    icono: ImageVector,
    texto: String,
    color: Color,
    colorTexto: Color = Color.Unspecified,
    flecha: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconoCuadrado(icono, color)
        Spacer(Modifier.width(14.dp))
        Text(
            texto,
            color = if (colorTexto == Color.Unspecified) BR.c.texto else colorTexto,
            fontSize = 15.sp,
            fontWeight = if (flecha) FontWeight.Normal else FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        if (flecha) Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = BR.c.textoSecundario)
    }
}

@Composable
private fun DialogoEditar(perfil: Perfil, onGuardar: (String, String, String) -> Unit, onCerrar: () -> Unit) {
    var nombre by rememberSaveable { mutableStateOf(perfil.nombreCompleto.orEmpty()) }
    var telefono by rememberSaveable { mutableStateOf(perfil.telefono.orEmpty()) }
    var piso by rememberSaveable { mutableStateOf(perfil.piso.orEmpty()) }

    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = BR.c.tarjeta,
        title = { Text("Editar mis datos", color = BR.c.texto) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CampoTexto(nombre, { nombre = it }, "Nombre completo")
                CampoTexto(telefono, { telefono = it }, "Teléfono", teclado = KeyboardOptions(keyboardType = KeyboardType.Phone))
                CampoTexto(piso, { piso = it }, "Piso (p. ej. 3ºB)")
            }
        },
        confirmButton = {
            TextButton(onClick = { onGuardar(nombre, telefono, piso) }, enabled = nombre.isNotBlank()) {
                Text("Guardar", color = Acento)
            }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar", color = BR.c.textoSecundario) } },
    )
}

@Composable
private fun DialogoContrasena(vm: PerfilViewModel, onCerrar: () -> Unit) {
    var actual by rememberSaveable { mutableStateOf("") }
    var nueva by rememberSaveable { mutableStateOf("") }
    var repetir by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var guardando by rememberSaveable { mutableStateOf(false) }
    val oculto = PasswordVisualTransformation()
    val tecladoClave = KeyboardOptions(keyboardType = KeyboardType.Password)

    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = BR.c.tarjeta,
        title = { Text("Cambiar contraseña", color = BR.c.texto) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CampoTexto(actual, { actual = it }, "Contraseña actual", transformacion = oculto, teclado = tecladoClave)
                CampoTexto(nueva, { nueva = it }, "Nueva (mínimo 6 caracteres)", transformacion = oculto, teclado = tecladoClave)
                CampoTexto(repetir, { repetir = it }, "Repite la nueva contraseña", transformacion = oculto, teclado = tecladoClave)
                error?.let { Text(it, color = Rojo, fontSize = 14.sp) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !guardando && actual.isNotEmpty() && nueva.isNotEmpty() && repetir.isNotEmpty(),
                onClick = {
                    error = when {
                        nueva.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
                        nueva != repetir -> "Las contraseñas no coinciden."
                        nueva == actual -> "La nueva contraseña debe ser distinta de la actual."
                        else -> null
                    }
                    if (error == null) {
                        guardando = true
                        vm.cambiarContrasena(actual, nueva) { fallo ->
                            guardando = false
                            if (fallo == null) onCerrar() else error = fallo
                        }
                    }
                },
            ) { Text(if (guardando) "Guardando…" else "Guardar", color = Acento) }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar", color = BR.c.textoSecundario) } },
    )
}
