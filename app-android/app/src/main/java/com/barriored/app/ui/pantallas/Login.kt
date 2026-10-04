package com.barriored.app.ui.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barriored.app.R
import com.barriored.app.data.Preferencias
import com.barriored.app.data.repo.AuthRepositorio
import com.barriored.app.ui.componentes.BotonPrincipal
import com.barriored.app.ui.componentes.CampoTexto
import com.barriored.app.ui.componentes.mensajeUsuario
import com.barriored.app.ui.theme.Acento
import com.barriored.app.ui.theme.AzulBrand
import com.barriored.app.ui.theme.BR
import com.barriored.app.ui.theme.Cian
import com.barriored.app.ui.theme.NegroBrand
import com.barriored.app.ui.theme.Rojo
import com.barriored.app.ui.theme.Verde
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
    val oscuro by Preferencias.modoOscuro.collectAsState()
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var verContrasena by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(BR.c.fondo)) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().navigationBarsPadding(),
        ) {
            // Cabecera de marca (bg-branding en la web: degradado azul → casi negro)
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(AzulBrand, NegroBrand)))
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painterResource(R.drawable.logo_barriored),
                    contentDescription = "Logo BarrioRed",
                    modifier = Modifier.size(110.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text("BARRIORED", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
                Spacer(Modifier.height(6.dp))
                Text("JUNTOS EN CONEXIÓN", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                Spacer(Modifier.height(18.dp))
                HorizontalDivider(Modifier.width(120.dp), color = Color.White.copy(alpha = 0.4f))
                Spacer(Modifier.height(18.dp))
                Text("Plataforma de gestión integral.", color = Color.White.copy(alpha = 0.75f), fontSize = 14.sp)
            }

            // Formulario
            Column(Modifier.padding(24.dp)) {
                Text("Bienvenido", color = BR.c.texto, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Introduce tus credenciales facilitadas por la administración.",
                    color = BR.c.textoSecundario, fontSize = 14.sp,
                )
                Spacer(Modifier.height(24.dp))
                CampoTexto(
                    correo, { correo = it }, "Correo Electrónico",
                    teclado = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                Spacer(Modifier.height(14.dp))
                CampoTexto(
                    contrasena, { contrasena = it }, "Contraseña",
                    transformacion = if (verContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                    teclado = KeyboardOptions(keyboardType = KeyboardType.Password),
                    finalIcono = {
                        IconButton(onClick = { verContrasena = !verContrasena }) {
                            Icon(
                                if (verContrasena) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (verContrasena) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = BR.c.textoSecundario,
                            )
                        }
                    },
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    "¿Olvidaste tu contraseña?",
                    color = if (oscuro) Cian else Acento,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable(enabled = !estado.cargando) { vm.recuperar(correo) },
                )

                estado.error?.let {
                    Spacer(Modifier.height(14.dp))
                    Text(it, color = Rojo, fontSize = 14.sp)
                }
                estado.aviso?.let {
                    Spacer(Modifier.height(14.dp))
                    Text(it, color = Verde, fontSize = 14.sp)
                }

                Spacer(Modifier.height(22.dp))
                BotonPrincipal(
                    "Acceder",
                    onClick = { vm.entrar(correo, contrasena) },
                    cargando = estado.cargando,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Botón de tema (arriba a la derecha, como en la web)
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(14.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(if (oscuro) NegroBrand.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.85f))
                .clickable { Preferencias.cambiarModoOscuro(!oscuro) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (oscuro) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                contentDescription = "Cambiar tema",
                tint = if (oscuro) Color(0xFFE5E7EB) else NegroBrand,
            )
        }
    }
}
