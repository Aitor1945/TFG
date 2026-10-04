package com.barriored.app.data.repo

import com.barriored.app.data.SupabaseCliente
import com.barriored.app.data.model.Actividad
import com.barriored.app.data.model.Comunidad
import com.barriored.app.data.model.Perfil
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class PerfilRepositorio {
    private val db get() = SupabaseCliente.cliente

    private fun idActual(): String =
        db.auth.currentUserOrNull()?.id ?: error("No hay sesión iniciada")

    suspend fun miPerfil(): Perfil =
        db.from("profiles").select {
            filter { eq("id", idActual()) }
        }.decodeSingle()

    suspend fun comunidad(id: String): Comunidad? =
        db.from("comunidades").select {
            filter { eq("id", id) }
        }.decodeSingleOrNull()

    /** Resto de vecinos con los que se puede chatear (la RLS de Supabase decide cuáles se ven). */
    suspend fun vecinos(): List<Perfil> =
        db.from("profiles")
            .select(Columns.list("id", "full_name", "username", "email", "role")) {
                filter { neq("id", idActual()) }
            }
            .decodeList()

    /** Devuelve un mapa id -> nombre para poner el autor en publicaciones e incidencias. */
    suspend fun nombres(ids: Collection<String>): Map<String, String> {
        if (ids.isEmpty()) return emptyMap()
        return db.from("profiles")
            .select(Columns.list("id", "full_name", "username", "email")) {
                filter { isIn("id", ids.toList()) }
            }
            .decodeList<Perfil>()
            .associate { it.id to it.nombreVisible }
    }

    suspend fun numIncidenciasCreadas(): Long =
        db.from("incidencias").select {
            count(Count.EXACT)
            head = true
            filter { eq("autor_id", idActual()) }
        }.countOrNull() ?: 0

    /** "Actividad reciente" de Mi perfil (la rellena un trigger de la base de datos). */
    suspend fun actividadReciente(): List<Actividad> =
        db.from("actividad_usuario").select(Columns.list("tipo", "titulo", "descripcion", "created_at")) {
            filter { eq("user_id", idActual()) }
            order("created_at", Order.DESCENDING)
            limit(4)
        }.decodeList()

    suspend fun actualizarDatos(nombre: String, telefono: String, piso: String) {
        db.from("profiles").update(DatosPerfil(nombre, telefono, piso)) {
            filter { eq("id", idActual()) }
        }
    }

    @Serializable
    private data class DatosPerfil(
        @SerialName("full_name") val nombre: String,
        val telefono: String,
        val piso: String,
    )
}
