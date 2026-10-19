package com.example.unihogarguaranda.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unihogarguaranda.ui.login.UserRole

data class PropertyItem(
    val id: Long,
    val title: String,
    val type: String,
    val price: Double,
    val services: String,
    val distanceCampus: String,
    val campus: String
)

val sampleProperties = listOf(
    PropertyItem(1, "Mini Dpto. Los Chasquis", "Mini Dpto.", 95.0, "Agua, Luz, Wi-Fi", "250 m a pie", "Campus Matriz Alpachaca"),
    PropertyItem(2, "Habitación Cerca de Matriz", "Habitación", 65.0, "Wi-Fi, Baño Privado", "120 m a pie", "Campus Matriz Alpachaca"),
    PropertyItem(3, "Departamento Completo Laguacoto", "Departamento", 160.0, "Agua, Luz, Internet Fibra", "400 m a pie", "Campus Laguacoto")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(
    studentEmail: String,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("UniHogar Guaranda", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(studentEmail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Salir", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Viviendas disponibles cerca de tu Campus",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(sampleProperties) { property ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = property.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "$${property.price}/mes",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "📍 ${property.campus} • ${property.distanceCampus}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Servicios: ${property.services}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { /* Ver Ficha */ },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Ver Ficha")
                            }
                            OutlinedButton(
                                onClick = { /* Solicitar visita */ },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Agendar Visita")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerHomeScreen(
    ownerEmail: String,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Panel del Propietario", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(ownerEmail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Salir", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* Publicar nuevo */ },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+ Publicar Inmueble")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Mis Anuncios Activos", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tienes 1 inmueble publicado y 2 solicitudes de visita pendientes.", fontSize = 14.sp)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mini Dpto. Los Chasquis", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF2E7D32)
                        ) {
                            Text(
                                "DISPONIBLE",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Precio: $95.00/mes • Depósito: $85.00", fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { /* Editar */ }, modifier = Modifier.weight(1f)) {
                            Text("Editar")
                        }
                        OutlinedButton(onClick = { /* Pausar */ }, modifier = Modifier.weight(1f)) {
                            Text("Pausar")
                        }
                    }
                }
            }
        }
    }
}
