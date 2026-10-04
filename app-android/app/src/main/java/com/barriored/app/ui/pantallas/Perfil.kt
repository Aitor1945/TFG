package com.barriored.app.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barriored.app.data.model.Comunidad
import com.barriored.app.data.model.Perfil
import com.barriored.app.data.repo.AuthRepositorio
import com.barriored.app.data.repo.PerfilRepositorio
import com.barriored.app.ui.componentes.Cargando
import com.barriored.app.ui.componentes.PantallaError
import com.barriored.app.ui.componentes.fecha
import com.barriored.app.ui.componentes.mensajeUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoPerfil(
    val cargando: Boolean = true,
    val error: String? = null,
    val perfil: Perfil? = null,
    val comunidad: Comunidad? = null,
    val numIncidencias: Long = 0,
)

class PerfilViewModel : ViewModel() {
    private val perfiles = PerfilRepositorio()
    private val auth = AuthRepositorio()

    private val _estado = MutableStateFlow(EstadoPerfil())
    val estado = _estado.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            try {
                val p = perfiles.miPerfil()
                _estado.value = EstadoPerfil(
                    cargando = false,
                    perfil = p,
                    comunidad = p.comunidadId?.let { perfiles.comunidad(it) },
                    numIncidencias = perfiles.numIncidenciasCreadas(),
                )
            } catch (e: Exception) {
                _estado.update { it.copy(cargando = false, error = e.mensajeUsuario()) }
            }
        }
    }

    fun guardar(nombre: String, telefono: String, piso: String) {
        viewModelScope.launch {
            try {
                perfiles.actualizarDatos(nombre.trim(), telefono.trim(), piso.trim())
                cargar()
            } catch (e: Exception) {
                _estado.update { it.copy(error = e.mensajeUsuario()) }
            }
        }
    }

    // Al cerrar sesión, RaizApp vuelve sola al Login
    fun cerrarSesion() {
        viewModelScope.launch { runCatching { auth.cerrarSesion() } }
    }
}

@Composable
fun PerfilPantalla(vm: PerfilViewModel = viewModel()) {
    val e by vm.estado.collectAsState()
    var editando by rememberSaveable { mutableStateOf(false) }
    val abrirEnlace = LocalUriHandler.current

    when {
        e.cargando -> Cargando()
        e.error != null -> PantallaError(e.error!!, vm::cargar)
        else -> {
            val p = e.perfil ?: return
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(p.nombreVisible, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    (p.role ?: "vecino").replaceFirstChar { it.uppercase() },
                    color = MaterialTheme.colorScheme.primary,
                )

                Seccion("Mis datos") {
                    Dato("Correo", p.email)
                    Dato("Usuario", p.username)
                    Dato("Teléfono", p.telefono)
                    Dato("Piso", p.piso)
                    Dato("Miembro desde", fecha(p.creadoEn))
                    Dato("Incidencias creadas", e.numIncidencias.toString())
                }

                e.comunidad?.let { c ->
                    Seccion("Mi comunidad") {
                        Dato("Nombre", c.nombre)
                        Dato("Tipo", c.tipo)
                        Dato("Ciudad", c.ciudad)
                        Dato("Vecinos", c.numVecinos?.toString())
                        Dato("Bloques", c.numBloques?.toString())
                        Dato("Zonas comunes", c.numZonasComunes?.toString())
                    }
                    // Sustituye a la página Documentos de la web: abre la carpeta de Drive
                    c.driveUrl?.takeIf { it.isNotBlank() }?.let { url ->
                        OutlinedButton(onClick = { abrirEnlace.openUri(url) }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Folder, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Documentos de la comunidad")
                        }
                    }
                }

                OutlinedButton(onClick = { editando = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Editar mis datos")
                }
                Button(
                    onClick = vm::cerrarSesion,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar sesión")
                }
            }

            if (editando) {
                DialogoEditar(
                    perfil = p,
                    onGuardar = { n, t, pi -> vm.guardar(n, t, pi); editando = false },
                    onCerrar = { editando = false },
                )
            }
        }
    }
}

@Composable
private fun Seccion(titulo: String, contenido: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            contenido()
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String?) {
    if (valor.isNullOrBlank()) return
    Row(Modifier.fillMaxWidth()) {
        Text(etiqueta, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(valor)
    }
}

@Composable
private fun DialogoEditar(perfil: Perfil, onGuardar: (String, String, String) -> Unit, onCerrar: () -> Unit) {
    var nombre by rememberSaveable { mutableStateOf(perfil.nombreCompleto.orEmpty()) }
    var telefono by rememberSaveable { mutableStateOf(perfil.telefono.orEmpty()) }
    var piso by rememberSaveable { mutableStateOf(perfil.piso.orEmpty()) }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Editar mis datos") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre completo") }, singleLine = true)
                OutlinedTextField(telefono, { telefono = it }, label = { Text("Teléfono") }, singleLine = true)
                OutlinedTextField(piso, { piso = it }, label = { Text("Piso") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = { onGuardar(nombre, telefono, piso) }, enabled = nombre.isNotBlank()) {
                Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } },
    )
}
