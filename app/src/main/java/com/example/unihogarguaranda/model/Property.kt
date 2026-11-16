package com.example.unihogarguaranda.model

data class Property(
    val id: Long,
    val title: String,
    val type: String, // "Habitación", "Departamento", "Casa"
    val location: String,
    val campusDistance: String,
    val price: Double,
    val deposit: Double,
    val beds: Int,
    val services: List<String>,
    val description: String,
    var isFavorite: Boolean = false,
    // Campus Matriz (Alpachaca), igual que CAMPUS_MATRIZ en unihogar-api/src/geo.js
    val lat: Double = -1.5696874,
    val lng: Double = -79.0036103,
    val fotoUrls: List<String> = emptyList(),
    val contact: String? = null,
    val costos: com.example.unihogarguaranda.data.CostosResponse? = null,
    val panoramaUrl: String? = null,
    val propietario: String? = null,
    val actualizado: String? = null,
    val estado: String? = null,
    val reglas: String? = null,
    val demostracion: Boolean = false,
    val modelo3d: Modelo3D? = null
)
