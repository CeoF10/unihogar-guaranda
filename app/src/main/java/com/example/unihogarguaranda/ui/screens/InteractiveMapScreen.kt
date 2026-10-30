package com.example.unihogarguaranda.ui.screens

import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.unihogarguaranda.R
import com.example.unihogarguaranda.model.Property
import com.example.unihogarguaranda.ui.theme.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

// Centro de Guaranda (Plaza Roja) y campus de la UEB (Alpachaca)
private val GUARANDA_CENTER = GeoPoint(-1.5921, -79.0035)
private val UEB_CAMPUS = GeoPoint(-1.591234, -79.004567)

@Composable
fun InteractiveMapScreen(
    properties: List<Property>,
    onFilterClick: () -> Unit,
    onPropertySelect: (Property) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedProperty by remember { mutableStateOf(properties.firstOrNull()) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                // OpenStreetMap bloquea user-agents genéricos; identificar la app con nombre propio
                Configuration.getInstance().userAgentValue = "UniHogarGuaranda-Android/1.0"
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(14.0)
                    controller.setCenter(GUARANDA_CENTER)
                }
            },
            update = { mapView ->
                mapView.overlays.clear()

                // Marcador del campus UEB
                mapView.overlays.add(Marker(mapView).apply {
                    position = UEB_CAMPUS
                    title = "Universidad Estatal de Bolívar (Campus Matriz)"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_map_campus)
                })

                // Un marcador por cada inmueble; el seleccionado va resaltado
                properties.forEach { prop ->
                    val isSelected = selectedProperty?.id == prop.id
                    mapView.overlays.add(Marker(mapView).apply {
                        position = GeoPoint(prop.lat, prop.lng)
                        title = prop.title
                        snippet = "$${prop.price.toInt()} / mes"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = ContextCompat.getDrawable(
                            context,
                            if (isSelected) R.drawable.ic_map_pin_selected else R.drawable.ic_map_pin
                        )
                        setOnMarkerClickListener { _, _ ->
                            selectedProperty = prop
                            true
                        }
                    })
                }
                mapView.invalidate()
            }
        )

        // Chip superior con la ciudad (en el diseño: "Guaranda, Bolívar")
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 2.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 72.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = BlueUEB,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Universidad Estatal de Bolívar (Campus Matriz)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = BlueUEB
                )
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = RedUEB,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "A ${prop.campusDistance} del campus",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

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
