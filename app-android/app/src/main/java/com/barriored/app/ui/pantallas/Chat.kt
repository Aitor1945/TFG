package com.barriored.app.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barriored.app.data.model.Mensaje
import com.barriored.app.data.model.Perfil
import com.barriored.app.data.repo.AuthRepositorio
import com.barriored.app.data.repo.ChatRepositorio
import com.barriored.app.data.repo.PerfilRepositorio
import com.barriored.app.ui.componentes.Cargando
import com.barriored.app.ui.componentes.PantallaError
import com.barriored.app.ui.componentes.TextoVacio
import com.barriored.app.ui.componentes.fechaHoraCorta
import com.barriored.app.ui.componentes.mensajeUsuario
import com.barriored.app.ui.theme.Acento
import com.barriored.app.ui.theme.BR
import com.barriored.app.ui.theme.Rojo
import io.github.jan.supabase.realtime.RealtimeChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ---------- Lista de vecinos ----------

data class EstadoListaChats(
    val cargando: Boolean = true,
    val error: String? = null,
    val vecinos: List<Perfil> = emptyList(),
    val noLeidos: Map<String, Int> = emptyMap(),
)

class ListaChatsViewModel : ViewModel() {
    private val perfiles = PerfilRepositorio()
    private val chat = ChatRepositorio()
    private val auth = AuthRepositorio()

    private val _estado = MutableStateFlow(EstadoListaChats())
    val estado = _estado.asStateFlow()

    fun cargar() {
        val yo = auth.idUsuarioActual ?: return
        viewModelScope.launch {
            _estado.update { it.copy(cargando = it.vecinos.isEmpty(), error = null) }
            try {
                val vecinos = perfiles.vecinos().sortedBy { it.nombreVisible.lowercase() }
                _estado.value = EstadoListaChats(
                    cargando = false,
                    vecinos = vecinos,
                    noLeidos = chat.noLeidos(yo),
                )
            } catch (e: Exception) {
                _estado.update { it.copy(cargando = false, error = e.mensajeUsuario()) }
            }
        }
    }
}

@Composable
fun ListaChatsPantalla(onAbrir: (id: String, nombre: String, rol: String) -> Unit, vm: ListaChatsViewModel = viewModel()) {
    val e by vm.estado.collectAsState()
    // Se recarga cada vez que se vuelve a la lista, para actualizar los no leídos
    LaunchedEffect(Unit) { vm.cargar() }

    when {
        e.cargando -> Cargando()
        e.error != null -> PantallaError(e.error!!, vm::cargar)
        else -> Column(Modifier.fillMaxSize().padding(16.dp)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BR.c.tarjeta)
                    .border(1.dp, BR.c.borde, RoundedCornerShape(16.dp)),
            ) {
                Text(
                    "BarrioChat",
                    color = BR.c.texto, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                )
                HorizontalDivider(color = BR.c.borde)
                if (e.vecinos.isEmpty()) {
                    TextoVacio("No hay vecinos disponibles")
                } else {
                    // Los que tienen mensajes sin leer, arriba
                    val ordenados = e.vecinos.sortedByDescending { e.noLeidos[it.id] ?: 0 }
                    LazyColumn(contentPadding = PaddingValues(8.dp)) {
                        items(ordenados, key = { it.id }) { v ->
                            FilaVecino(v, e.noLeidos[v.id] ?: 0) { onAbrir(v.id, v.nombreVisible, v.role ?: "vecino") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaVecino(v: Perfil, pendientes: Int, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            InicialChat(v.nombreVisible, 44.dp)
            if (pendientes > 0) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Rojo)
                        .border(2.dp, BR.c.tarjeta, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (pendientes > 99) "99+" else "$pendientes", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                v.nombreVisible,
                color = BR.c.texto,
                fontSize = 16.sp,
                fontWeight = if (pendientes > 0) FontWeight.Bold else FontWeight.SemiBold,
            )
            Text((v.role ?: "vecino").replaceFirstChar { it.uppercase() }, color = BR.c.textoSecundario, fontSize = 13.sp)
        }
    }
}

/** Avatar del chat: círculo oscuro con la inicial en azul (bc-avatar en la web). */
@Composable
private fun InicialChat(nombre: String, tamano: androidx.compose.ui.unit.Dp) {
    Box(
        Modifier.size(tamano).clip(CircleShape).background(BR.c.interior),
        contentAlignment = Alignment.Center,
    ) {
        Text(nombre.take(1).uppercase(), color = Acento, fontWeight = FontWeight.Bold, fontSize = (tamano.value * 0.38f).sp)
    }
}

// ---------- Conversación ----------

data class EstadoConversacion(
    val cargando: Boolean = true,
    val error: String? = null,
    val yo: String = "",
    val rolOtro: String = "",
    val mensajes: List<Mensaje> = emptyList(),
)

/** Recibe el id del vecino desde la ruta "conversacion/{id}/{nombre}" gracias a SavedStateHandle. */
class ConversacionViewModel(guardado: SavedStateHandle) : ViewModel() {
    private val chat = ChatRepositorio()
    private val otro: String = checkNotNull(guardado.get<String>("id"))
    private val yo: String = checkNotNull(AuthRepositorio().idUsuarioActual)
    private var canal: RealtimeChannel? = null

    private val _estado = MutableStateFlow(
        EstadoConversacion(yo = yo, rolOtro = guardado.get<String>("rol").orEmpty())
    )
    val estado = _estado.asStateFlow()

    init {
        cargar()
        escucharTiempoReal()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            try {
                val mensajes = chat.conversacion(yo, otro)
                _estado.update { it.copy(cargando = false, mensajes = mensajes) }
                chat.marcarLeidos(yo, otro)
            } catch (e: Exception) {
                _estado.update { it.copy(cargando = false, error = e.mensajeUsuario()) }
            }
        }
    }

    private fun escucharTiempoReal() {
        val (c, nuevos) = chat.canal(yo, otro)
        canal = c
        viewModelScope.launch {
            launch {
                nuevos.collect { m ->
                    agregar(m)
                    if (m.receptorId == yo) runCatching { chat.marcarLeidos(yo, otro) }
                }
            }
            runCatching { chat.suscribir(c) }
        }
    }

    /** Añade sin duplicar (el mensaje propio llega por el insert y también por tiempo real). */
    private fun agregar(m: Mensaje) = _estado.update { e ->
        if (e.mensajes.any { it.id == m.id }) e else e.copy(mensajes = e.mensajes + m)
    }

    fun enviar(texto: String) {
        if (texto.isBlank()) return
        viewModelScope.launch {
            try {
                agregar(chat.enviar(yo, otro, texto.trim()))
            } catch (e: Exception) {
                _estado.update { it.copy(error = e.mensajeUsuario()) }
            }
        }
    }

    override fun onCleared() {
        // viewModelScope ya está cancelado aquí, así que el canal se cierra en otro scope
        canal?.let { c -> CoroutineScope(Dispatchers.IO).launch { runCatching { chat.cerrar(c) } } }
    }
}

@Composable
fun ConversacionPantalla(nombre: String, onVolver: () -> Unit, vm: ConversacionViewModel = viewModel()) {
    val e by vm.estado.collectAsState()
    var texto by rememberSaveable { mutableStateOf("") }
    val lista = rememberLazyListState()

    // Baja al último mensaje cuando llega uno nuevo
    LaunchedEffect(e.mensajes.size) {
        if (e.mensajes.isNotEmpty()) lista.animateScrollToItem(e.mensajes.lastIndex)
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .padding(12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(BR.c.fondo)
            .border(1.dp, BR.c.borde, RoundedCornerShape(16.dp)),
    ) {
        // Cabecera con el vecino
        Row(
            Modifier.fillMaxWidth().background(BR.c.tarjeta).padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Acento)
            }
            InicialChat(nombre, 36.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(nombre, color = BR.c.texto, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(e.rolOtro, color = BR.c.textoSecundario, fontSize = 13.sp)
            }
        }
        HorizontalDivider(color = BR.c.borde)

        Box(Modifier.weight(1f)) {
            when {
                e.cargando -> Cargando()
                e.error != null -> PantallaError(e.error!!, vm::cargar)
                e.mensajes.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Di hola a $nombre 👋", color = BR.c.textoSecundario)
                }
                else -> LazyColumn(
                    state = lista,
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(e.mensajes, key = { it.id }) { Burbuja(it, esMio = it.emisorId == e.yo, nombreOtro = nombre) }
                }
            }
        }

        // Caja para escribir
        HorizontalDivider(color = BR.c.borde)
        Row(
            Modifier.fillMaxWidth().background(BR.c.tarjeta).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                placeholder = { Text("Escribe tu mensaje...", color = BR.c.textoSecundario) },
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BR.c.fondo, unfocusedContainerColor = BR.c.fondo,
                    focusedBorderColor = Acento, unfocusedBorderColor = BR.c.borde,
                    focusedTextColor = BR.c.texto, unfocusedTextColor = BR.c.texto,
                ),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "Enviar",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (texto.isBlank()) Acento.copy(alpha = 0.45f) else Acento)
                    .clickable(enabled = texto.isNotBlank()) { vm.enviar(texto); texto = "" }
                    .padding(horizontal = 18.dp, vertical = 17.dp),
            )
        }
    }
}

/** Burbujas como en la web: las mías azules con la esquina inferior derecha en pico. */
@Composable
private fun Burbuja(m: Mensaje, esMio: Boolean, nombreOtro: String) {
    Box(Modifier.fillMaxWidth(), contentAlignment = if (esMio) Alignment.CenterEnd else Alignment.CenterStart) {
        Column(
            Modifier
                .widthIn(max = 280.dp)
                .clip(
                    if (esMio) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
                    else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                )
                .background(if (esMio) Acento else BR.c.interior)
                .padding(horizontal = 12.dp, vertical = 9.dp),
        ) {
            if (!esMio) {
                Text(nombreOtro, color = BR.c.textoSecundario, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
            }
            Text(m.content.orEmpty(), color = if (esMio) Color.White else BR.c.texto, fontSize = 15.sp, lineHeight = 21.sp)
            Text(
                fechaHoraCorta(m.creadoEn),
                color = if (esMio) Color.White.copy(alpha = 0.75f) else BR.c.textoSecundario,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.End).padding(top = 3.dp),
            )
        }
    }
}
