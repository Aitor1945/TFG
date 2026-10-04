package com.barriored.app.ui.asistente

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barriored.app.ui.theme.Acento
import com.barriored.app.ui.theme.BR
import com.barriored.app.ui.theme.Rojo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class MensajeAsistente(val id: Int, val texto: String, val deUsuario: Boolean, val hora: String)

/** Estado del asistente; vive en la Activity, así no se pierde al cambiar de pestaña. */
class AsistenteViewModel : ViewModel() {
    val mensajes = mutableStateListOf<MensajeAsistente>()
    var abierto by mutableStateOf(false)
        private set
    var escribiendo by mutableStateOf(false)
        private set
    var badgeVisible by mutableStateOf(true)
        private set

    private var contador = 0
    private var bienvenidaMostrada = false
    private val formatoHora = DateTimeFormatter.ofPattern("HH:mm")

    fun abrir() {
        abierto = true
        badgeVisible = false
        if (!bienvenidaMostrada) {
            bienvenidaMostrada = true
            viewModelScope.launch {
                delay(400)
                agregar(ContenidoAsistente.BIENVENIDA, deUsuario = false)
            }
        }
    }

    fun cerrar() { abierto = false }

    fun pulsarBoton(b: ContenidoAsistente.BotonRapido) {
        agregar("${b.icono} ${b.texto}", deUsuario = true)
        responder(ContenidoAsistente.respuesta(b.codigo))
    }

    fun enviar(texto: String) {
        val limpio = texto.trim()
        if (limpio.isEmpty()) return
        agregar(limpio, deUsuario = true)
        responder(ContenidoAsistente.buscarRespuesta(limpio))
    }

    // Pequeña espera con "escribiendo…" para que parezca que piensa
    private fun responder(respuesta: String) {
        viewModelScope.launch {
            escribiendo = true
            delay(950)
            escribiendo = false
            agregar(respuesta, deUsuario = false)
        }
    }

    private fun agregar(texto: String, deUsuario: Boolean) {
        mensajes.add(MensajeAsistente(++contador, texto, deUsuario, LocalTime.now().format(formatoHora)))
    }
}

/** Botón flotante azul + panel del asistente (cb-btn-abrir / cb-contenedor en la web). */
@Composable
fun AsistenteFlotante(modifier: Modifier = Modifier, vm: AsistenteViewModel = viewModel()) {
    Column(modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
        AnimatedVisibility(
            visible = vm.abierto,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f),
        ) {
            PanelAsistente(vm)
        }
        Spacer(Modifier.height(12.dp))
        BotonAsistente(abierto = vm.abierto, badge = vm.badgeVisible) {
            if (vm.abierto) vm.cerrar() else vm.abrir()
        }
    }
}

@Composable
private fun BotonAsistente(abierto: Boolean, badge: Boolean, onClick: () -> Unit) {
    Box {
        Box(
            Modifier
                .size(58.dp)
                .shadow(10.dp, CircleShape, ambientColor = Acento, spotColor = Acento)
                .clip(CircleShape)
                .background(Acento)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (abierto) Icons.Filled.Close else Icons.Filled.SupportAgent,
                contentDescription = if (abierto) "Cerrar asistente" else "Abrir asistente",
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }
        if (badge) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Rojo)
                    .border(2.dp, BR.c.fondo, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("1", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PanelAsistente(vm: AsistenteViewModel) {
    var texto by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    val lista = rememberLazyListState()
    val total = vm.mensajes.size + if (vm.escribiendo) 1 else 0

    LaunchedEffect(total) { if (total > 0) lista.animateScrollToItem(total - 1) }

    Column(
        Modifier
            .widthIn(max = 380.dp)
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(BR.c.tarjeta)
            .border(1.dp, BR.c.borde, RoundedCornerShape(16.dp)),
    ) {
        // Cabecera azul
        Row(
            Modifier.fillMaxWidth().background(Acento).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("🤝", fontSize = 26.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Asistente BarrioRed", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Estoy aquí para ayudarte", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
            }
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f))
                    .clickable { vm.cerrar() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        // Mensajes
        LazyColumn(
            state = lista,
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 260.dp).background(BR.c.fondo),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(vm.mensajes, key = { it.id }) { BurbujaAsistente(it) }
            if (vm.escribiendo) item(key = "escribiendo") { Escribiendo() }
        }

        // Botones rápidos
        Column(Modifier.fillMaxWidth().background(BR.c.tarjeta).padding(12.dp)) {
            Text("Elige una opción o escribe:", color = BR.c.textoSecundario, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            ContenidoAsistente.BOTONES.chunked(2).forEach { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    fila.forEach { b ->
                        Text(
                            "${b.icono} ${b.texto}",
                            color = BR.c.texto,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BR.c.interior)
                                .border(1.dp, BR.c.borde, RoundedCornerShape(8.dp))
                                .clickable { vm.pulsarBoton(b) }
                                .padding(vertical = 9.dp, horizontal = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }
        }

        // Caja de texto
        Row(
            Modifier.fillMaxWidth().background(BR.c.tarjeta).padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                placeholder = { Text("Escribe tu pregunta...", color = BR.c.textoSecundario, fontSize = 14.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { vm.enviar(texto); texto = "" }),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BR.c.fondo, unfocusedContainerColor = BR.c.fondo,
                    focusedBorderColor = Acento, unfocusedBorderColor = BR.c.borde,
                    focusedTextColor = BR.c.texto, unfocusedTextColor = BR.c.texto,
                ),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Enviar",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (texto.isBlank()) Acento.copy(alpha = 0.5f) else Acento)
                    .clickable(enabled = texto.isNotBlank()) { vm.enviar(texto); texto = "" }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            )
        }
    }
}

@Composable
private fun BurbujaAsistente(m: MensajeAsistente) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = if (m.deUsuario) Alignment.End else Alignment.Start) {
        Text(
            m.texto,
            color = if (m.deUsuario) Color.White else BR.c.texto,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (m.deUsuario) Acento else BR.c.tarjeta)
                .border(1.dp, if (m.deUsuario) Color.Transparent else BR.c.borde, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
        )
        Text(m.hora, color = BR.c.textoSecundario, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp, start = 4.dp, end = 4.dp))
    }
}

/** Tres puntos animados ("escribiendo…"). */
@Composable
private fun Escribiendo() {
    val t = rememberInfiniteTransition(label = "escribiendo")
    Row(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BR.c.tarjeta)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        repeat(3) { i ->
            val a by t.animateFloat(
                initialValue = 0.3f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(500, delayMillis = i * 180), RepeatMode.Reverse),
                label = "punto$i",
            )
            Box(Modifier.size(7.dp).alpha(a).clip(CircleShape).background(Acento))
        }
    }
}
