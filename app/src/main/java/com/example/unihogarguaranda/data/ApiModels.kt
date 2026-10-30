package com.example.unihogarguaranda.data

// Modelos JSON de unihogar-api (ver unihogar-api/src/routes.js)

data class LoginRequest(val email: String, val password: String)

data class LoginResponse(val token: String, val user: ApiUser)

data class ApiUser(val id: Long, val nombre: String, val email: String, val roles: List<String>)

data class ApiError(val error: String)

data class InmuebleResponse(
    val id: Long,
    val title: String,
    val type: String,
    val location: String?,
    val campusDistance: String?,
    val price: Double,
    val deposit: Double?,
    val beds: Int?,
    val services: List<String>?,
    val description: String?,
    val lat: Double,
    val lng: Double,
    val estado: String?,
    val verificado: Boolean? = null,
    val costos: CostosResponse? = null,
    val fotos: List<FotoResponse>? = null
)

data class NuevoInmuebleRequest(
    val titulo: String,
    val tipo: String,
    val direccion: String?,
    val descripcion: String?,
    val precio: Double,
    val deposito: Double,
    val dormitorios: Int,
    val servicios: List<String>,
    val lat: Double,
    val lng: Double,
    val precisionGpsM: Double?,
    val origen: String = "GPS",
    val cargos: List<CargoResponse> = emptyList()
)

data class NuevoInmuebleResponse(val id: Long, val estado: String)

data class MensajeResponse(val mensaje: String?)

data class RegisterRequest(
    val nombre: String,
    val email: String,
    val password: String,
    val rol: String,
    val telefono: String?,
    val cedula: String?
)

data class EmailRequest(val email: String)

data class CargoResponse(val nombre: String, val monto: Double?)

data class CostosResponse(
    val arriendo: Double,
    val deposito: Double,
    val cargosRecurrentes: List<CargoResponse>?,
    val totalMensual: Double?,
    val mensaje: String?
)

data class FotoResponse(val id: Long, val orden: Int, val url: String)

data class InmueblesPage(
    val items: List<InmuebleResponse>,
    val total: Int,
    val page: Int,
    val sinResultados: Boolean,
    val mensaje: String?
)

data class FotosResponse(val fotos: List<FotoResponse>)

// Solo los campos que se envían cambian; los null no viajan (Gson los omite)
data class EditarInmuebleRequest(
    val precio: Double? = null,
    val deposito: Double? = null,
    val estado: String? = null,
    val descripcion: String? = null,
    val contacto: String? = null
)

// Filtros del catálogo (RF06). Un campo null no se envía.
data class FiltroCatalogo(
    val precioMin: Double? = null,
    val precioMax: Double? = null,
    val tipo: String? = null,
    val servicios: List<String> = emptyList()
)

data class ExpedienteResponse(
    val estado: String,
    val claveCatastral: String?,
    val tipoDocumento: String?,
    val dictamen: String?
)
