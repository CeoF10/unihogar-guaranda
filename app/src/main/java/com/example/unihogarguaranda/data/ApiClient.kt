package com.example.unihogarguaranda.data

import com.example.unihogarguaranda.BuildConfig
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente de la API de UniHogar. La URL viene de local.properties (API_BASE_URL).
 */
object ApiClient {

    val service: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
            .create(ApiService::class.java)
    }

    /** Lee el mensaje de error que devuelve la API ({"error": "..."}). */
    fun errorMessage(raw: String?, fallback: String): String =
        raw?.let { runCatching { Gson().fromJson(it, ApiError::class.java).error }.getOrNull() } ?: fallback
}
