package com.example.unihogarguaranda.util

import kotlin.math.*

/**
 * Utilidad de Geocercado (Geofencing) para el Cantón Guaranda.
 * Implementa el cálculo de distancia ortodrómica basado en la fórmula de Haversine.
 * Garantiza que solo se puedan registrar propiedades y arrendadores ubicados
 * físicamente dentro de la jurisdicción de Guaranda y campus UEB.
 */
object GuarandaGeofenceValidator {

    // Coordenadas centrales de Guaranda (Plaza Roja / Campus Matriz Alpachaca UEB)
    const val GUARANDA_LATITUDE = -1.5921
    const val GUARANDA_LONGITUDE = -79.0035

    // Radio de cobertura máximo permitido (15 km cubre casco urbano, Alpachaca, Laguacoto, Guanujo)
    const val MAX_RADIUS_KM = 15.0

    // Radio de la Tierra en kilómetros
    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Calcula la distancia en kilómetros entre dos coordenadas usando la fórmula de Haversine.
     */
    fun calculateDistanceKm(
        lat1: Double,
        lon1: Double,
        lat2: Double = GUARANDA_LATITUDE,
        lon2: Double = GUARANDA_LONGITUDE
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)

        val a = sin(dLat / 2).pow(2.0) +
                cos(rLat1) * cos(rLat2) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }

    /**
     * Valida si las coordenadas están estrictamente dentro de la jurisdicción de Guaranda.
     */
    fun isWithinGuaranda(latitude: Double, longitude: Double): Boolean {
        val distance = calculateDistanceKm(latitude, longitude)
        return distance <= MAX_RADIUS_KM
    }
}
