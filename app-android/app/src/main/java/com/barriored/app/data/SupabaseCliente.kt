package com.barriored.app.data

import com.barriored.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

/**
 * Cliente único de Supabase para toda la app (equivale a src/lib/supabase.js de la web).
 * Usa el mismo proyecto, así que los usuarios y los datos son los mismos que en la web.
 */
object SupabaseCliente {
    val cliente: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            // isLenient permite leer ids numéricos o uuid como String indistintamente
            defaultSerializer = KotlinXSerializer(Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            })
            install(Auth)      // La sesión se guarda sola en el móvil
            install(Postgrest) // Consultas a tablas
            install(Realtime)  // Chat en tiempo real
        }
    }
}
