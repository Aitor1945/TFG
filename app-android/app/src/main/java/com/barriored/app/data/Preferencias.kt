package com.barriored.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Preferencias guardadas en el móvil (equivale al localStorage "theme" de la web). */
object Preferencias {
    private const val ARCHIVO = "barriored"
    private const val MODO_OSCURO = "modo_oscuro"

    private var prefs: SharedPreferences? = null
    private val _modoOscuro = MutableStateFlow(true)
    val modoOscuro: StateFlow<Boolean> = _modoOscuro.asStateFlow()

    fun iniciar(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).also {
            _modoOscuro.value = it.getBoolean(MODO_OSCURO, true)
        }
    }

    fun cambiarModoOscuro(oscuro: Boolean) {
        _modoOscuro.value = oscuro
        prefs?.edit()?.putBoolean(MODO_OSCURO, oscuro)?.apply()
    }
}
