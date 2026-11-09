package com.example.unihogarguaranda

import com.example.unihogarguaranda.data.ApiClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ServidorUrlTest {
    @Test fun ipSinPuertoUsaHttpYPuerto3000() =
        assertEquals("http://192.168.1.5:3000/api/", ApiClient.normalizar("192.168.1.5"))

    @Test fun ipConPuertoLoRespeta() =
        assertEquals("http://10.16.11.189:8080/api/", ApiClient.normalizar(" 10.16.11.189:8080/ "))

    @Test fun dominioSinEsquemaUsaHttps() =
        assertEquals("https://abc.trycloudflare.com/api/", ApiClient.normalizar("abc.trycloudflare.com"))

    @Test fun urlCompletaSeConserva() =
        assertEquals("https://unihogar.onrender.com/api/", ApiClient.normalizar("https://unihogar.onrender.com/api/"))

    @Test fun vacioEsInvalido() = assertNull(ApiClient.normalizar("   "))
}
