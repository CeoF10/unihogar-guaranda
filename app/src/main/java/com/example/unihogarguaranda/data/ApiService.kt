package com.example.unihogarguaranda.data

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // RF01 / RF02: cuentas y sesión
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<MensajeResponse>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("auth/recuperar")
    suspend fun recuperarClave(@Body body: EmailRequest): Response<MensajeResponse>

    // RF06 / RF07: catálogo con filtros y paginación
    @GET("inmuebles")
    suspend fun getInmuebles(
        @Query("tipo") tipo: String? = null,
        @Query("precioMin") precioMin: Double? = null,
        @Query("precioMax") precioMax: Double? = null,
        @Query("servicios") servicios: String? = null,
        @Query("distanciaMaxKm") distanciaMaxKm: Double? = null,
        @Query("q") q: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<InmueblesPage>

    // RF08: ficha con costos y fotos
    @GET("inmuebles/{id}")
    suspend fun getInmueble(@Path("id") id: Long): Response<InmuebleResponse>

    // RF03 / RF04: propietario
    @POST("inmuebles")
    suspend fun crearInmueble(
        @Header("Authorization") auth: String,
        @Body body: NuevoInmuebleRequest
    ): Response<NuevoInmuebleResponse>

    @GET("mis-inmuebles")
    suspend fun misInmuebles(@Header("Authorization") auth: String): Response<List<InmuebleResponse>>

    @PATCH("inmuebles/{id}")
    suspend fun editarInmueble(
        @Header("Authorization") auth: String,
        @Path("id") id: Long,
        @Body body: EditarInmuebleRequest
    ): Response<MensajeResponse>

    @POST("inmuebles/{id}/publicar")
    suspend fun publicar(@Header("Authorization") auth: String, @Path("id") id: Long): Response<MensajeResponse>

    // RF05: fotos
    @Multipart
    @POST("inmuebles/{id}/fotos")
    suspend fun subirFotos(
        @Header("Authorization") auth: String,
        @Path("id") id: Long,
        @Part fotos: List<MultipartBody.Part>
    ): Response<FotosResponse>

    @DELETE("fotos/{fotoId}")
    suspend fun borrarFoto(@Header("Authorization") auth: String, @Path("fotoId") fotoId: Long): Response<Unit>

    // RF01 / RF03: expediente del propietario
    @GET("expediente")
    suspend fun getExpediente(@Header("Authorization") auth: String): Response<ExpedienteResponse>

    @Multipart
    @POST("expediente")
    suspend fun enviarExpediente(
        @Header("Authorization") auth: String,
        @Part partes: List<MultipartBody.Part>
    ): Response<MensajeResponse>

    // RF09: favoritos
    @GET("favoritos")
    suspend fun getFavoritos(@Header("Authorization") auth: String): Response<List<InmuebleResponse>>

    @PUT("favoritos/{id}")
    suspend fun addFavorito(@Header("Authorization") auth: String, @Path("id") id: Long): Response<Unit>

    @DELETE("favoritos/{id}")
    suspend fun removeFavorito(@Header("Authorization") auth: String, @Path("id") id: Long): Response<Unit>
}
