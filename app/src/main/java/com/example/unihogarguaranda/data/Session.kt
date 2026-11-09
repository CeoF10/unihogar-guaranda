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
    private const val KEY_NAME = "nombre"
    private const val KEY_ROLES = "roles"
    private const val KEY_ACTIVE_ROLE = "active_role"

    fun save(context: Context, token: String, user: ApiUser) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_NAME, user.nombre)
            .putStringSet(KEY_ROLES, user.roles.toSet())
            .apply()
    }

    fun token(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_TOKEN, null)

    fun bearer(context: Context): String? = token(context)?.let { "Bearer $it" }

    fun email(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_EMAIL, null)

    fun nombre(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_NAME, null)

    fun roles(context: Context): Set<String> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getStringSet(KEY_ROLES, emptySet()) ?: emptySet()

    /** Rol con el que se inició sesión (un usuario puede tener ambos roles). */
    fun saveActiveRole(context: Context, role: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_ACTIVE_ROLE, role).apply()
    }

    fun activeRole(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ACTIVE_ROLE, null)

    /** true si hay un token guardado que todavía no vence (se lee el campo exp del JWT). */
    fun hasValidToken(context: Context): Boolean {
        val token = token(context) ?: return false
        return try {
            val payload = token.split(".")[1]
            val json = String(android.util.Base64.decode(payload, android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP))
            val exp = org.json.JSONObject(json).optLong("exp", 0L)
            exp == 0L || exp * 1000 > System.currentTimeMillis()
        } catch (e: Exception) {
            false
        }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
