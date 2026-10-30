package com.example.unihogarguaranda.data

import android.content.Context

/**
 * Sesión guardada en el teléfono (SharedPreferences): token JWT y datos del usuario.
 * Así la sesión sobrevive a cerrar la app.
 */
object Session {
    private const val PREFS = "unihogar_session"
    private const val KEY_TOKEN = "token"
    private const val KEY_EMAIL = "email"
    private const val KEY_ROLES = "roles"

    fun save(context: Context, token: String, user: ApiUser) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_EMAIL, user.email)
            .putStringSet(KEY_ROLES, user.roles.toSet())
            .apply()
    }

    fun token(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_TOKEN, null)

    fun bearer(context: Context): String? = token(context)?.let { "Bearer $it" }

    fun email(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_EMAIL, null)

    fun roles(context: Context): Set<String> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getStringSet(KEY_ROLES, emptySet()) ?: emptySet()

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
