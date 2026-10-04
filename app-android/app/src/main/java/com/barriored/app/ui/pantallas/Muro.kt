package com.barriored.app.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import com.barriored.app.data.model.Perfil
import com.barriored.app.data.model.Publicacion
import com.barriored.app.data.repo.MuroRepositorio
import com.barriored.app.data.repo.PerfilRepositorio
import com.barriored.app.ui.componentes.Cargando
import com.barriored.app.ui.componentes.DialogoTituloTexto
import com.barriored.app.ui.componentes.PantallaError
import com.barriored.app.ui.componentes.TarjetaPublicacion
import com.barriored.app.ui.componentes.TextoVacio
import com.barriored.app.ui.componentes.mensajeUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoMuro(
    val cargando: Boolean = true,
    val error: String? = null,
    val yo: Perfil? = null,
    val anuncios: List<ConAutor<Publicacion>> = emptyList(),
)

class MuroViewModel : ViewModel() {
    private val perfiles = PerfilRepositorio()
    private val muro = MuroRepositorio(perfiles)

    private val _estado = MutableStateFlow(EstadoMuro())
    val estado = _estado.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            try {
                val yo = _estado.value.yo ?: perfiles.miPerfil()
                _estado.value = EstadoMuro(cargando = false, yo = yo, anuncios = muro.publicaciones())
            } catch (e: Exception) {
                _estado.update { it.copy(cargando = false, error = e.mensajeUsuario()) }
            }
        }
    }

    fun publicar(titulo: String, contenido: String) {
        val yo = _estado.value.yo ?: return
        val comunidad = yo.comunidadId ?: return
        viewModelScope.launch {
            try {
                muro.publicar(titulo, contenido, yo.id, comunidad)
                cargar()
            } catch (e: Exception) {
                _estado.update { it.copy(error = e.mensajeUsuario()) }
            }
        }
    }
}

@Composable
fun MuroPantalla(vm: MuroViewModel = viewModel()) {
    val e by vm.estado.collectAsState()
    var creando by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            // Igual que en la web: solo presidente, admin y ayuntamiento publican anuncios
            if (e.yo?.puedeGestionar == true) {
                FloatingActionButton(onClick = { creando = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Nuevo anuncio")
                }
            }
        },
    ) { relleno ->
        when {
            e.cargando -> Cargando(Modifier.padding(relleno))
            e.error != null -> PantallaError(e.error!!, vm::cargar)
            e.anuncios.isEmpty() -> TextoVacio("Todavía no hay anuncios en el muro.")
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(relleno),
            ) {
                items(e.anuncios, key = { it.item.id }) { TarjetaPublicacion(it) }
            }
        }
    }

    if (creando) {
        DialogoTituloTexto(
            titulo = "Nuevo anuncio",
            etiquetaTexto = "Contenido",
            onEnviar = { t, c -> vm.publicar(t, c); creando = false },
            onCerrar = { creando = false },
        )
    }
}
