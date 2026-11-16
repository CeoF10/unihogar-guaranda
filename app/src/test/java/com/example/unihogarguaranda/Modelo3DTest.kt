package com.example.unihogarguaranda

import com.example.unihogarguaranda.model.Modelo3D
import com.example.unihogarguaranda.ui.home.leerMetros
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class Modelo3DTest {
    @Test
    fun modeloPorDefectoEsValido() = assertNull(Modelo3D().error())

    @Test
    fun rechazaMedidasFueraDeRango() {
        assertEquals("El ancho debe estar entre 2 y 8 metros", Modelo3D(ancho = 1.5).error())
        assertEquals("El largo debe estar entre 2 y 8 metros", Modelo3D(largo = 9.0).error())
        assertEquals("El ancho debe estar entre 2 y 8 metros", Modelo3D(ancho = Double.NaN).error())
    }

    @Test
    fun rechazaDosCamasYMueblesDesconocidos() {
        assertEquals("Elija cama simple o cama doble", Modelo3D(muebles = listOf("CAMA_SIMPLE", "CAMA_DOBLE")).error())
        assertEquals("Revise los muebles", Modelo3D(muebles = listOf("SOFA")).error())
        assertEquals("Revise los muebles", Modelo3D(muebles = listOf("PUERTA", "PUERTA")).error())
    }

    @Test
    fun rechazaOpcionesDesconocidas() {
        assertEquals("Elija el piso", Modelo3D(piso = "MARMOL").error())
        assertEquals("Elija la posición de la ventana", Modelo3D(ventana = "ARRIBA").error())
    }

    @Test
    fun leeMetrosConComaOPunto() {
        assertEquals(3.5, leerMetros(" 3,5 ")!!, 0.0)
        assertEquals(4.25, leerMetros("4.25")!!, 0.0)
        assertNull(leerMetros("tres"))
    }
}
