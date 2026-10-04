package com.barriored.app.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.barriored.app.data.model.ConAutor
import com.barriored.app.data.model.EstadoIncidencia
import com.barriored.app.data.model.Incidencia
import com.barriored.app.data.model.Publicacion
import com.barriored.app.ui.theme.Ambar
import com.barriored.app.ui.theme.AzulClaro
import com.barriored.app.ui.theme.Verde

@Composable
fun TarjetaPublicacion(p: ConAutor<Publicacion>, resumida: Boolean = false) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            p.item.titulo?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
            }
            Text(
                p.item.contenido,
                maxLines = if (resumida) 2 else Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${p.autor} · ${fecha(p.item.creadoEn)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun EtiquetaEstado(estado: EstadoIncidencia) {
    val color: Color = when (estado) {
        EstadoIncidencia.PENDIENTE -> Ambar
        EstadoIncidencia.EN_PROCESO -> AzulClaro
        EstadoIncidencia.RESUELTA -> Verde
    }
    Text(
        estado.etiqueta,
        color = color,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
fun TarjetaIncidencia(
    i: ConAutor<Incidencia>,
    resumida: Boolean = false,
    acciones: @Composable () -> Unit = {},
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    i.item.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                EtiquetaEstado(EstadoIncidencia.de(i.item.estado))
            }
            i.item.descripcion?.takeIf { !resumida && it.isNotBlank() }?.let {
                Spacer(Modifier.height(4.dp))
                Text(it)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${i.autor} · ${fecha(i.item.creadoEn)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            acciones()
        }
    }
}
