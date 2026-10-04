package com.barriored.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.barriored.app.ui.navegacion.RaizApp
import com.barriored.app.ui.theme.BarrioRedTema

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BarrioRedTema {
                RaizApp()
            }
        }
    }
}
