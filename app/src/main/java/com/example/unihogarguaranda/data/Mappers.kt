package com.example.unihogarguaranda.data

import com.example.unihogarguaranda.model.Property

// Convierte la respuesta de la API al modelo que usan las pantallas
fun InmuebleResponse.toProperty(isFavorite: Boolean = false) = Property(
    id = id,
    title = title,
    type = type,
    location = location ?: "",
    campusDistance = campusDistance ?: "",
    price = price,
    deposit = deposit ?: 0.0,
    beds = beds ?: 1,
    baths = "Baño incluido",
    services = services ?: emptyList(),
    description = description ?: "",
    isFavorite = isFavorite,
    lat = lat,
    lng = lng,
    fotoUrls = fotos?.filter { it.tipo != "PANORAMA" }?.map { ApiClient.urlArchivo(it.url) } ?: emptyList(),
    contact = contacto,
    photoCount = fotos?.count { it.tipo != "PANORAMA" } ?: 0,
    costos = costos,
    panoramaUrl = fotos?.firstOrNull { it.tipo == "PANORAMA" }?.let { ApiClient.urlArchivo(it.url) },
    propietario = propietario,
    actualizado = actualizado,
    estado = estado,
    reglas = reglas,
    demostracion = demostracion
)
