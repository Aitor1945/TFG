package com.barriored.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Modelos que reflejan las tablas de Supabase que ya usa la web.
// Los nombres en @SerialName son los de las columnas.

@Serializable
data class Perfil(
    val id: String,
    val email: String? = null,
    val role: String? = null,
    @SerialName("comunidad_id") val comunidadId: String? = null,
    val username: String? = null,
    @SerialName("full_name") val nombreCompleto: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val telefono: String? = null,
    val piso: String? = null,
    @SerialName("created_at") val creadoEn: String? = null,
) {
    val nombreVisible: String get() = nombreCompleto ?: username ?: email ?: "Vecino"
    val puedeGestionar: Boolean get() = role in ROLES_GESTION
}

/** Mismos roles que ROLES_GESTION / rolesPermitidos en la web. */
val ROLES_GESTION = setOf("ayuntamiento", "presidente", "admin")

@Serializable
data class Comunidad(
    val id: String? = null,
    val nombre: String? = null,
    val tipo: String? = null,
    val ciudad: String? = null,
    @SerialName("num_vecinos") val numVecinos: Int? = null,
    @SerialName("num_bloques") val numBloques: Int? = null,
    @SerialName("num_zonas_comunes") val numZonasComunes: Int? = null,
    @SerialName("drive_url") val driveUrl: String? = null,
)

@Serializable
data class Publicacion(
    val id: String,
    val titulo: String? = null, // opcional en la base de datos (tipo "texto")
    val contenido: String,
    @SerialName("autor_id") val autorId: String? = null,
    val tipo: String? = null,
    @SerialName("created_at") val creadoEn: String? = null,
)

@Serializable
data class NuevaPublicacion(
    val titulo: String,
    val contenido: String,
    @SerialName("autor_id") val autorId: String,
    @SerialName("comunidad_id") val comunidadId: String,
    val tipo: String = "anuncio",
)

@Serializable
data class Incidencia(
    val id: String,
    val titulo: String,
    val descripcion: String? = null,
    val estado: String = EstadoIncidencia.PENDIENTE.valor,
    @SerialName("autor_id") val autorId: String? = null,
    @SerialName("created_at") val creadoEn: String? = null,
)

@Serializable
data class NuevaIncidencia(
    val titulo: String,
    val descripcion: String,
    @SerialName("autor_id") val autorId: String,
    @SerialName("comunidad_id") val comunidadId: String,
    val estado: String = EstadoIncidencia.PENDIENTE.valor,
)

enum class EstadoIncidencia(val valor: String, val etiqueta: String) {
    PENDIENTE("pendiente", "Pendiente"),
    EN_PROCESO("en_proceso", "En proceso"),
    RESUELTA("resuelta", "Resuelta");

    companion object {
        fun de(valor: String) = entries.firstOrNull { it.valor == valor } ?: PENDIENTE
    }
}

@Serializable
data class Mensaje(
    val id: String,
    @SerialName("sender_id") val emisorId: String,
    @SerialName("receiver_id") val receptorId: String,
    val content: String? = null,
    val read: Boolean? = null,
    @SerialName("created_at") val creadoEn: String? = null,
)

@Serializable
data class NuevoMensaje(
    @SerialName("sender_id") val emisorId: String,
    @SerialName("receiver_id") val receptorId: String,
    val content: String,
)

/** Publicación o incidencia junto al nombre de su autor, ya lista para pintar. */
data class ConAutor<T>(val item: T, val autor: String)
