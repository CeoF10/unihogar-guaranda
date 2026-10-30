package com.example.unihogarguaranda.data

import android.content.Context
import retrofit2.Response
import java.io.IOException

/** Error con un mensaje listo para mostrar al usuario. */
class ApiException(message: String) : Exception(message)

/**
 * Único punto de acceso a la API para las pantallas.
 * Convierte las respuestas de Retrofit en Result, con el mensaje de error que envía la API.
 */
class UniHogarRepository(private val context: Context) {

    private val api = ApiClient.service
    private fun auth(): String? = Session.bearer(context)

    suspend fun catalogo(filtro: FiltroCatalogo, pagina: Int): Result<InmueblesPage> = llamar {
        api.getInmuebles(
            tipo = filtro.tipo,
            precioMin = filtro.precioMin,
            precioMax = filtro.precioMax,
            servicios = filtro.servicios.takeIf { it.isNotEmpty() }?.joinToString(","),
            distanciaMaxKm = filtro.distanciaMaxKm,
            q = filtro.texto,
            page = pagina
        )
    }

    // Todos los favoritos del usuario, sin importar los filtros del catálogo
    suspend fun favoritos(): List<InmuebleResponse> {
        val a = auth() ?: return emptyList()
        return llamar { api.getFavoritos(a) }.getOrNull() ?: emptyList()
    }

    suspend fun setFavorito(inmuebleId: Long, favorito: Boolean): Result<Unit> {
        val a = auth() ?: return Result.failure(ApiException(SIN_SESION))
        return llamarSinCuerpo {
            if (favorito) api.addFavorito(a, inmuebleId) else api.removeFavorito(a, inmuebleId)
        }
    }

    suspend fun misInmuebles(): Result<List<InmuebleResponse>> {
        val a = auth() ?: return Result.failure(ApiException(SIN_SESION))
        return llamar { api.misInmuebles(a) }
    }

    suspend fun expediente(): Result<ExpedienteResponse> {
        val a = auth() ?: return Result.failure(ApiException(SIN_SESION))
        return llamar { api.getExpediente(a) }
    }

    private suspend fun <T> llamar(peticion: suspend () -> Response<T>): Result<T> = try {
        val r = peticion()
        val cuerpo = r.body()
        if (r.isSuccessful && cuerpo != null) Result.success(cuerpo)
        else Result.failure(ApiException(ApiClient.errorMessage(r.errorBody()?.string(), "Error ${r.code()} del servidor")))
    } catch (e: IOException) {
        Result.failure(ApiException(SIN_CONEXION))
    }

    // Para respuestas 204 (sin cuerpo), como marcar o quitar un favorito
    private suspend fun llamarSinCuerpo(peticion: suspend () -> Response<*>): Result<Unit> = try {
        val r = peticion()
        if (r.isSuccessful) Result.success(Unit)
        else Result.failure(ApiException(ApiClient.errorMessage(r.errorBody()?.string(), "Error ${r.code()} del servidor")))
    } catch (e: IOException) {
        Result.failure(ApiException(SIN_CONEXION))
    }

    companion object {
        const val SIN_CONEXION = "No hay conexión con el servidor UniHogar"
        const val SIN_SESION = "Su sesión terminó. Vuelva a iniciar sesión."
    }
}
