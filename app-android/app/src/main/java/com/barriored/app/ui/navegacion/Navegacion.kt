package com.barriored.app.ui.navegacion

import android.net.Uri
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.barriored.app.data.repo.AuthRepositorio
import com.barriored.app.ui.componentes.Cargando
import com.barriored.app.ui.pantallas.ConversacionPantalla
import com.barriored.app.ui.pantallas.IncidenciasPantalla
import com.barriored.app.ui.pantallas.InicioPantalla
import com.barriored.app.ui.pantallas.ListaChatsPantalla
import com.barriored.app.ui.pantallas.LoginPantalla
import com.barriored.app.ui.pantallas.MuroPantalla
import com.barriored.app.ui.pantallas.PerfilPantalla
import io.github.jan.supabase.auth.status.SessionStatus

/**
 * Decide qué mostrar según la sesión, igual que Private / PublicRoute en Rutas.jsx:
 * sin sesión -> Login; con sesión -> la app con barra inferior.
 */
@Composable
fun RaizApp() {
    val auth = remember { AuthRepositorio() }
    val estado by auth.estadoSesion.collectAsState()

    when (estado) {
        is SessionStatus.Initializing -> Cargando()
        // RefreshFailure = hay sesión guardada pero no se pudo renovar (p. ej. sin internet): seguimos dentro
        is SessionStatus.Authenticated, is SessionStatus.RefreshFailure -> AppPrincipal()
        is SessionStatus.NotAuthenticated -> LoginPantalla()
    }
}

private enum class Pestana(val ruta: String, val titulo: String, val icono: ImageVector) {
    INICIO("inicio", "Inicio", Icons.Filled.Home),
    MURO("muro", "Muro", Icons.Filled.Campaign),
    INCIDENCIAS("incidencias", "Incidencias", Icons.Filled.ReportProblem),
    CHAT("chat", "Chat", Icons.AutoMirrored.Filled.Chat),
    PERFIL("perfil", "Perfil", Icons.Filled.Person),
}

@Composable
private fun AppPrincipal() {
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route

    fun irA(ruta: String) = nav.navigate(ruta) {
        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    Scaffold(
        bottomBar = {
            // La barra inferior se oculta dentro de una conversación
            if (rutaActual?.startsWith("conversacion") != true) {
                NavigationBar {
                    Pestana.entries.forEach { p ->
                        NavigationBarItem(
                            selected = rutaActual == p.ruta,
                            onClick = { irA(p.ruta) },
                            icon = { Icon(p.icono, contentDescription = p.titulo) },
                            label = { Text(p.titulo) },
                        )
                    }
                }
            }
        },
    ) { relleno ->
        NavHost(
            navController = nav,
            startDestination = Pestana.INICIO.ruta,
            // consumeWindowInsets evita que el teclado (imePadding) sume el margen de la barra del sistema dos veces
            modifier = Modifier.padding(relleno).consumeWindowInsets(relleno),
        ) {
            composable(Pestana.INICIO.ruta) {
                InicioPantalla(
                    onVerMuro = { irA(Pestana.MURO.ruta) },
                    onVerIncidencias = { irA(Pestana.INCIDENCIAS.ruta) },
                )
            }
            composable(Pestana.MURO.ruta) { MuroPantalla() }
            composable(Pestana.INCIDENCIAS.ruta) { IncidenciasPantalla() }
            composable(Pestana.CHAT.ruta) {
                ListaChatsPantalla(onAbrir = { id, nombre ->
                    nav.navigate("conversacion/$id/${Uri.encode(nombre)}")
                })
            }
            composable(
                "conversacion/{id}/{nombre}",
                arguments = listOf(
                    navArgument("id") { type = NavType.StringType },
                    navArgument("nombre") { type = NavType.StringType },
                ),
            ) { entrada ->
                ConversacionPantalla(
                    nombre = entrada.arguments?.getString("nombre").orEmpty(),
                    onVolver = { nav.popBackStack() },
                )
            }
            composable(Pestana.PERFIL.ruta) { PerfilPantalla() }
        }
    }
}
