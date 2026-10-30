package com.example.unihogarguaranda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unihogarguaranda.data.FiltroCatalogo
import com.example.unihogarguaranda.model.Property
import com.example.unihogarguaranda.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    filtroActual: FiltroCatalogo,
    onDismiss: () -> Unit,
    onApply: (FiltroCatalogo) -> Unit
) {
    // RF06: la hoja abre con los filtros que ya están aplicados
    var priceRange by remember {
        mutableStateOf((filtroActual.precioMin?.toFloat() ?: PRECIO_MIN)..(filtroActual.precioMax?.toFloat() ?: PRECIO_MAX))
    }
    var selectedType by remember { mutableStateOf(filtroActual.tipo) }
    val services = remember { mutableStateListOf<String>().apply { addAll(filtroActual.servicios) } }
    var distancia by remember { mutableStateOf(filtroActual.distanciaMaxKm) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filtros", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlueUEB)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                }
            }

            Column {
                Text("Precio mensual", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(
                    text = "$${priceRange.start.toInt()} - $${priceRange.endInclusive.toInt()}" +
                        if (priceRange.endInclusive >= PRECIO_MAX) " o más" else "",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueUEB
                )
                RangeSlider(
                    value = priceRange,
                    onValueChange = { priceRange = it },
                    valueRange = PRECIO_MIN..PRECIO_MAX,
                    steps = 10,
                    colors = SliderDefaults.colors(thumbColor = RedUEB, activeTrackColor = RedUEB)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tipo de vivienda", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    (listOf<String?>(null) + TIPOS_VIVIENDA).forEach { tipo ->
                        FilterChip(
                            selected = selectedType == tipo,
                            onClick = { selectedType = tipo },
                            label = { Text(tipo ?: "Todos") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RedUEB,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Servicios", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SERVICIOS.forEach { servicio ->
                        val marcado = servicio in services
                        FilterChip(
                            selected = marcado,
                            onClick = { if (marcado) services.remove(servicio) else services.add(servicio) },
                            label = { Text(servicio) },
                            leadingIcon = if (marcado) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Distancia al campus", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DISTANCIAS.forEach { (km, texto) ->
                        FilterChip(
                            selected = distancia == km,
                            onClick = { distancia = km },
                            label = { Text(texto) }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    onApply(
                        filtroActual.copy(
                            precioMin = priceRange.start.toDouble().takeIf { it > PRECIO_MIN },
                            precioMax = priceRange.endInclusive.toDouble().takeIf { it < PRECIO_MAX },
                            tipo = selectedType,
                            servicios = services.toList(),
                            distanciaMaxKm = distancia
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedUEB)
            ) {
                Text("Aplicar filtros", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            TextButton(
                onClick = { onApply(FiltroCatalogo(texto = filtroActual.texto)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Limpiar filtros", color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}

private const val PRECIO_MIN = 50f
private const val PRECIO_MAX = 600f
val TIPOS_VIVIENDA = listOf("Habitación", "Departamento", "Casa")
private val SERVICIOS = listOf("WiFi", "Agua", "Luz", "Lavadora", "Amoblado", "Parqueadero")
private val DISTANCIAS = listOf(null to "Cualquiera", 0.5 to "Hasta 500 m", 1.0 to "Hasta 1 km", 3.0 to "Hasta 3 km")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailScreen(
    property: Property,
    onBack: () -> Unit,
    onToggleFavorite: (Property) -> Unit,
    onContactOwner: (Property) -> Unit
) {
    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = { onContactOwner(property) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedUEB, contentColor = Color.White)
                    ) {
                        Text("Contactar propietario", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Galería con botón atrás y favorito
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Color(0xFFE2E8F0))
            ) {
                // RF05: fotos reales de la API; si no hay, se muestra un marcador
                if (property.fotoUrls.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Hotel, contentDescription = null, tint = BlueUEB, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sin fotos todavía", color = BlueUEB, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                } else {
                    val pagerState = rememberPagerState(pageCount = { property.fotoUrls.size })
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { pagina ->
                        AsyncImage(
                            model = property.fotoUrls[pagina],
                            contentDescription = "Foto ${pagina + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Botón atrás
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = TextPrimary
                    )
                }

                // Botón favorito
                IconButton(
                    onClick = { onToggleFavorite(property) },
                    modifier = Modifier
                        .padding(16.dp)
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = if (property.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = RedUEB
                    )
                }

                // Indicador de puntos carrusel
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(if (property.fotoUrls.isEmpty()) 1 else property.fotoUrls.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == 0) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (index == 0) RedUEB else Color.White.copy(alpha = 0.7f))
                        )
                    }
                }
            }

            // Información detallada
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = property.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$${property.price.toInt()}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = BlueUEB
                        )
                        Text(
                            text = " / mes",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    // RF08: arriendo, cargos recurrentes y depósito por separado
                    property.costos?.let { costos ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Arriendo: $${costos.arriendo.toInt()}", fontSize = 13.sp, color = TextPrimary)
                            costos.cargosRecurrentes.orEmpty().forEach { cargo ->
                                Text(
                                    "${cargo.nombre}: " + (cargo.monto?.let { "$${it.toInt()}" } ?: "por confirmar"),
                                    fontSize = 13.sp,
                                    color = if (cargo.monto == null) RedUEB else TextPrimary
                                )
                            }
                            Text("Depósito (se paga una vez): $${costos.deposito.toInt()}", fontSize = 13.sp, color = TextSecondary)
                            Text(
                                text = costos.totalMensual?.let { "Total mensual: $${it.toInt()}" }
                                    ?: (costos.mensaje ?: "Total pendiente"),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (costos.totalMensual == null) RedUEB else BlueUEB
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = RedUEB,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${property.location} (${property.campusDistance} del campus)",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Chips de especificaciones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { },
                        label = { Text("${property.beds} hab.", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.SingleBed,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = BlueUEB
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    AssistChip(
                        onClick = { },
                        label = { Text(property.baths, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Bathtub,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = BlueUEB
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    AssistChip(
                        onClick = { },
                        label = { Text(property.type, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.HomeWork,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = BlueUEB
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Descripción (se oculta si el propietario no escribió una)
                if (property.description.isNotBlank()) Column {
                    Text(
                        text = "Descripción",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = property.description,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }

                // Servicios incluidos
                Column {
                    Text(
                        text = "Servicios incluidos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    property.services.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { service ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 4.dp),
                                    border = CardDefaults.outlinedCardBorder()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = RedUEB,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(service, fontSize = 13.sp, color = TextPrimary)
                                    }
                                }
                            }
                            if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
