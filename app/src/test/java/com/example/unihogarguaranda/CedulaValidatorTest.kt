package com.example.unihogarguaranda

import com.example.unihogarguaranda.util.CedulaValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CedulaValidatorTest {

    @Test
    fun cedulaConDigitoVerificadorCorrecto() {
        assertTrue(CedulaValidator.esValida("0201834926"))
        assertTrue(CedulaValidator.esValida("0201948577"))
    }

    @Test
    fun digitoVerificadorIncorrecto() {
        assertFalse(CedulaValidator.esValida("0201834927"))
    }

    @Test
    fun provinciaInexistente() {
        assertFalse(CedulaValidator.esValida("2501834926"))
        assertFalse(CedulaValidator.esValida("0001834926"))
    }

    @Test
    fun tercerDigitoNoEsDePersonaNatural() {
        assertFalse(CedulaValidator.esValida("0261834926"))
    }

    @Test
    fun longitudOLetrasInvalidas() {
        assertFalse(CedulaValidator.esValida("020183492"))
        assertFalse(CedulaValidator.esValida("02018349261"))
        assertFalse(CedulaValidator.esValida("02018A4926"))
        assertFalse(CedulaValidator.esValida(""))
    }
}
