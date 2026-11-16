package com.example.unihogarguaranda.model

/**
 * RF14: modelo 3D ilustrativo del inmueble. Los valores coinciden con unihogar-api/src/modelo3d.js
 * y con assets/room-3d.js, que acomoda los muebles en la habitación.
 */
data class Modelo3D(
    val ancho: Double = 3.5,
    val largo: Double = 3.5,
    val piso: String = "BALDOSA_CLARA",
    val pared: String = "CREMA",
    val ropa: String = "AZUL",
    val ventana: String = "CENTRO",
    val muebles: List<String> = listOf("PUERTA", "CAMA_SIMPLE", "ESCRITORIO", "ARMARIO")
) {
    /** Devuelve el primer problema del modelo o null si la API lo aceptará. */
    fun error(): String? = when {
        !ancho.isFinite() || ancho !in MEDIDA_MIN..MEDIDA_MAX -> "El ancho debe estar entre 2 y 8 metros"
        !largo.isFinite() || largo !in MEDIDA_MIN..MEDIDA_MAX -> "El largo debe estar entre 2 y 8 metros"
        piso !in PISOS -> "Elija el piso"
        pared !in PAREDES -> "Elija el color de pared"
        ropa !in ROPAS -> "Elija el color de la ropa de cama"
        ventana !in VENTANAS -> "Elija la posición de la ventana"
        muebles.any { it !in MUEBLES } || muebles.toSet().size != muebles.size -> "Revise los muebles"
        "CAMA_SIMPLE" in muebles && "CAMA_DOBLE" in muebles -> "Elija cama simple o cama doble"
        else -> null
    }

    companion object {
        const val MEDIDA_MIN = 2.0
        const val MEDIDA_MAX = 8.0
        val PISOS = linkedMapOf("MADERA" to "Madera", "BALDOSA_CLARA" to "Baldosa clara", "BALDOSA_TERRACOTA" to "Baldosa terracota")
        val PAREDES = linkedMapOf("CREMA" to "Crema", "BLANCO" to "Blanco", "CELESTE" to "Celeste", "VERDE_SUAVE" to "Verde suave")
        val ROPAS = linkedMapOf("AZUL" to "Azul", "VERDE" to "Verde", "GRIS" to "Gris", "TERRACOTA" to "Terracota")
        val VENTANAS = linkedMapOf("IZQUIERDA" to "Izquierda", "CENTRO" to "Centro", "DERECHA" to "Derecha", "NINGUNA" to "Sin ventana")
        val MUEBLES = linkedMapOf(
            "PUERTA" to "Puerta", "CAMA_SIMPLE" to "Cama simple", "CAMA_DOBLE" to "Cama doble",
            "VELADOR" to "Velador", "ARMARIO" to "Armario", "ESCRITORIO" to "Escritorio", "REPISA" to "Repisa",
            "COCINETA" to "Cocineta", "REFRIGERADORA" to "Refrigeradora", "COMEDOR" to "Comedor",
            "ALFOMBRA" to "Alfombra", "PLANTA" to "Planta"
        )
    }
}
