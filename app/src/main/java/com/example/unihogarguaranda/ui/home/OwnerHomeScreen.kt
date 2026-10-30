package com.example.unihogarguaranda.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.unihogarguaranda.data.ExpedienteResponse
import com.example.unihogarguaranda.data.InmuebleResponse
import com.example.unihogarguaranda.data.toProperty
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.style.TextOverflow
import com.example.unihogarguaranda.ui.screens.FullProfileScreen
import com.example.unihogarguaranda.ui.screens.MessagesScreen
import com.example.unihogarguaranda.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerHomeScreen(
    ownerEmail: String,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var showRegisterWizard by remember { mutableStateOf(false) }
    var showMisPublicaciones by remember { mutableStateOf(false) }
    var showMiExpediente by remember { mutableStateOf(false) }
    var showMessages by remember { mutableStateOf(false) }
    val vm: OwnerViewModel = viewModel()
    val ui by vm.ui.collectAsState()

    ui.error?.let { mensaje ->
        LaunchedEffect(mensaje) {
            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
            vm.errorMostrado()
        }
    }

    when {
        showRegisterWizard -> {
            com.example.unihogarguaranda.ui.screens.RegisterPropertyWizardScreen(
                onDismiss = { showRegisterWizard = false },
                onFinished = { mensaje ->
                    Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                    showRegisterWizard = false
                    vm.recargar()
                }
            )
            return
        }
        showMisPublicaciones -> {
            MisPublicacionesScreen(onBack = { showMisPublicaciones = false; vm.recargar() })
            return
        }
        showMiExpediente -> {
            MiExpedienteScreen(onBack = { showMiExpediente = false; vm.recargar() })
            return
        }
        showMessages -> {
            MessagesScreen(onBack = { showMessages = false })
            return
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Uni", fontWeight = FontWeight.Black, fontSize = 22.sp, color = BlueUEB)
                        Text("Hogar", fontWeight = FontWeight.Black, fontSize = 22.sp, color = RedUEB)
                        Text(" Propietario", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showRegisterWizard = true },
                    containerColor = RedUEB,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Publicar inmueble", fontWeight = FontWeight.Bold) }
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                listOf(Icons.Default.HomeWork to "Mis anuncios", Icons.Default.Person to "Perfil").forEachIndexed { i, (icono, texto) ->
                    NavigationBarItem(
                        selected = selectedTab == i,
                        onClick = { selectedTab = i },
                        icon = { Icon(icono, contentDescription = texto) },
                        label = { Text(texto, fontSize = 11.sp, fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RedUEB,
                            selectedTextColor = RedUEB,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = ChipBackground
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        if (selectedTab == 1) {
            FullProfileScreen(
                email = ownerEmail,
                roleTitle = "Propietario",
                onLogout = onLogout,
                modifier = Modifier.padding(paddingValues),
                onOptionClick = { option ->
                    when (option) {
                        "Mis publicaciones" -> showMisPublicaciones = true
                        "Mi expediente" -> showMiExpediente = true
                        "Mensajes" -> showMessages = true
                    }
                }
            )
            return@Scaffold
        }

        val anuncios = ui.anuncios

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Resumen
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BlueUEB),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Panel del propietario", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(ownerEmail, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            ResumenNumero("${ui.visibles}", "Visibles")
                            ResumenNumero("${ui.enRevision}", "En revisión")
                            ResumenNumero("${ui.borradores}", "Borradores")
                        }
                    }
                }
            }

            // Estado real del expediente
            item {
                ExpedienteCard(ui.expediente, ui.cargando) { showMiExpediente = true }
            }

            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Tus inmuebles", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                    if (anuncios.isNotEmpty()) {
                        TextButton(onClick = { showMisPublicaciones = true }) { Text("Gestionar", color = RedUEB) }
                    }
                }
            }

            if (!ui.cargando && anuncios.isEmpty()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                        Column(
                            Modifier.fillMaxWidth().padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.HomeWork, null, tint = BlueUEB.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(10.dp))
                            Text("Aún no tienes inmuebles", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(
                                "Publica tu primer inmueble con el botón de abajo. Bienestar UEB lo revisará antes de mostrarlo.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            items(anuncios, key = { it.id }) { anuncio ->
                AnuncioPropioCard(anuncio) { showMisPublicaciones = true }
            }
        }
    }
}

@Composable
private fun ResumenNumero(valor: String, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
        Text(etiqueta, color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
    }
}

@Composable
private fun ExpedienteCard(expediente: ExpedienteResponse?, cargando: Boolean, onClick: () -> Unit) {
    val estado = expediente?.estado
    val (color, titulo, detalle, boton) = when (estado) {
        "APROBADO" -> listOf("verde", "Expediente aprobado", "Bienestar UEB validó su identidad y la propiedad.", "Ver expediente")
        "PENDIENTE_AUDITORIA" -> listOf("ambar", "Expediente en revisión", "Bienestar UEB está revisando sus documentos.", "Ver expediente")
        "OBSERVADO" -> listOf("rojo", "Expediente observado", expediente.dictamen ?: "Corrija los documentos y vuelva a enviarlos.", "Corregir")
        else -> listOf("ambar", "Envíe su expediente", "Cédula, título de la propiedad y una planilla de luz o agua.", "Enviar expediente")
    }
    val acento = when (color) {
        "verde" -> Color(0xFF047857)
        "rojo" -> RedUEB
        else -> Color(0xFFB45309)
    }
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, acento.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(acento.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (estado) {
                        "APROBADO" -> Icons.Default.VerifiedUser
                        "OBSERVADO" -> Icons.Default.ErrorOutline
                        else -> Icons.Default.Description
                    },
                    null,
                    tint = acento
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(if (cargando) "Cargando expediente..." else titulo, fontWeight = FontWeight.Bold, color = TextPrimary)
                if (!cargando) {
                    Text(detalle, fontSize = 12.sp, color = TextSecondary)
                    TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
                        Text(boton, color = acento, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ChevronRight, null, tint = acento, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AnuncioPropioCard(anuncio: InmuebleResponse, onClick: () -> Unit) {
    val (etiqueta, color) = when (anuncio.estado) {
        "DISPONIBLE" -> "Visible" to Color(0xFF047857)
        "EN_AUDITORIA" -> "En revisión" to Color(0xFFB45309)
        "PAUSADO" -> "Pausado" to TextSecondary
        "ARRENDADO" -> "Arrendado" to BlueUEB
        else -> "Borrador" to TextSecondary
    }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(72.dp).clip(RoundedCornerShape(12.dp))) {
                FotoInmueble(anuncio.toProperty(), iconSize = 30)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(anuncio.title, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("$${anuncio.price.toInt()} / mes · ${anuncio.type}", fontSize = 13.sp, color = BlueUEB)
                Text("${anuncio.fotos?.size ?: 0} foto(s)", fontSize = 12.sp, color = TextSecondary)
            }
            Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.12f)) {
                Text(etiqueta, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}
