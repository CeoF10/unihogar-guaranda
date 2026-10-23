package com.example.unihogarguaranda.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unihogarguaranda.model.Property
import com.example.unihogarguaranda.ui.theme.*

@Composable
fun InteractiveMapScreen(
    properties: List<Property>,
    onFilterClick: () -> Unit,
    onPropertySelect: (Property) -> Unit
) {
    var selectedProperty by remember { mutableStateOf(properties.firstOrNull()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Lienzo simulado del plano urbano de Guaranda y campus UEB
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Bloques simulados de calles y manzanas
            drawLine(Color(0xFFE2E8F0), Offset(0f, h * 0.25f), Offset(w, h * 0.25f), strokeWidth = 14f)
            drawLine(Color(0xFFE2E8F0), Offset(0f, h * 0.5f), Offset(w, h * 0.5f), strokeWidth = 16f)
            drawLine(Color(0xFFE2E8F0), Offset(0f, h * 0.75f), Offset(w, h * 0.75f), strokeWidth = 14f)

            drawLine(Color(0xFFE2E8F0), Offset(w * 0.3f, 0f), Offset(w * 0.3f, h), strokeWidth = 14f)
            drawLine(Color(0xFFE2E8F0), Offset(w * 0.7f, 0f), Offset(w * 0.7f, h), strokeWidth = 16f)
            drawLine(Color(0xFFCBD5E1), Offset(w * 0.5f, 0f), Offset(w * 0.5f, h), strokeWidth = 20f) // Av. Principal
        }

        // Campus de la Universidad Estatal de Bolívar
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 2.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 70.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎓", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Universidad Estatal de Bolívar (Campus Matriz)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = BlueUEB
                )
            }
        }

        // Marcadores de casas en el mapa
        val markerOffsets = listOf(
            Alignment.TopStart to Pair(50.dp, 160.dp),
            Alignment.TopEnd to Pair(70.dp, 190.dp),
            Alignment.CenterStart to Pair(60.dp, 0.dp),
            Alignment.Center to Pair(0.dp, (-30).dp),
            Alignment.BottomEnd to Pair(80.dp, 240.dp)
        )

        properties.forEachIndexed { index, prop ->
            val position = markerOffsets.getOrElse(index) { Alignment.Center to Pair(0.dp, 0.dp) }
            val isSelected = selectedProperty?.id == prop.id

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = if (position.first == Alignment.TopStart || position.first == Alignment.CenterStart) position.second.first else 0.dp,
                        end = if (position.first == Alignment.TopEnd || position.first == Alignment.BottomEnd) position.second.first else 0.dp,
                        top = if (position.first == Alignment.TopStart || position.first == Alignment.TopEnd) position.second.second else 0.dp,
                        bottom = if (position.first == Alignment.BottomEnd) position.second.second else 0.dp
                    ),
                contentAlignment = position.first
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { selectedProperty = prop }
                ) {
                    if (isSelected) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BlueUEB,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                "A ${prop.campusDistance}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 44.dp else 36.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) RedUEB else Color.White)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(if (isSelected) RedUEB else RedUEB.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "🏠",
                                fontSize = if (isSelected) 18.sp else 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Botón superior de Filtros
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Guaranda, Bolívar", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BlueUEB)
                }
            }

            IconButton(
                onClick = onFilterClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filtros", tint = BlueUEB)
            }
        }

        // Tarjeta flotante inferior de la propiedad seleccionada (Estilo Airbnb / Showcase)
        selectedProperty?.let { prop ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .clickable { onPropertySelect(prop) }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = prop.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "${prop.beds} hab. · ${prop.baths}",
                                fontSize = 12.sp,
                                color = BlueUEB
                            )
                        }
                        Text(
                            text = "$${prop.price.toInt()} / mes",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = RedUEB
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📍 A ${prop.campusDistance} del campus",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Button(
                            onClick = { onPropertySelect(prop) },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RedUEB)
                        ) {
                            Text("Ver detalles", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
