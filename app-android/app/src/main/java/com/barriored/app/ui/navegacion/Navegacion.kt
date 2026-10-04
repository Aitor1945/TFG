package com.barriored.app.ui.navegacion

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import com.barriored.app.ui.asistente.AsistenteFlotante
import com.barriored.app.ui.theme.Acento
import com.barriored.app.ui.theme.BR
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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
        is SessionStatus.Initializing -> Box(Modifier.fillMaxSize().background(BR.c.fondo)) { Cargando() }
        // RefreshFailure = hay sesión guardada pero no se pudo renovar (p. ej. sin internet): seguimos dentro
        is SessionStatus.Authenticated, is SessionStatus.RefreshFailure -> AppPrincipal()
        is SessionStatus.NotAuthenticated -> LoginPantalla()
    }
}

private enum class Pestana(val ruta: String, val titulo: String, val icono: ImageVector) {
    INICIO("inicio", "Inicio", Icons.Filled.ShowChart),
    MURO("muro", "Muro", Icons.Filled.Newspaper),
    INCIDENCIAS("incidencias", "Incidencias", Icons.Filled.Warning),
    CHAT("chat", "Chat", Icons.Filled.Forum),
    PERFIL("perfil", "Perfil", Icons.Filled.Person),
}

@Composable
private fun AppPrincipal() {
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route
    val enConversacion = rutaActual?.startsWith("conversacion") == true
    // Como en la web, el asistente no aparece en el chat para no tapar la caja de texto
    val mostrarAsistente = rutaActual != Pestana.CHAT.ruta && !enConversacion

    fun irA(ruta: String) = nav.navigate(ruta) {
        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    Scaffold(
        containerColor = BR.c.fondo,
        bottomBar = {
            // La barra inferior se oculta dentro de una conversación
            if (!enConversacion) {
                NavigationBar(containerColor = BR.c.tarjeta, tonalElevation = 0.dp) {
                    Pestana.entries.forEach { p ->
                        NavigationBarItem(
                            selected = rutaActual == p.ruta,
                            onClick = { irA(p.ruta) },
                            icon = { Icon(p.icono, contentDescription = p.titulo) },
                            label = { Text(p.titulo, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Acento,
                                selectedTextColor = Acento,
                                indicatorColor = Acento.copy(alpha = 0.18f),
                                unselectedIconColor = BR.c.textoSecundario,
                                unselectedTextColor = BR.c.textoSecundario,
                            ),
                        )
                    }
                }
            }
        },
    ) { relleno ->
        // consumeWindowInsets evita que el teclado (imePadding) sume el margen de la barra del sistema dos veces
        Box(Modifier.fillMaxSize().padding(relleno).consumeWindowInsets(relleno)) {
            NavHost(
                navController = nav,
                startDestination = Pestana.INICIO.ruta,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable(Pestana.INICIO.ruta) { InicioPantalla(onIr = { irA(it) }) }
                composable(Pestana.MURO.ruta) { MuroPantalla() }
                composable(Pestana.INCIDENCIAS.ruta) { IncidenciasPantalla() }
                composable(Pestana.CHAT.ruta) {
                    ListaChatsPantalla(onAbrir = { id, nombre, rol ->
                        nav.navigate("conversacion/$id/${Uri.encode(nombre)}?rol=${Uri.encode(rol)}")
                    })
                }
                composable(
                    "conversacion/{id}/{nombre}?rol={rol}",
                    arguments = listOf(
                        navArgument("id") { type = NavType.StringType },
                        navArgument("nombre") { type = NavType.StringType },
                        navArgument("rol") { type = NavType.StringType; defaultValue = "" },
                    ),
                ) { entrada ->
                    ConversacionPantalla(
                        nombre = entrada.arguments?.getString("nombre").orEmpty(),
                        onVolver = { nav.popBackStack() },
                    )
                }
                composable(Pestana.PERFIL.ruta) { PerfilPantalla() }
            }

            if (mostrarAsistente) {
                AsistenteFlotante(Modifier.align(Alignment.BottomEnd).imePadding())
            }
        }
    }
}
