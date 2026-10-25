package com.example.unihogarguaranda.model

/**
 * Respuesta del servicio de verificación de identidad con el Registro Civil del Ecuador.
 */
data class RegistroCivilResponse(
    val cedula: String,
    val nombresCompletos: String,
    val condicionCedulado: String = "CIUDADANO ACTIVO",
    val fechaNacimiento: String = "1988-05-14",
    val provincia: String = "BOLÍVAR",
    val canton: String = "GUARANDA",
    val estadoCivil: String = "SOLTERO",
    val esValido: Boolean = true
)

/**
 * Servicio simulador de integración con los Web Services del Registro Civil del Ecuador / DIGERCIC.
 * En producción se conecta al servicio SOAP/REST gubernamental según Ley Orgánica de Datos Personales.
 */
object RegistroCivilService {

    /**
     * Consulta en tiempo real los datos demográficos y vigencia de la cédula.
     */
    fun consultarCedula(cedula: String): RegistroCivilResponse? {
        if (cedula.length != 10) return null

        // Nombres precargados para demostración académica
        val nombreAsignado = when (cedula) {
            "0201834926" -> "NEYCER ROBERTO JIMENEZ FLORES"
            "0201948577" -> "ELIAS SEBASTIAN VELASTEGUI"
            else -> "PROPIETARIO REGISTRADO BOLIVAR"
        }

        return RegistroCivilResponse(
            cedula = cedula,
            nombresCompletos = nombreAsignado,
            condicionCedulado = "CIUDADANO ACTIVO CON DERECHOS POLÍTICOS",
            provincia = "BOLÍVAR",
            canton = "GUARANDA",
            esValido = true
        )
    }
}
