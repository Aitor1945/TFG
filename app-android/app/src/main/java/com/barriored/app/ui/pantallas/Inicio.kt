package com.barriored.app.ui.pantallas

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barriored.app.data.model.ConAutor
import com.barriored.app.data.model.Incidencia
import com.barriored.app.data.model.Publicacion
import com.barriored.app.data.repo.IncidenciasRepositorio
import com.barriored.app.data.repo.MuroRepositorio
import com.barriored.app.data.repo.PerfilRepositorio
import com.barriored.app.data.repo.Tiempo
import com.barriored.app.data.repo.TiempoRepositorio
import com.barriored.app.ui.componentes.Avatar
import com.barriored.app.ui.componentes.Cargando
import com.barriored.app.ui.componentes.FilaIncidencia
import com.barriored.app.ui.componentes.PantallaError
import com.barriored.app.ui.componentes.Pastilla
import com.barriored.app.ui.componentes.Tarjeta
import com.barriored.app.ui.componentes.fecha
import com.barriored.app.ui.componentes.mensajeUsuario
import com.barriored.app.ui.theme.Acento
import com.barriored.app.ui.theme.Ambar
import com.barriored.app.ui.theme.BR
import com.barriored.app.ui.theme.Morado
import com.barriored.app.ui.theme.Verde
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val ES = Locale.forLanguageTag("es-ES")

// ------------------------------------------------------------------ Datos

data class EstadoInicio(
    val cargando: Boolean = true,
    val error: String? = null,
    val nombre: String = "",
    val rol: String = "vecino",
    val comunidad: String = "",
    val ultimoAnuncio: ConAutor<Publicacion>? = null,
    val incidencias: List<ConAutor<Incidencia>> = emptyList(),
)

class InicioViewModel : ViewModel() {
    private val perfiles = PerfilRepositorio()
    private val muro = MuroRepositorio(perfiles)
    private val incidencias = IncidenciasRepositorio(perfiles)

    private val _estado = MutableStateFlow(EstadoInicio())
    val estado = _estado.asStateFlow()

    init { cargar() }

    fun cargar() {
        _estado.value = EstadoInicio()
        viewModelScope.launch {
            try {
                // Las consultas van en paralelo (coroutineScope para que un fallo llegue al catch)
                _estado.value = coroutineScope {
                    val perfil = async { perfiles.miPerfil() }
                    val pubs = async { muro.publicaciones(limite = 1) }
                    val incs = async { incidencias.incidencias(limite = 3) }
                    val p = perfil.await()
                    val comunidad = p.comunidadId?.let { perfiles.comunidad(it)?.nombre }.orEmpty()
                    EstadoInicio(
                        cargando = false,
                        nombre = p.nombreVisible,
                        rol = p.role ?: "vecino",
                        comunidad = comunidad,
                        ultimoAnuncio = pubs.await().firstOrNull(),
                        incidencias = incs.await(),
                    )
                }
            } catch (e: Exception) {
                _estado.value = EstadoInicio(cargando = false, error = e.mensajeUsuario())
            }
        }
    }
}

sealed interface EstadoTiempo {
    data object Cargando : EstadoTiempo
    data object SinPermiso : EstadoTiempo
    data object Error : EstadoTiempo
    data class Listo(val tiempo: Tiempo) : EstadoTiempo
}

/** AndroidViewModel porque necesita el Context para la ubicación. */
class TiempoViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = TiempoRepositorio(app)
    private val _estado = MutableStateFlow<EstadoTiempo>(EstadoTiempo.Cargando)
    val estado = _estado.asStateFlow()
    private var cargado = false

    fun cargar(conPermiso: Boolean) {
        if (!conPermiso) { _estado.value = EstadoTiempo.SinPermiso; return }
        if (cargado) return
        cargado = true
        viewModelScope.launch {
            _estado.value = EstadoTiempo.Cargando
            _estado.value = runCatching { repo.tiempoActual() }.getOrNull()
                ?.let { EstadoTiempo.Listo(it) } ?: EstadoTiempo.Error.also { cargado = false }
        }
    }
}

// ------------------------------------------------------------------ Pantalla

private data class AccesoRapido(val texto: String, val icono: ImageVector, val color: Color, val destino: String)

/** Equivale al Dashboard de la web. */
@Composable
fun InicioPantalla(
    onIr: (destino: String) -> Unit,
    vm: InicioViewModel = viewModel(),
) {
    val e by vm.estado.collectAsState()
    when {
        e.cargando -> Cargando()
        e.error != null -> PantallaError(e.error!!, vm::cargar)
        else -> Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TarjetaBienvenida(e.nombre, e.comunidad, e.rol)
            TarjetaCalendario()
            TarjetaTiempo()
            TarjetaAccesos(onIr)
            TarjetaIncidencias(e.incidencias.map { it.item }, onVerTodo = { onIr("incidencias") })
            e.ultimoAnuncio?.let { TarjetaUltimoAnuncio(it) { onIr("muro") } }
            Spacer(Modifier.height(72.dp)) // hueco para el botón del asistente
        }
    }
}

@Composable
private fun TarjetaBienvenida(nombre: String, comunidad: String, rol: String) {
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(nombre, tamano = 44.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Bienvenido, ${nombre.substringBefore(' ')}", color = BR.c.texto, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (comunidad.isNotEmpty()) Text(comunidad, color = BR.c.textoSecundario, fontSize = 13.sp)
            }
            Pastilla(rol, Acento, mayusculas = true)
        }
    }
}

@Composable
private fun TarjetaCalendario() {
    val hoy = remember { LocalDate.now() }
    var mes by remember { mutableStateOf(YearMonth.from(hoy)) }
    var dia by remember { mutableIntStateOf(hoy.dayOfMonth) }
    val seleccionado = mes.atDay(dia.coerceAtMost(mes.lengthOfMonth()))

    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "%02d".format(seleccionado.dayOfMonth),
                color = Acento, fontSize = 58.sp, fontWeight = FontWeight.Bold, lineHeight = 58.sp,
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    seleccionado.dayOfWeek.getDisplayName(TextStyle.FULL, ES).replaceFirstChar { it.uppercase() },
                    color = BR.c.texto, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                )
                Text(
                    "${mes.month.getDisplayName(TextStyle.FULL, ES).replaceFirstChar { it.uppercase() }} ${mes.year}",
                    color = BR.c.textoSecundario, fontSize = 14.sp,
                )
            }
            BotonFlecha(Icons.Filled.ChevronLeft) { mes = mes.minusMonths(1); dia = 1 }
            Spacer(Modifier.width(6.dp))
            BotonFlecha(Icons.Filled.ChevronRight) { mes = mes.plusMonths(1); dia = 1 }
        }
        Spacer(Modifier.height(16.dp))

        // Cabecera L M X J V S D
        Row(Modifier.fillMaxWidth()) {
            listOf("L", "M", "X", "J", "V", "S", "D").forEach {
                Text(it, color = BR.c.textoSecundario, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
        Spacer(Modifier.height(6.dp))

        // Celdas: días del mes anterior, del mes y del siguiente (empezando en lunes)
        val desfase = mes.atDay(1).dayOfWeek.value - DayOfWeek.MONDAY.value
        val diasMesAnterior = mes.minusMonths(1).lengthOfMonth()
        val celdas = buildList {
            for (i in desfase - 1 downTo 0) add((diasMesAnterior - i) to false)
            for (d in 1..mes.lengthOfMonth()) add(d to true)
            var siguiente = 1
            while (size % 7 != 0) add(siguiente++ to false)
        }
        celdas.chunked(7).forEach { semana ->
            Row(Modifier.fillMaxWidth()) {
                semana.forEach { (d, delMes) ->
                    val esSel = delMes && d == seleccionado.dayOfMonth
                    val esHoy = delMes && mes == YearMonth.from(hoy) && d == hoy.dayOfMonth
                    Box(
                        Modifier.weight(1f).aspectRatio(1.15f).padding(2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (esSel) Acento else Color.Transparent)
                            .then(if (esHoy && !esSel) Modifier.border(1.dp, Acento, RoundedCornerShape(6.dp)) else Modifier)
                            .clickable(enabled = delMes) { dia = d },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "$d",
                            fontSize = 14.sp,
                            fontWeight = if (esSel) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                esSel -> Color.White
                                delMes -> BR.c.texto
                                else -> BR.c.textoSecundario.copy(alpha = 0.4f)
                            },
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = BR.c.borde)
        Spacer(Modifier.height(12.dp))
        Text("SIN EVENTOS", color = BR.c.textoSecundario, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(4.dp))
        Text("No hay nada programado para este día.", color = BR.c.textoSecundario.copy(alpha = 0.8f), fontSize = 14.sp)
    }
}

@Composable
private fun BotonFlecha(icono: ImageVector, onClick: () -> Unit) {
    Box(
        Modifier.size(34.dp).clip(RoundedCornerShape(6.dp)).background(BR.c.interior).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icono, contentDescription = null, tint = BR.c.textoSecundario, modifier = Modifier.size(20.dp)) }
}

private fun infoTiempo(codigo: Int): Pair<String, ImageVector> = when (codigo) {
    0 -> "Despejado" to Icons.Filled.WbSunny
    1, 2 -> "Poco nuboso" to Icons.Filled.WbCloudy
    3 -> "Nublado" to Icons.Filled.Cloud
    45, 48 -> "Niebla" to Icons.Filled.Dehaze
    51, 53, 55 -> "Llovizna" to Icons.Filled.Grain
    61, 63, 65 -> "Lluvia" to Icons.Filled.Umbrella
    71, 73, 75, 77 -> "Nieve" to Icons.Filled.AcUnit
    80, 81, 82 -> "Chubascos" to Icons.Filled.Umbrella
    95, 96, 99 -> "Tormenta" to Icons.Filled.Bolt
    else -> "Variable" to Icons.Filled.Cloud
}

@Composable
private fun TarjetaTiempo(vm: TiempoViewModel = viewModel()) {
    val contexto = LocalContext.current
    val estado by vm.estado.collectAsState()
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        vm.cargar(concedido)
    }
    LaunchedEffect(Unit) {
        val tiene = ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (tiene) vm.cargar(true) else pedirPermiso.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    Tarjeta {
        when (val s = estado) {
            EstadoTiempo.Cargando -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(18.dp), color = Acento, strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Obteniendo ubicación…", color = BR.c.textoSecundario)
            }
            EstadoTiempo.SinPermiso, EstadoTiempo.Error -> Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { pedirPermiso.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
            ) {
                Icon(Icons.Filled.LocationOff, contentDescription = null, tint = BR.c.textoSecundario)
                Spacer(Modifier.width(10.dp))
                Text(
                    if (s == EstadoTiempo.SinPermiso) "Activa los permisos de ubicación." else "No se pudo obtener el tiempo. Toca para reintentar.",
                    color = BR.c.textoSecundario,
                )
            }
            is EstadoTiempo.Listo -> {
                val (desc, icono) = infoTiempo(s.tiempo.codigo)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Acento, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(s.tiempo.lugar, color = BR.c.textoSecundario, fontSize = 13.sp)
                        }
                        Text("${s.tiempo.temperatura}°C", color = BR.c.texto, fontSize = 42.sp, fontWeight = FontWeight.Bold)
                        Text(desc, color = BR.c.textoSecundario, fontSize = 15.sp)
                    }
                    Icon(icono, contentDescription = desc, tint = Acento, modifier = Modifier.size(58.dp))
                }
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = BR.c.borde)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    DatoTiempo(Icons.Filled.Air, "${s.tiempo.viento} km/h")
                    DatoTiempo(Icons.Filled.WaterDrop, "${s.tiempo.humedad}%")
                }
            }
        }
    }
}

@Composable
private fun DatoTiempo(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = Acento, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(5.dp))
        Text(texto, color = BR.c.textoSecundario, fontSize = 14.sp)
    }
}

@Composable
private fun TituloTarjeta(icono: ImageVector, texto: String, colorIcono: Color = Acento) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(texto, color = BR.c.texto, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
private fun TarjetaAccesos(onIr: (String) -> Unit) {
    val accesos = listOf(
        AccesoRapido("Incidencias", Icons.Filled.Warning, Ambar, "incidencias"),
        AccesoRapido("Muro", Icons.Filled.PushPin, Verde, "muro"),
        AccesoRapido("Documentos", Icons.Filled.FolderOpen, Acento, "perfil"),
        AccesoRapido("Chat", Icons.AutoMirrored.Filled.Chat, Morado, "chat"),
    )
    Tarjeta {
        TituloTarjeta(Icons.Filled.Bolt, "Accesos rápidos")
        Spacer(Modifier.height(14.dp))
        accesos.chunked(2).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 10.dp)) {
                fila.forEach { a ->
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BR.c.interior)
                            .clickable { onIr(a.destino) }
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(a.icono, contentDescription = null, tint = a.color, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(a.texto, color = BR.c.textoSecundario, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaIncidencias(lista: List<Incidencia>, onVerTodo: () -> Unit) {
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { TituloTarjeta(Icons.Filled.Warning, "Últimas incidencias") }
            Text("Ver todo", color = Acento, fontSize = 13.sp, modifier = Modifier.clickable(onClick = onVerTodo))
        }
        Spacer(Modifier.height(14.dp))
        if (lista.isEmpty()) {
            Text("No hay incidencias registradas.", color = BR.c.textoSecundario, fontSize = 14.sp)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { lista.forEach { FilaIncidencia(it) } }
        }
    }
}

@Composable
private fun TarjetaUltimoAnuncio(p: ConAutor<Publicacion>, onClick: () -> Unit) {
    // Tarjeta con borde izquierdo azul, como "Último anuncio del muro" en la web
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Acento)
            .clickable(onClick = onClick),
    ) {
        Column(
            Modifier
                .padding(start = 4.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp))
                .background(BR.c.tarjeta)
                .padding(18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.PushPin, contentDescription = null, tint = Acento, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("ÚLTIMO ANUNCIO DEL MURO", color = Acento, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text("${p.autor} · ${fecha(p.item.creadoEn)}", color = BR.c.textoSecundario, fontSize = 13.sp)
            p.item.titulo?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, color = BR.c.texto, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(p.item.contenido, color = BR.c.textoSecundario, fontSize = 14.sp, lineHeight = 21.sp, maxLines = 4)
        }
    }
}
