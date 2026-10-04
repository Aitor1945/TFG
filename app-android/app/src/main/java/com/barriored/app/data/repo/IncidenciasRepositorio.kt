package com.barriored.app.data.repo

import com.barriored.app.data.SupabaseCliente
import com.barriored.app.data.model.ConAutor
import com.barriored.app.data.model.EstadoIncidencia
import com.barriored.app.data.model.Incidencia
import com.barriored.app.data.model.NuevaIncidencia
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class IncidenciasRepositorio(private val perfiles: PerfilRepositorio = PerfilRepositorio()) {
    private val db get() = SupabaseCliente.cliente

    suspend fun incidencias(limite: Long? = null): List<ConAutor<Incidencia>> {
        val lista = db.from("incidencias").select {
            order("created_at", Order.DESCENDING)
            limite?.let { limit(it) }
        }.decodeList<Incidencia>()

        val nombres = perfiles.nombres(lista.mapNotNull { it.autorId }.toSet())
        return lista.map { ConAutor(it, nombres[it.autorId] ?: "Vecino") }
    }

    suspend fun crear(titulo: String, descripcion: String, autorId: String, comunidadId: String) {
        db.from("incidencias").insert(NuevaIncidencia(titulo, descripcion, autorId, comunidadId))
    }

    suspend fun cambiarEstado(id: String, estado: EstadoIncidencia) {
        db.from("incidencias").update({ set("estado", estado.valor) }) {
            filter { eq("id", id) }
        }
    }

    suspend fun borrar(id: String) {
        db.from("incidencias").delete { filter { eq("id", id) } }
    }
}
