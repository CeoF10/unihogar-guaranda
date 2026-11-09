package com.example.unihogarguaranda.data

import com.example.unihogarguaranda.BuildConfig
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente de la API de UniHogar. La URL viene de local.properties (API_BASE_URL).
 */
object ApiClient {

    // El servidor gratis se duerme sin uso y tarda ~50 s en despertar: se espera hasta 75 s
    private val http by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(75, TimeUnit.SECONDS)
            .writeTimeout(75, TimeUnit.SECONDS)
            .callTimeout(90, TimeUnit.SECONDS)
            .build()
    }

    val service: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
            .create(ApiService::class.java)
    }

    /** Lee el mensaje de error que devuelve la API ({"error": "..."}). */
    fun errorMessage(raw: String?, fallback: String): String =
        raw?.let { runCatching { Gson().fromJson(it, ApiError::class.java).error }.getOrNull() } ?: fallback
}
