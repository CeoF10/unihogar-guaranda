package com.example.unihogarguaranda.data

import android.content.Context
import com.example.unihogarguaranda.BuildConfig
import com.google.gson.Gson
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente de la API de UniHogar.
 * La dirección del servidor se puede cambiar desde la app (pantalla de inicio de sesión → Servidor),
 * así funciona por cable (adb reverse), por Wi-Fi con la IP de la PC o con un túnel HTTPS.
 * Si nunca se cambió, se usa la de compilación (API_BASE_URL en local.properties).
 */
object ApiClient {
    private const val PREFS = "unihogar_servidor"
    private const val KEY_URL = "api_base_url"

    private lateinit var appContext: Context

    // El servidor puede tardar en responder (equipo dormido, red lenta): se espera hasta 75 s
    private val http by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(75, TimeUnit.SECONDS)
            .writeTimeout(75, TimeUnit.SECONDS)
            .callTimeout(90, TimeUnit.SECONDS)
            .build()
    }

    private var urlActual: String? = null
    private var servicioActual: ApiService? = null

    fun configurar(context: Context) {
        appContext = context.applicationContext
    }

    /** URL base en uso, siempre terminada en "/api/". */
    val baseUrl: String
        get() = (if (::appContext.isInitialized)
            appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_URL, null)
        else null) ?: BuildConfig.API_BASE_URL

    val urlPorDefecto: String get() = BuildConfig.API_BASE_URL

    /**
     * Normaliza lo que escribe el usuario: "192.168.1.5" → "http://192.168.1.5:3000/api/",
     * "https://xyz.trycloudflare.com" → "https://xyz.trycloudflare.com/api/". Devuelve null si no es válida.
     */
    fun normalizar(entrada: String): String? {
        var t = entrada.trim().trimEnd('/')
        if (t.isEmpty()) return null
        if (!t.startsWith("http://") && !t.startsWith("https://")) {
            val host = t.substringBefore('/')
            val esLocal = Regex("^([0-9.]+|localhost)(:\\d+)?$").matches(host)
            // IP o localhost → API local por http (puerto 3000 si no se indica); dominio → https (túnel)
            t = if (esLocal) "http://" + (if (host.contains(':')) t else t.replaceFirst(host, "$host:3000")) else "https://$t"
        }
        if (!t.endsWith("/api")) t += "/api"
        val url = "$t/"
        return url.takeIf { it.toHttpUrlOrNull() != null }
    }

    fun guardarServidor(url: String?) {
        appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply {
            if (url == null) remove(KEY_URL) else putString(KEY_URL, url)
        }.apply()
    }

    val service: ApiService
        @Synchronized get() {
            val url = baseUrl
            servicioActual?.takeIf { url == urlActual }?.let { return it }
            return crear(url).also { servicioActual = it; urlActual = url }
        }

    /** Servicio para una URL concreta (sirve para probar un servidor antes de guardarlo). */
    fun crear(url: String): ApiService =
        Retrofit.Builder()
            .baseUrl(url)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
            .create(ApiService::class.java)

    /**
     * Las fotos llegan con la dirección que conoce el servidor (p. ej. http://localhost:3000/api/...).
     * Se reescriben con el servidor configurado para que se vean desde el teléfono.
     */
    fun urlArchivo(url: String): String {
        val i = url.indexOf("/api/")
        return if (i >= 0) baseUrl + url.substring(i + 5) else url
    }

    /** Lee el mensaje de error que devuelve la API ({"error": "..."}). */
    fun errorMessage(raw: String?, fallback: String): String =
        raw?.let { runCatching { Gson().fromJson(it, ApiError::class.java).error }.getOrNull() } ?: fallback
}
