package com.example.unihogarguaranda

import com.example.unihogarguaranda.util.GpsLocationProvider
import com.example.unihogarguaranda.util.GpsResult
import org.junit.Assert.assertTrue
import org.junit.Test

class GpsReglaTest {

    @Test
    fun ubicacionPrecisaSeAcepta() {
        val r = GpsLocationProvider.evaluar(-1.5921, -79.0035, accuracy = 8f, simulada = false)
        assertTrue(r is GpsResult.Success)
    }

    @Test
    fun ubicacionFalsaSeRechaza() {
        val r = GpsLocationProvider.evaluar(-1.5921, -79.0035, accuracy = 5f, simulada = true)
        assertTrue(r is GpsResult.Error)
    }

    @Test
    fun masDe50MetrosDeErrorSeRechaza() {
        val r = GpsLocationProvider.evaluar(-1.5921, -79.0035, accuracy = 80f, simulada = false)
        assertTrue(r is GpsResult.Error)
    }

    @Test
    fun exactamente50MetrosSeAcepta() {
        val r = GpsLocationProvider.evaluar(-1.5921, -79.0035, accuracy = 50f, simulada = false)
        assertTrue(r is GpsResult.Success)
    }
}
