package com.barriored.app.data.repo

import com.barriored.app.data.SupabaseCliente
import com.barriored.app.data.model.Mensaje
import com.barriored.app.data.model.NuevoMensaje
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class ChatRepositorio {
    private val db get() = SupabaseCliente.cliente

    /** Conversación entre dos usuarios, de más antiguo a más nuevo (igual que en la web). */
    suspend fun conversacion(yo: String, otro: String): List<Mensaje> =
        db.from("messages").select {
            filter {
                or {
                    and { eq("sender_id", yo); eq("receiver_id", otro) }
                    and { eq("sender_id", otro); eq("receiver_id", yo) }
                }
            }
            order("created_at", Order.ASCENDING)
        }.decodeList()

    /** Nº de mensajes sin leer por cada vecino que me ha escrito. */
    suspend fun noLeidos(yo: String): Map<String, Int> =
        db.from("messages").select(Columns.list("id", "sender_id", "receiver_id", "read")) {
            filter {
                eq("receiver_id", yo)
                eq("read", false)
            }
        }.decodeList<Mensaje>().groupingBy { it.emisorId }.eachCount()

    suspend fun enviar(yo: String, otro: String, texto: String): Mensaje =
        db.from("messages").insert(NuevoMensaje(yo, otro, texto)) { select() }.decodeSingle()

    suspend fun marcarLeidos(yo: String, otro: String) {
        db.from("messages").update({ set("read", true) }) {
            filter {
                eq("sender_id", otro)
                eq("receiver_id", yo)
            }
        }
    }

    /**
     * Canal en tiempo real con los mensajes nuevos de una conversación
     * (equivale a supabase.channel(...).on("postgres_changes", ...) de Chat.jsx).
     * Hay que llamar a [suscribir] después de empezar a recoger el Flow.
     */
    fun canal(yo: String, otro: String): Pair<RealtimeChannel, Flow<Mensaje>> {
        val canal = db.channel("chat-$yo-$otro")
        val nuevos = canal.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "messages"
        }
            .map { it.decodeRecord<Mensaje>() }
            .filter {
                (it.emisorId == yo && it.receptorId == otro) ||
                    (it.emisorId == otro && it.receptorId == yo)
            }
        return canal to nuevos
    }

    suspend fun suscribir(canal: RealtimeChannel) = canal.subscribe()

    suspend fun cerrar(canal: RealtimeChannel) = db.realtime.removeChannel(canal)
}
