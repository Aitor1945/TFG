package com.barriored.app.data.repo

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.math.roundToInt

data class Tiempo(
    val temperatura: Int,
    val codigo: Int,
    val viento: Int,
    val humedad: Int,
    val lugar: String,
)

/**
 * El tiempo de la tarjeta de Inicio. Usa los mismos servicios gratuitos que la web
 * (Dashboard.jsx): Open-Meteo para el tiempo y Nominatim para el nombre del barrio.
 */
class TiempoRepositorio(private val context: Context) {

    /** Requiere permiso de ubicación (lo pide la pantalla antes de llamar aquí). */
    suspend fun tiempoActual(): Tiempo? {
        val loc = ubicacion() ?: return null
        return withContext(Dispatchers.IO) {
            val tiempo = leerJson(
                "https://api.open-meteo.com/v1/forecast?latitude=${loc.latitude}&longitude=${loc.longitude}" +
                    "&current=temperature_2m,weathercode,windspeed_10m,relative_humidity_2m&timezone=auto",
            )["current"]!!.jsonObject
            val lugar = runCatching {
                val dir = leerJson(
                    "https://nominatim.openstreetmap.org/reverse?lat=${loc.latitude}&lon=${loc.longitude}&format=json",
                )["address"]?.jsonObject
                listOf("neighbourhood", "suburb", "village", "town", "city", "municipality")
                    .firstNotNullOfOrNull { dir?.get(it)?.jsonPrimitive?.contentOrNull }
            }.getOrNull() ?: "Tu ubicación"

            Tiempo(
                temperatura = tiempo["temperature_2m"]!!.jsonPrimitive.double.roundToInt(),
                codigo = tiempo["weathercode"]!!.jsonPrimitive.int,
                viento = tiempo["windspeed_10m"]!!.jsonPrimitive.double.roundToInt(),
                humedad = tiempo["relative_humidity_2m"]!!.jsonPrimitive.double.roundToInt(),
                lugar = lugar,
            )
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun ubicacion(): Location? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val proveedores = lm.getProviders(true)
        proveedores.mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }?.let { return it }

        // Sin última ubicación conocida: pedimos una nueva (Android 11+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && proveedores.isNotEmpty()) {
            val proveedor = if (LocationManager.NETWORK_PROVIDER in proveedores) LocationManager.NETWORK_PROVIDER
            else proveedores.first()
            return withTimeoutOrNull(10_000) {
                suspendCancellableCoroutine { cont ->
                    lm.getCurrentLocation(proveedor, null, context.mainExecutor) { cont.resume(it) }
                }
            }
        }
        return null
    }

    private fun leerJson(url: String): JsonObject {
        val conexion = URL(url).openConnection() as HttpURLConnection
        return try {
            conexion.connectTimeout = 8_000
            conexion.readTimeout = 8_000
            conexion.setRequestProperty("User-Agent", "BarrioRed-App/1.0")
            conexion.setRequestProperty("Accept-Language", "es")
            Json.parseToJsonElement(conexion.inputStream.bufferedReader().readText()).jsonObject
        } finally {
            conexion.disconnect()
        }
    }
}
