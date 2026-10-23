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
    val baths: String,
    val services: List<String>,
    val description: String,
    val isFeatured: Boolean = false,
    var isFavorite: Boolean = false,
    val lat: Double = -1.591234,
    val lng: Double = -79.004567,
    val photoCount: Int = 3
)

object MockData {
    val initialProperties = listOf(
        Property(
            id = 1,
            title = "Habitación amoblada",
            type = "Habitación",
            location = "Cerca de la UEB · Guaranda",
            campusDistance = "1.2 km",
            price = 150.0,
            deposit = 100.0,
            beds = 1,
            baths = "Baño compartido",
            services = listOf("WiFi", "Agua", "Luz", "Amoblado"),
            description = "Habitación cómoda y segura, ideal para estudiantes. Cuenta con cama, escritorio, closet y acceso a áreas comunes.",
            isFeatured = true,
            isFavorite = true,
            lat = -1.5921,
            lng = -79.0035
        ),
        Property(
            id = 2,
            title = "Departamento en el centro",
            type = "Departamento",
            location = "Av. Guayaquil y Convención · Guaranda",
            campusDistance = "800 m",
            price = 250.0,
            deposit = 150.0,
            beds = 2,
            baths = "1 baño privado",
            services = listOf("WiFi", "Agua", "Luz", "Lavadora"),
            description = "Departamento espacioso a pocos minutos de paradas de buses hacia la UEB. Excelente iluminación natural y zona comercial segura.",
            isFeatured = false,
            isFavorite = true,
            lat = -1.5905,
            lng = -79.0062
        ),
        Property(
            id = 3,
            title = "Casa compartida",
            type = "Casa",
            location = "Sector Alpachaca · Guaranda",
            campusDistance = "1.5 km",
            price = 200.0,
            deposit = 120.0,
            beds = 3,
            baths = "2 baños",
            services = listOf("WiFi", "Agua", "Luz", "Lavadora", "Amoblado"),
            description = "Ambiente tranquilo y acogedor para grupos de estudiantes. Incluye cocina equipada, patio de lavado y servicios básicos incluidos.",
            isFeatured = false,
            isFavorite = false,
            lat = -1.5938,
            lng = -79.0018
        ),
        Property(
            id = 4,
            title = "Habitación con balcón",
            type = "Habitación",
            location = "Calle Sucre · Centro Histórico",
            campusDistance = "1.1 km",
            price = 180.0,
            deposit = 100.0,
            beds = 1,
            baths = "1 baño privado",
            services = listOf("WiFi", "Agua", "Luz"),
            description = "Hermosa habitación con vista a la ciudad, balcón privado, piso de madera y ambiente de estudio silencioso.",
            isFeatured = false,
            isFavorite = true,
            lat = -1.5942,
            lng = -79.0075
        )
    )
}
