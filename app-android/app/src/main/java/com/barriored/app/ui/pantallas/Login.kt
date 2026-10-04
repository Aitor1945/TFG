package com.barriored.app.ui.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barriored.app.R
import com.barriored.app.data.repo.AuthRepositorio
import com.barriored.app.ui.componentes.mensajeUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoLogin(
    val cargando: Boolean = false,
    val error: String? = null,
    val aviso: String? = null,
)

class LoginViewModel : ViewModel() {
    private val auth = AuthRepositorio()
    private val _estado = MutableStateFlow(EstadoLogin())
    val estado = _estado.asStateFlow()

    fun entrar(correo: String, contrasena: String) {
        if (correo.isBlank() || contrasena.isBlank()) {
            _estado.update { it.copy(error = "Rellena el correo y la contraseña.") }
            return
        }
        viewModelScope.launch {
            _estado.update { EstadoLogin(cargando = true) }
            try {
                auth.iniciarSesion(correo.trim(), contrasena)
                // No hace falta navegar: RaizApp cambia sola al detectar la sesión
            } catch (e: Exception) {
                _estado.update { EstadoLogin(error = e.mensajeUsuario()) }
            }
        }
    }

    fun recuperar(correo: String) {
        if (correo.isBlank()) {
            _estado.update { it.copy(error = "Escribe tu correo para recuperar la contraseña.") }
            return
        }
        viewModelScope.launch {
            _estado.update { EstadoLogin(cargando = true) }
            try {
                auth.recuperarContrasena(correo.trim())
                _estado.update { EstadoLogin(aviso = "Te hemos enviado un correo para restablecer la contraseña.") }
            } catch (e: Exception) {
                _estado.update { EstadoLogin(error = e.mensajeUsuario()) }
            }
        }
    }
}

@Composable
fun LoginPantalla(vm: LoginViewModel = viewModel()) {
    val estado by vm.estado.collectAsState()
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painterResource(R.drawable.logo_barriored),
                contentDescription = "Logo BarrioRed",
                modifier = Modifier.size(120.dp),
            )
            Text("BarrioRed", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "Tu comunidad, conectada",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo electrónico") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )

            estado.error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            estado.aviso?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { vm.entrar(correo, contrasena) },
                enabled = !estado.cargando,
                modifier = Modifier.fillMaxWidth().height(50.dp),
            ) {
                if (estado.cargando) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                else Text("Iniciar sesión")
            }
            TextButton(onClick = { vm.recuperar(correo) }, enabled = !estado.cargando) {
                Text("¿Has olvidado tu contraseña?")
            }
        }
    }
}
