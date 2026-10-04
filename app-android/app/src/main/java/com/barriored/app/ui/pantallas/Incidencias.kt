package com.barriored.app.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.barriored.app.ui.componentes.Cargando
import com.barriored.app.ui.componentes.DialogoTituloTexto
import com.barriored.app.ui.componentes.PantallaError
import com.barriored.app.ui.componentes.TarjetaIncidencia
import com.barriored.app.ui.componentes.TextoVacio
import com.barriored.app.ui.componentes.mensajeUsuario
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
            _estado.update { it.copy(cargando = true, error = null) }
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

    private fun ejecutar(accion: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                accion()
                cargar()
            } catch (e: Exception) {
                _estado.update { it.copy(error = e.mensajeUsuario()) }
            }
        }
    }

    fun crear(titulo: String, descripcion: String) {
        val yo = _estado.value.yo ?: return
        val comunidad = yo.comunidadId ?: return
        ejecutar { repo.crear(titulo, descripcion, yo.id, comunidad) }
    }

    fun cambiarEstado(id: String, nuevo: EstadoIncidencia) = ejecutar { repo.cambiarEstado(id, nuevo) }

    fun borrar(id: String) = ejecutar { repo.borrar(id) }
}

@Composable
fun IncidenciasPantalla(vm: IncidenciasViewModel = viewModel()) {
    val e by vm.estado.collectAsState()
    var creando by rememberSaveable { mutableStateOf(false) }
    var aBorrar by rememberSaveable { mutableStateOf<String?>(null) }
    val gestiona = e.yo?.puedeGestionar == true

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            // Cualquier vecino puede reportar una incidencia
            FloatingActionButton(onClick = { creando = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva incidencia")
            }
        },
    ) { relleno ->
        when {
            e.cargando -> Cargando(Modifier.padding(relleno))
            e.error != null -> PantallaError(e.error!!, vm::cargar)
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(relleno),
            ) {
                item { Filtros(e.filtro, vm::filtrar) }
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
    }

    if (creando) {
        DialogoTituloTexto(
            titulo = "Nueva incidencia",
            etiquetaTexto = "Describe el problema",
            onEnviar = { t, d -> vm.crear(t, d); creando = false },
            onCerrar = { creando = false },
        )
    }

    aBorrar?.let { id ->
        AlertDialog(
            onDismissRequest = { aBorrar = null },
            title = { Text("Eliminar incidencia") },
            text = { Text("¿Seguro que quieres eliminarla? No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { vm.borrar(id); aBorrar = null }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { aBorrar = null }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun Filtros(actual: EstadoIncidencia?, onFiltrar: (EstadoIncidencia?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(selected = actual == null, onClick = { onFiltrar(null) }, label = { Text("Todas") })
        }
        items(EstadoIncidencia.entries) { f ->
            FilterChip(selected = actual == f, onClick = { onFiltrar(f) }, label = { Text(f.etiqueta) })
        }
    }
}

/** Botones que en la web solo ven ayuntamiento, presidente y admin. */
@Composable
private fun AccionesGestion(
    estado: EstadoIncidencia,
    onCambiar: (EstadoIncidencia) -> Unit,
    onBorrar: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        if (estado == EstadoIncidencia.PENDIENTE) {
            TextButton(onClick = { onCambiar(EstadoIncidencia.EN_PROCESO) }) { Text("En proceso") }
        }
        if (estado != EstadoIncidencia.RESUELTA) {
            TextButton(onClick = { onCambiar(EstadoIncidencia.RESUELTA) }) { Text("Resuelta") }
        }
        TextButton(onClick = onBorrar) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
    }
}
