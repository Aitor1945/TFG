package com.barriored.app.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.barriored.app.ui.componentes.hora
import com.barriored.app.ui.componentes.mensajeUsuario
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
fun ListaChatsPantalla(onAbrir: (id: String, nombre: String) -> Unit, vm: ListaChatsViewModel = viewModel()) {
    val e by vm.estado.collectAsState()
    // Se recarga cada vez que se vuelve a la lista, para actualizar los no leídos
    LaunchedEffect(Unit) { vm.cargar() }

    when {
        e.cargando -> Cargando()
        e.error != null -> PantallaError(e.error!!, vm::cargar)
        e.vecinos.isEmpty() -> TextoVacio("No hay vecinos con los que chatear.")
        else -> LazyColumn {
            items(e.vecinos, key = { it.id }) { v ->
                val pendientes = e.noLeidos[v.id] ?: 0
                ListItem(
                    headlineContent = {
                        Text(v.nombreVisible, fontWeight = if (pendientes > 0) FontWeight.Bold else null)
                    },
                    supportingContent = { v.role?.let { Text(it.replaceFirstChar { c -> c.uppercase() }) } },
                    leadingContent = { Inicial(v.nombreVisible) },
                    trailingContent = { if (pendientes > 0) Badge { Text("$pendientes") } },
                    modifier = Modifier.clickable { onAbrir(v.id, v.nombreVisible) },
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun Inicial(nombre: String) {
    Box(
        Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(nombre.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
    }
}

// ---------- Conversación ----------

data class EstadoConversacion(
    val cargando: Boolean = true,
    val error: String? = null,
    val yo: String = "",
    val mensajes: List<Mensaje> = emptyList(),
)

/** Recibe el id del vecino desde la ruta "conversacion/{id}/{nombre}" gracias a SavedStateHandle. */
class ConversacionViewModel(guardado: SavedStateHandle) : ViewModel() {
    private val chat = ChatRepositorio()
    private val otro: String = checkNotNull(guardado.get<String>("id"))
    private val yo: String = checkNotNull(AuthRepositorio().idUsuarioActual)
    private var canal: RealtimeChannel? = null

    private val _estado = MutableStateFlow(EstadoConversacion(yo = yo))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversacionPantalla(nombre: String, onVolver: () -> Unit, vm: ConversacionViewModel = viewModel()) {
    val e by vm.estado.collectAsState()
    var texto by rememberSaveable { mutableStateOf("") }
    val lista = rememberLazyListState()

    // Baja al último mensaje cuando llega uno nuevo
    LaunchedEffect(e.mensajes.size) {
        if (e.mensajes.isNotEmpty()) lista.animateScrollToItem(e.mensajes.lastIndex)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = { Text(nombre) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { relleno ->
        Column(Modifier.fillMaxSize().padding(relleno).imePadding()) {
            Box(Modifier.weight(1f)) {
                when {
                    e.cargando -> Cargando()
                    e.error != null -> PantallaError(e.error!!, vm::cargar)
                    e.mensajes.isEmpty() -> TextoVacio("Aún no hay mensajes. ¡Saluda!")
                    else -> LazyColumn(
                        state = lista,
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(e.mensajes, key = { it.id }) { Burbuja(it, esMio = it.emisorId == e.yo) }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    placeholder = { Text("Escribe un mensaje…") },
                    maxLines = 4,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { vm.enviar(texto); texto = "" },
                    enabled = texto.isNotBlank(),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                }
            }
        }
    }
}

@Composable
private fun Burbuja(m: Mensaje, esMio: Boolean) {
    Box(Modifier.fillMaxWidth(), contentAlignment = if (esMio) Alignment.CenterEnd else Alignment.CenterStart) {
        Column(
            Modifier
                .widthIn(max = 280.dp)
                .background(
                    if (esMio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(16.dp),
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            val color = if (esMio) Color.White else MaterialTheme.colorScheme.onSurface
            Text(m.content.orEmpty(), color = color)
            Text(
                hora(m.creadoEn),
                color = color.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}
