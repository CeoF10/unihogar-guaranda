package com.example.unihogarguaranda

import com.example.unihogarguaranda.util.CedulaValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CedulaValidatorTest {

    @Test
    fun cedulaConDigitoVerificadorCorrecto() {
        assertTrue(CedulaValidator.esValida("0201111119"))
        assertTrue(CedulaValidator.esValida("0202222220"))
    }

    @Test
    fun digitoVerificadorIncorrecto() {
        assertFalse(CedulaValidator.esValida("0201111117"))
    }

    @Test
    fun provinciaInexistente() {
        assertFalse(CedulaValidator.esValida("2501111119"))
        assertFalse(CedulaValidator.esValida("0001111119"))
    }

    @Test
    fun tercerDigitoNoEsDePersonaNatural() {
        assertFalse(CedulaValidator.esValida("0261111119"))
    }

    @Test
    fun longitudOLetrasInvalidas() {
        assertFalse(CedulaValidator.esValida("020111111"))
        assertFalse(CedulaValidator.esValida("02011111191"))
        assertFalse(CedulaValidator.esValida("02018A4926"))
        assertFalse(CedulaValidator.esValida(""))
    }

    @Test fun provinciaDeEmision() {
        org.junit.Assert.assertEquals("Bolívar", CedulaValidator.provincia("0201234564"))
        org.junit.Assert.assertNull(CedulaValidator.provincia("0201234565"))
    }
}
