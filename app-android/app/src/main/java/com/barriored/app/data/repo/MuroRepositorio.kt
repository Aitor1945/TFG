package com.barriored.app.data.repo

import com.barriored.app.data.SupabaseCliente
import com.barriored.app.data.model.ConAutor
import com.barriored.app.data.model.NuevaPublicacion
import com.barriored.app.data.model.Publicacion
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class MuroRepositorio(private val perfiles: PerfilRepositorio = PerfilRepositorio()) {
    private val db get() = SupabaseCliente.cliente

    suspend fun publicaciones(limite: Long? = null): List<ConAutor<Publicacion>> {
        val lista = db.from("muro_publicaciones").select {
            order("created_at", Order.DESCENDING)
            limite?.let { limit(it) }
        }.decodeList<Publicacion>()

        val nombres = perfiles.nombres(lista.mapNotNull { it.autorId }.toSet())
        return lista.map { ConAutor(it, nombres[it.autorId] ?: "Vecino") }
    }

    suspend fun publicar(titulo: String, contenido: String, autorId: String, comunidadId: String) {
        db.from("muro_publicaciones").insert(
            NuevaPublicacion(titulo, contenido, autorId, comunidadId)
        )
    }
}
