package com.example.unihogarguaranda

import com.example.unihogarguaranda.data.FiltroCatalogo
import org.junit.Assert.assertEquals
import org.junit.Test

class FiltroCatalogoTest {

    @Test
    fun sinFiltrosNoHayActivos() {
        assertEquals(0, FiltroCatalogo().activos)
    }

    @Test
    fun elTextoDelBuscadorNoCuentaComoFiltro() {
        assertEquals(0, FiltroCatalogo(texto = "centro").activos)
    }

    @Test
    fun cuentaTipoPrecioDistanciaYCadaServicio() {
        val f = FiltroCatalogo(
            tipo = "Casa",
            precioMax = 300.0,
            distanciaMaxKm = 1.0,
            servicios = listOf("WiFi", "Agua")
        )
        assertEquals(5, f.activos)
    }
}
