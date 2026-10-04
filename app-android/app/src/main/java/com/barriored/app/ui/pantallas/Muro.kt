package com.barriored.app.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barriored.app.data.model.ConAutor
import com.barriored.app.data.model.Perfil
import com.barriored.app.data.model.Publicacion
import com.barriored.app.data.repo.MuroRepositorio
import com.barriored.app.data.repo.PerfilRepositorio
import com.barriored.app.ui.componentes.BotonPrincipal
import com.barriored.app.ui.componentes.CampoTexto
import com.barriored.app.ui.componentes.Cargando
import com.barriored.app.ui.componentes.PantallaError
import com.barriored.app.ui.componentes.Tarjeta
import com.barriored.app.ui.componentes.TarjetaPublicacion
import com.barriored.app.ui.componentes.TextoVacio
import com.barriored.app.ui.componentes.TituloPagina
import com.barriored.app.ui.componentes.mensajeUsuario
import com.barriored.app.ui.theme.BR
import com.barriored.app.ui.theme.Rojo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoMuro(
    val cargando: Boolean = true,
    val error: String? = null,
    val yo: Perfil? = null,
    val anuncios: List<ConAutor<Publicacion>> = emptyList(),
    val publicando: Boolean = false,
    val errorPublicar: String? = null,
)

class MuroViewModel : ViewModel() {
    private val perfiles = PerfilRepositorio()
    private val muro = MuroRepositorio(perfiles)

    private val _estado = MutableStateFlow(EstadoMuro())
    val estado = _estado.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = it.anuncios.isEmpty(), error = null) }
            try {
                val yo = _estado.value.yo ?: perfiles.miPerfil()
                val anuncios = muro.publicaciones()
                _estado.update { it.copy(cargando = false, yo = yo, anuncios = anuncios) }
            } catch (e: Exception) {
                _estado.update { it.copy(cargando = false, error = e.mensajeUsuario()) }
            }
        }
    }

    /** Devuelve true si se publicó, para limpiar el formulario. */
    fun publicar(titulo: String, contenido: String, alTerminar: (Boolean) -> Unit) {
        val yo = _estado.value.yo ?: return
        val comunidad = yo.comunidadId ?: return
        viewModelScope.launch {
            _estado.update { it.copy(publicando = true, errorPublicar = null) }
            val ok = runCatching { muro.publicar(titulo.trim(), contenido.trim(), yo.id, comunidad) }
                .onFailure { e -> _estado.update { it.copy(errorPublicar = e.mensajeUsuario()) } }
                .isSuccess
            _estado.update { it.copy(publicando = false) }
            alTerminar(ok)
            if (ok) cargar()
        }
    }
}

@Composable
fun MuroPantalla(vm: MuroViewModel = viewModel()) {
    val e by vm.estado.collectAsState()
    var titulo by rememberSaveable { mutableStateOf("") }
    var contenido by rememberSaveable { mutableStateOf("") }

    when {
        e.cargando -> Cargando()
        e.error != null -> PantallaError(e.error!!, vm::cargar)
        else -> LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { TituloPagina("Muro Comunitario", "Novedades y anuncios de la comunidad") }

            // Igual que en la web: solo presidente, admin y ayuntamiento publican
            if (e.yo?.puedeGestionar == true) item {
                Tarjeta {
                    Text("Nuevo mensaje", color = BR.c.texto, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    CampoTexto(titulo, { titulo = it }, "Título")
                    Spacer(Modifier.height(10.dp))
                    CampoTexto(contenido, { contenido = it }, "Escribe el mensaje...", unaLinea = false, lineasMin = 3)
                    e.errorPublicar?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = Rojo, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    BotonPrincipal(
                        "Publicar",
                        onClick = { vm.publicar(titulo, contenido) { ok -> if (ok) { titulo = ""; contenido = "" } } },
                        habilitado = titulo.isNotBlank() && contenido.isNotBlank(),
                        cargando = e.publicando,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            if (e.anuncios.isEmpty()) item { TextoVacio("Todavía no hay anuncios en el muro.") }
            items(e.anuncios, key = { it.item.id }) { TarjetaPublicacion(it) }
        }
    }
}
