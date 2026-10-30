package com.example.unihogarguaranda

import com.example.unihogarguaranda.util.GuarandaGeofenceValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GuarandaGeofenceValidatorTest {

    @Test
    fun centroDeGuarandaEstaDentro() {
        assertTrue(GuarandaGeofenceValidator.isWithinGuaranda(-1.5921, -79.0035))
    }

    @Test
    fun guanujoYLaguacotoEstanDentro() {
        assertTrue(GuarandaGeofenceValidator.isWithinGuaranda(-1.5562, -79.0012))
        assertTrue(GuarandaGeofenceValidator.isWithinGuaranda(-1.6018, -78.9961))
    }

    @Test
    fun quitoEstaFueraYAMasDe160Km() {
        val km = GuarandaGeofenceValidator.calculateDistanceKm(-0.1807, -78.4678)
        assertTrue(km in 160.0..175.0)
        assertFalse(GuarandaGeofenceValidator.isWithinGuaranda(-0.1807, -78.4678))
    }

    @Test
    fun limiteDe15KmAlSur() {
        // 1 grado de latitud mide unos 111.2 km
        assertTrue(GuarandaGeofenceValidator.isWithinGuaranda(-1.5921 - 0.1259, -79.0035))  // ~14 km
        assertFalse(GuarandaGeofenceValidator.isWithinGuaranda(-1.5921 - 0.1439, -79.0035)) // ~16 km
    }

    @Test
    fun distanciaDeUnPuntoAsiMismoEsCero() {
        assertEquals(0.0, GuarandaGeofenceValidator.calculateDistanceKm(-1.5921, -79.0035), 0.0001)
    }
}
