package com.example.unihogarguaranda.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import coil3.compose.AsyncImage
import androidx.compose.ui.text.style.TextOverflow
import com.example.unihogarguaranda.model.Property
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import com.example.unihogarguaranda.ui.theme.*

// Foto principal del inmueble o un marcador si todavía no tiene fotos
@Composable
fun FotoInmueble(property: Property, iconSize: Int = 42) {
    if (property.fotoUrls.isNotEmpty()) {
        AsyncImage(
            model = property.fotoUrls.first(),
            contentDescription = property.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFFE8EEF7), Color(0xFFD5DFEC)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (property.type) {
                    "Habitación" -> Icons.Default.Bed
                    "Departamento" -> Icons.Default.Apartment
                    else -> Icons.Default.Home
                },
                contentDescription = null,
                tint = BlueUEB.copy(alpha = 0.45f),
                modifier = Modifier.size(iconSize.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PropertyCard(property: Property, onClick: () -> Unit, onToggleFavorite: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                FotoInmueble(property)

                // Etiquetas sobre la foto: tipo y verificación
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(shape = RoundedCornerShape(8.dp), color = BlueUEB) {
                        Text(
                            property.type,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF047857)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Verificado UEB", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.95f))
                ) {
                    Icon(
                        imageVector = if (property.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (property.isFavorite) "Quitar de favoritos" else "Guardar en favoritos",
                        tint = RedUEB,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (property.fotoUrls.size > 1) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                    ) {
                        Text(
                            "${property.fotoUrls.size} fotos",
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = property.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = RedUEB, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = listOf(property.location, property.campusDistance.takeIf { it.isNotEmpty() }?.let { "$it del campus" })
                            .filterNotNull()
                            .filter { it.isNotEmpty() }
                            .joinToString(" · "),
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    (listOf("${property.beds} hab.") + property.services.take(3)).forEach { etiqueta ->
                        Surface(shape = RoundedCornerShape(8.dp), color = ChipBackground) {
                            Text(
                                etiqueta,
                                fontSize = 11.sp,
                                color = BlueUEB,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (property.services.size > 3) {
                        Text("+${property.services.size - 3}", fontSize = 11.sp, color = TextSecondary,
                            modifier = Modifier.padding(vertical = 4.dp))
                    }
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$${property.price.toInt()}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = BlueUEB
                    )
                    Text(" / mes", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 3.dp))
                    Spacer(modifier = Modifier.weight(1f))
                    property.costos?.let { costos ->
                        Text(
                            text = costos.totalMensual?.let { "Total $${it.toInt()}" } ?: "Total por confirmar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (costos.totalMensual == null) RedUEB else TextSecondary,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

// RF12 básico: abre WhatsApp con un mensaje sobre el anuncio; si no hay WhatsApp, abre el marcador
fun contactarPropietario(context: Context, property: Property) {
    val telefono = property.contact?.filter { it.isDigit() }
    if (telefono.isNullOrEmpty()) {
        Toast.makeText(context, "Este anuncio no tiene un contacto registrado", Toast.LENGTH_SHORT).show()
        return
    }
    // Números de Ecuador: 09XXXXXXXX -> 5939XXXXXXXX
    val internacional = if (telefono.startsWith("0")) "593" + telefono.drop(1) else telefono
    val mensaje = Uri.encode("Hola, vi su anuncio \"${property.title}\" en UniHogar Guaranda. ¿Sigue disponible?")
    val whatsapp = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$internacional?text=$mensaje"))
    try {
        context.startActivity(whatsapp)
    } catch (e: android.content.ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telefono")))
    }
}
