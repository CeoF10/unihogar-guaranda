package com.example.unihogarguaranda.util

/**
 * Validación de cédula ecuatoriana con el algoritmo módulo 10 del Registro Civil.
 * La API hace la misma validación en el servidor (geo.js).
 */
object CedulaValidator {

    fun esValida(cedula: String): Boolean {
        if (cedula.length != 10 || cedula.any { it !in '0'..'9' }) return false
        val provincia = cedula.substring(0, 2).toInt()
        if (provincia < 1 || provincia > 24) return false
        if (cedula[2].digitToInt() >= 6) return false

        val coeficientes = intArrayOf(2, 1, 2, 1, 2, 1, 2, 1, 2)
        var suma = 0
        for (i in 0 until 9) {
            var valor = cedula[i].digitToInt() * coeficientes[i]
            if (valor >= 10) valor -= 9
            suma += valor
        }
        val digitoVerificador = if (suma % 10 == 0) 0 else 10 - (suma % 10)
        return digitoVerificador == cedula[9].digitToInt()
    }

    private val PROVINCIAS = listOf(
        "Azuay", "Bolívar", "Cañar", "Carchi", "Cotopaxi", "Chimborazo", "El Oro", "Esmeraldas",
        "Guayas", "Imbabura", "Loja", "Los Ríos", "Manabí", "Morona Santiago", "Napo", "Pastaza",
        "Pichincha", "Tungurahua", "Zamora Chinchipe", "Galápagos", "Sucumbíos", "Orellana",
        "Santo Domingo de los Tsáchilas", "Santa Elena"
    )

    /** Provincia donde se emitió la cédula (los dos primeros dígitos), o null si no es válida. */
    fun provincia(cedula: String): String? =
        if (esValida(cedula)) PROVINCIAS.getOrNull(cedula.substring(0, 2).toInt() - 1) else null
}
