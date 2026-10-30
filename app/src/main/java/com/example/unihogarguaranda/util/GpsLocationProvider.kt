package com.example.unihogarguaranda.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

/**
 * Resultado de una lectura GPS real del dispositivo.
 */
sealed class GpsResult {
    data class Success(val latitude: Double, val longitude: Double, val accuracyMeters: Float) : GpsResult()
    data class Error(val message: String) : GpsResult()
}

/**
 * Lectura de coordenadas reales con Fused Location Provider (Google Play Services).
 * Rechaza ubicaciones simuladas (apps de "Fake GPS") y lecturas poco precisas.
 */
object GpsLocationProvider {

    // Precisión mínima aceptada: lecturas con más de 50 m de error se descartan
    const val MAX_ACCURACY_METERS = 50f

    /**
     * Requiere que ACCESS_FINE_LOCATION ya esté concedido (se pide desde la UI).
     */
    @SuppressLint("MissingPermission")
    fun requestCurrentLocation(context: Context, onResult: (GpsResult) -> Unit) {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val cancellationToken = CancellationTokenSource()

        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationToken.token)
            .addOnSuccessListener { location: Location? ->
                onResult(evaluate(location))
            }
            .addOnFailureListener { e ->
                onResult(GpsResult.Error("No se pudo obtener la ubicación: ${e.localizedMessage}"))
            }
    }

    private fun evaluate(location: Location?): GpsResult {
        if (location == null) {
            return GpsResult.Error("GPS sin señal. Active la ubicación del dispositivo e intente al aire libre.")
        }
        return evaluar(location.latitude, location.longitude, location.accuracy, isMockLocation(location))
    }

    /** Regla de aceptación separada de la clase Location para poder probarla con JUnit. */
    internal fun evaluar(latitude: Double, longitude: Double, accuracy: Float, simulada: Boolean): GpsResult {
        if (simulada) {
            return GpsResult.Error("Se detectó una ubicación simulada (Fake GPS). Desactive apps de ubicación falsa.")
        }
        if (accuracy > MAX_ACCURACY_METERS) {
            return GpsResult.Error(
                "Precisión insuficiente (±${accuracy.toInt()} m). Se requiere ±${MAX_ACCURACY_METERS.toInt()} m o menos."
            )
        }
        return GpsResult.Success(latitude, longitude, accuracy)
    }

    @Suppress("DEPRECATION")
    private fun isMockLocation(location: Location): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) location.isMock else location.isFromMockProvider
}
