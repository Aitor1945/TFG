package com.barriored.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.barriored.app.data.Preferencias
import com.barriored.app.ui.navegacion.RaizApp
import com.barriored.app.ui.theme.BarrioRedTema

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Preferencias.iniciar(this)
        enableEdgeToEdge()
        setContent {
            val oscuro by Preferencias.modoOscuro.collectAsState()
            // Iconos de la barra de estado claros u oscuros según el tema de la app
            DisposableEffect(oscuro) {
                val estilo = if (oscuro) SystemBarStyle.dark(Color.TRANSPARENT)
                else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = estilo, navigationBarStyle = estilo)
                onDispose { }
            }
            BarrioRedTema(oscuro) {
                RaizApp()
            }
        }
    }
}
