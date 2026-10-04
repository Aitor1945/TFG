package com.barriored.app.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.barriored.app.data.model.ConAutor
import com.barriored.app.data.model.EstadoIncidencia
import com.barriored.app.data.model.Incidencia
import com.barriored.app.data.model.Publicacion
import com.barriored.app.ui.theme.Acento
import com.barriored.app.ui.theme.Ambar
import com.barriored.app.ui.theme.BR
import com.barriored.app.ui.theme.Verde

/** Color de cada estado, igual que en la web. */
fun EstadoIncidencia.color(): Color = when (this) {
    EstadoIncidencia.PENDIENTE -> Ambar
    EstadoIncidencia.EN_PROCESO -> Acento
    EstadoIncidencia.RESUELTA -> Verde
}

/** Badge de estado con borde (página Incidencias de la web). */
@Composable
fun EtiquetaEstado(estado: EstadoIncidencia) {
    val c = estado.color()
    Text(
        estado.etiqueta,
        color = c,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(c.copy(alpha = 0.12f))
            .border(1.dp, c.copy(alpha = 0.4f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 5.dp),
    )
}

/** Botón-pastilla con borde de color ("Marcar en proceso", "Eliminar"…). */
@Composable
fun BotonPastilla(texto: String, color: Color, onClick: () -> Unit) {
    Text(
        texto,
        color = color,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(50))
            .clickableSinIndicacion(onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    )
}

/** Pie "👤 Autor ........ fecha" de anuncios e incidencias. */
@Composable
fun PieAutor(autor: String, fecha: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("👤 $autor", color = BR.c.textoSecundario, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(fecha, color = BR.c.textoSecundario, fontSize = 14.sp)
    }
}

/** Anuncio del Muro. */
@Composable
fun TarjetaPublicacion(p: ConAutor<Publicacion>) {
    Tarjeta {
        p.item.titulo?.takeIf { it.isNotBlank() }?.let {
            Text(it, color = BR.c.texto, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
        }
        Text(p.item.contenido, color = BR.c.textoSecundario, fontSize = 15.sp, lineHeight = 22.sp)
        Spacer(Modifier.height(12.dp))
        PieAutor(p.autor, fecha(p.item.creadoEn))
    }
}

/** Incidencia, con hueco para los botones de gestión. */
@Composable
fun TarjetaIncidencia(
    i: ConAutor<Incidencia>,
    acciones: @Composable () -> Unit = {},
) {
    Tarjeta {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                i.item.titulo,
                color = BR.c.texto,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            EtiquetaEstado(EstadoIncidencia.de(i.item.estado))
        }
        i.item.descripcion?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = BR.c.textoSecundario, fontSize = 15.sp, lineHeight = 22.sp)
        }
        acciones()
        Spacer(Modifier.height(12.dp))
        PieAutor(i.autor, fecha(i.item.creadoEn))
    }
}

/** Fila compacta de incidencia del Panel: barra de color + título + estado. */
@Composable
fun FilaIncidencia(i: Incidencia) {
    val estado = EstadoIncidencia.de(i.estado)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BR.c.interior)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(
            Modifier
                .width(3.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(estado.color()),
        ) {}
        Text(i.titulo, color = BR.c.texto, fontSize = 14.sp, modifier = Modifier.weight(1f), maxLines = 1)
        Pastilla(i.estado, estado.color(), tamano = 11.sp)
    }
}
