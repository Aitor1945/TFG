package com.barriored.app.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barriored.app.data.model.ConAutor
import com.barriored.app.data.model.Incidencia
import com.barriored.app.data.model.Publicacion
import com.barriored.app.data.repo.IncidenciasRepositorio
import com.barriored.app.data.repo.MuroRepositorio
import com.barriored.app.data.repo.PerfilRepositorio
import com.barriored.app.ui.componentes.Cargando
import com.barriored.app.ui.componentes.PantallaError
import com.barriored.app.ui.componentes.TarjetaIncidencia
import com.barriored.app.ui.componentes.TarjetaPublicacion
import com.barriored.app.ui.componentes.mensajeUsuario
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EstadoInicio(
    val cargando: Boolean = true,
    val error: String? = null,
    val nombre: String = "",
    val comunidad: String = "",
    val publicaciones: List<ConAutor<Publicacion>> = emptyList(),
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
                // Las tres consultas van en paralelo (coroutineScope para que un fallo llegue al catch)
                _estado.value = coroutineScope {
                    val perfil = async { perfiles.miPerfil() }
                    val pubs = async { muro.publicaciones(limite = 3) }
                    val incs = async { incidencias.incidencias(limite = 3) }
                    val p = perfil.await()
                    val comunidad = p.comunidadId?.let { perfiles.comunidad(it)?.nombre }.orEmpty()
                    EstadoInicio(
                        cargando = false,
                        nombre = p.nombreVisible,
                        comunidad = comunidad,
                        publicaciones = pubs.await(),
                        incidencias = incs.await(),
                    )
                }
            } catch (e: Exception) {
                _estado.value = EstadoInicio(cargando = false, error = e.mensajeUsuario())
            }
        }
    }
}

@Composable
fun InicioPantalla(
    onVerMuro: () -> Unit,
    onVerIncidencias: () -> Unit,
    vm: InicioViewModel = viewModel(),
) {
    val e by vm.estado.collectAsState()
    when {
        e.cargando -> Cargando()
        e.error != null -> PantallaError(e.error!!, vm::cargar)
        else -> LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Hola, ${e.nombre} 👋", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                if (e.comunidad.isNotEmpty()) {
                    Text(e.comunidad, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item { Cabecera("Últimos anuncios", onVerMuro) }
            if (e.publicaciones.isEmpty()) item { Text("No hay anuncios todavía.") }
            items(e.publicaciones, key = { "p" + it.item.id }) { TarjetaPublicacion(it, resumida = true) }

            item { Cabecera("Últimas incidencias", onVerIncidencias) }
            if (e.incidencias.isEmpty()) item { Text("No hay incidencias.") }
            items(e.incidencias, key = { "i" + it.item.id }) { TarjetaIncidencia(it, resumida = true) }
        }
    }
}

@Composable
private fun Cabecera(titulo: String, onVerTodo: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(titulo, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        TextButton(onClick = onVerTodo) { Text("Ver todo") }
    }
}
