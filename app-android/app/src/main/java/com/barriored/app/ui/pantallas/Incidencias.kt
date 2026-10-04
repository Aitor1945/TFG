package com.barriored.app.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barriored.app.data.model.ConAutor
import com.barriored.app.data.model.EstadoIncidencia
import com.barriored.app.data.model.Incidencia
import com.barriored.app.data.model.Perfil
import com.barriored.app.data.repo.IncidenciasRepositorio
import com.barriored.app.data.repo.PerfilRepositorio
import com.barriored.app.ui.componentes.BotonPastilla
import com.barriored.app.ui.componentes.BotonPrincipal
import com.barriored.app.ui.componentes.CampoTexto
import com.barriored.app.ui.componentes.Cargando
import com.barriored.app.ui.componentes.PantallaError
import com.barriored.app.ui.componentes.Tarjeta
import com.barriored.app.ui.componentes.TarjetaIncidencia
import com.barriored.app.ui.componentes.TextoVacio
import com.barriored.app.ui.componentes.TituloPagina
import com.barriored.app.ui.componentes.mensajeUsuario
import com.barriored.app.ui.theme.Acento
import com.barriored.app.ui.theme.BR
import com.barriored.app.ui.theme.Rojo
import com.barriored.app.ui.theme.Verde
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoIncidencias(
    val cargando: Boolean = true,
    val error: String? = null,
    val yo: Perfil? = null,
    val lista: List<ConAutor<Incidencia>> = emptyList(),
    val filtro: EstadoIncidencia? = null, // null = todas
    val enviando: Boolean = false,
    val errorAccion: String? = null,
) {
    val filtradas get() = filtro?.let { f -> lista.filter { it.item.estado == f.valor } } ?: lista
}

class IncidenciasViewModel : ViewModel() {
    private val perfiles = PerfilRepositorio()
    private val repo = IncidenciasRepositorio(perfiles)

    private val _estado = MutableStateFlow(EstadoIncidencias())
    val estado = _estado.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = it.lista.isEmpty(), error = null) }
            try {
                val yo = _estado.value.yo ?: perfiles.miPerfil()
                val lista = repo.incidencias()
                _estado.update { it.copy(cargando = false, yo = yo, lista = lista) }
            } catch (e: Exception) {
                _estado.update { it.copy(cargando = false, error = e.mensajeUsuario()) }
            }
        }
    }

    fun filtrar(f: EstadoIncidencia?) = _estado.update { it.copy(filtro = f) }

    private fun ejecutar(alTerminar: (Boolean) -> Unit = {}, accion: suspend () -> Unit) {
        viewModelScope.launch {
            _estado.update { it.copy(enviando = true, errorAccion = null) }
            val ok = try {
                accion()
                true
            } catch (e: Exception) {
                _estado.update { it.copy(errorAccion = e.mensajeUsuario()) }
                false
            }
            _estado.update { it.copy(enviando = false) }
            alTerminar(ok)
            if (ok) cargar()
        }
    }

    fun crear(titulo: String, descripcion: String, alTerminar: (Boolean) -> Unit) {
        val yo = _estado.value.yo ?: return
        val comunidad = yo.comunidadId ?: return
        ejecutar(alTerminar) { repo.crear(titulo.trim(), descripcion.trim(), yo.id, comunidad) }
    }

    fun cambiarEstado(id: String, nuevo: EstadoIncidencia) = ejecutar { repo.cambiarEstado(id, nuevo) }

    fun borrar(id: String) = ejecutar { repo.borrar(id) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IncidenciasPantalla(vm: IncidenciasViewModel = viewModel()) {
    val e by vm.estado.collectAsState()
    var formulario by rememberSaveable { mutableStateOf(false) }
    var titulo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var aBorrar by rememberSaveable { mutableStateOf<String?>(null) }
    val gestiona = e.yo?.puedeGestionar == true

    when {
        e.cargando -> Cargando()
        e.error != null -> PantallaError(e.error!!, vm::cargar)
        else -> LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { TituloPagina("Incidencias", "Consulta y seguimiento de incidencias de la comunidad") }

            item {
              Column {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FiltroPastilla("Todas", e.filtro == null) { vm.filtrar(null) }
                    EstadoIncidencia.entries.forEach { f ->
                        FiltroPastilla(f.etiqueta, e.filtro == f) { vm.filtrar(f) }
                    }
                }
                Spacer(Modifier.height(14.dp))
                // Cualquier vecino puede reportar una incidencia
                Text(
                    if (formulario) "Cancelar" else "+ Nueva incidencia",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (formulario) BR.c.interior else Acento)
                        .clickable { formulario = !formulario }
                        .padding(horizontal = 22.dp, vertical = 12.dp),
                )
              }
            }

            if (formulario) item {
                Tarjeta {
                    Text("Nueva incidencia", color = BR.c.texto, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    CampoTexto(titulo, { titulo = it }, "Título (p. ej. Luz del portal fundida)")
                    Spacer(Modifier.height(10.dp))
                    CampoTexto(descripcion, { descripcion = it }, "Describe el problema", unaLinea = false, lineasMin = 3)
                    e.errorAccion?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = Rojo, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    BotonPrincipal(
                        "Enviar incidencia",
                        onClick = {
                            vm.crear(titulo, descripcion) { ok ->
                                if (ok) { titulo = ""; descripcion = ""; formulario = false }
                            }
                        },
                        habilitado = titulo.isNotBlank() && descripcion.isNotBlank(),
                        cargando = e.enviando,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item {
                val n = e.filtradas.size
                Text("$n ${if (n == 1) "incidencia" else "incidencias"}", color = BR.c.textoSecundario, fontSize = 14.sp)
            }
            if (e.filtradas.isEmpty()) item { TextoVacio("No hay incidencias.") }
            items(e.filtradas, key = { it.item.id }) { inc ->
                TarjetaIncidencia(inc) {
                    if (gestiona) {
                        AccionesGestion(
                            estado = EstadoIncidencia.de(inc.item.estado),
                            onCambiar = { vm.cambiarEstado(inc.item.id, it) },
                            onBorrar = { aBorrar = inc.item.id },
                        )
                    }
                }
            }
        }
    }

    aBorrar?.let { id ->
        AlertDialog(
            onDismissRequest = { aBorrar = null },
            containerColor = BR.c.tarjeta,
            title = { Text("Eliminar incidencia", color = BR.c.texto) },
            text = { Text("¿Seguro que quieres eliminarla? No se puede deshacer.", color = BR.c.textoSecundario) },
            confirmButton = {
                TextButton(onClick = { vm.borrar(id); aBorrar = null }) { Text("Eliminar", color = Rojo) }
            },
            dismissButton = { TextButton(onClick = { aBorrar = null }) { Text("Cancelar", color = BR.c.textoSecundario) } },
        )
    }
}

@Composable
private fun FiltroPastilla(texto: String, activo: Boolean, onClick: () -> Unit) {
    Text(
        texto,
        color = if (activo) Color.White else BR.c.textoSecundario,
        fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
        fontSize = 15.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (activo) Acento else BR.c.tarjeta)
            .border(1.dp, if (activo) Acento else BR.c.borde, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 9.dp),
    )
}

/** Botones que en la web solo ven ayuntamiento, presidente y admin. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccionesGestion(
    estado: EstadoIncidencia,
    onCambiar: (EstadoIncidencia) -> Unit,
    onBorrar: () -> Unit,
) {
    FlowRow(
        Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (estado == EstadoIncidencia.PENDIENTE) {
            BotonPastilla("Marcar en proceso", Acento) { onCambiar(EstadoIncidencia.EN_PROCESO) }
        }
        if (estado != EstadoIncidencia.RESUELTA) {
            BotonPastilla("Marcar como resuelta", Verde) { onCambiar(EstadoIncidencia.RESUELTA) }
        }
        BotonPastilla("Eliminar", Rojo, onBorrar)
    }
}
