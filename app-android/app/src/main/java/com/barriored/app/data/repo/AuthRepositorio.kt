package com.barriored.app.data.repo

import com.barriored.app.data.SupabaseCliente
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow

class AuthRepositorio {
    private val auth get() = SupabaseCliente.cliente.auth

    /** Estado de la sesión: sirve para decidir si mostrar Login o la app (como Private/PublicRoute). */
    val estadoSesion: StateFlow<SessionStatus> get() = auth.sessionStatus

    val idUsuarioActual: String? get() = auth.currentUserOrNull()?.id

    suspend fun iniciarSesion(correo: String, contrasena: String) {
        auth.signInWith(Email) {
            email = correo
            password = contrasena
        }
    }

    suspend fun recuperarContrasena(correo: String) {
        // Usa la "Site URL" configurada en Supabase (la web en Vercel) para el enlace del correo
        auth.resetPasswordForEmail(correo)
    }

    suspend fun cambiarContrasena(nueva: String) {
        auth.updateUser { password = nueva }
    }

    suspend fun cerrarSesion() = auth.signOut()
}
