package com.example.unihogarguaranda.ui.home

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unihogarguaranda.data.ApiClient
import com.example.unihogarguaranda.data.EditarInmuebleRequest
import com.example.unihogarguaranda.data.InmuebleResponse
import com.example.unihogarguaranda.data.Session
import com.example.unihogarguaranda.ui.theme.*
import com.example.unihogarguaranda.ui.screens.TIPOS_VIVIENDA
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

// RF05: límites que también valida la API
private const val MAX_FOTO_BYTES = 5 * 1024 * 1024
private val TIPOS_FOTO = setOf("image/jpeg", "image/png", "image/webp")

/**
 * RF03 y RF04: el propietario ve solo sus anuncios, los edita, sube fotos y publica.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisPublicacionesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var anuncios by remember { mutableStateOf<List<InmuebleResponse>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var editando by remember { mutableStateOf<InmuebleResponse?>(null) }
    var gestionFotosId by rememberSaveable { mutableStateOf<Long?>(null) }
    var idParaFotos by rememberSaveable { mutableStateOf<Long?>(null) }
    var idParaPanorama by rememberSaveable { mutableStateOf<Long?>(null) }

    suspend fun recargar() {
        val auth = Session.bearer(context) ?: return
        try {
            anuncios = ApiClient.service.misInmuebles(auth).body() ?: emptyList()
        } catch (e: IOException) {
            Toast.makeText(context, "Sin conexión con la API", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        recargar()
        cargando = false
    }

    // Selector de fotos del sistema (no necesita permisos de almacenamiento)
    val selectorFotos = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        val id = idParaFotos ?: return@rememberLauncherForActivityResult
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        scope.launch {
            val partes = prepararFotos(context, uris)
            if (partes.isEmpty()) return@launch
            val auth = Session.bearer(context) ?: return@launch
            try {
                val respuesta = ApiClient.service.subirFotos(auth, id, partes)
                if (respuesta.isSuccessful) {
                    Toast.makeText(context, "Fotos subidas", Toast.LENGTH_SHORT).show()
                    recargar()
                } else {
                    val mensaje = ApiClient.errorMessage(respuesta.errorBody()?.string(), "No se pudieron subir las fotos")
                    Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                Toast.makeText(context, "Sin conexión con la API", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val selectorPanorama = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val id = idParaPanorama ?: return@rememberLauncherForActivityResult
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val auth = Session.bearer(context) ?: return@launch
            try {
                val partes = prepararFotos(context, listOf(uri))
                if (partes.isEmpty()) return@launch
                val respuesta = ApiClient.service.subirFotos(auth, id, partes, tipo = "PANORAMA")
                val mensaje = if (respuesta.isSuccessful) "Panorama guardado"
                    else ApiClient.errorMessage(respuesta.errorBody()?.string(), "No se pudo guardar el panorama")
                Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                if (respuesta.isSuccessful) recargar()
            } catch (_: IOException) {
                Toast.makeText(context, "No se pudo leer o enviar el panorama", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Mis publicaciones", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BlueUEB) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = BlueUEB)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (!cargando && anuncios.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Aún no tiene inmuebles registrados.", color = TextSecondary)
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(anuncios) { anuncio ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(anuncio.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(etiquetaEstado(anuncio.estado), fontSize = 12.sp, color = colorEstado(anuncio.estado), fontWeight = FontWeight.Bold)
                        }
                        Text("$${anuncio.price.toInt()} / mes · ${anuncio.type}", fontSize = 13.sp, color = TextSecondary)
                        val panorama = anuncio.fotos?.firstOrNull { it.tipo == "PANORAMA" }
                        Text("Fotos: ${anuncio.fotos?.count { it.tipo != "PANORAMA" } ?: 0}", fontSize = 12.sp, color = TextSecondary)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { editando = anuncio }) { Text("Editar") }
                            OutlinedButton(onClick = {
                                gestionFotosId = anuncio.id
                            }) { Text("Fotos") }
                        }
                        OutlinedButton(onClick = {
                            idParaPanorama = anuncio.id
                            selectorPanorama.launch("image/jpeg")
                        }) { Text(if (panorama == null) "Subir panorama 360°" else "Cambiar panorama 360°") }
                        if (panorama != null) {
                            TextButton(onClick = {
                                scope.launch {
                                    val auth = Session.bearer(context) ?: return@launch
                                    try {
                                        val respuesta = ApiClient.service.borrarFoto(auth, panorama.id)
                                        if (respuesta.isSuccessful) recargar()
                                        else Toast.makeText(context, "No se pudo quitar el panorama", Toast.LENGTH_SHORT).show()
                                    } catch (_: IOException) {
                                        Toast.makeText(context, "Sin conexión con el servidor", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }) { Text("Quitar panorama") }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (anuncio.estado == "BORRADOR") {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            val auth = Session.bearer(context) ?: return@launch
                                            val r = ApiClient.service.publicar(auth, anuncio.id)
                                            val msg = if (r.isSuccessful) r.body()?.mensaje
                                            else ApiClient.errorMessage(r.errorBody()?.string(), "No se pudo publicar")
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            recargar()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RedUEB)
                                ) { Text("Publicar") }
                            }
                            if (anuncio.ocultoPorAdmin == true) {
                                Text("Oculto por Bienestar UEB")
                            } else if (anuncio.estado == "DISPONIBLE" || anuncio.estado == "PAUSADO") {
                                val pausado = anuncio.estado == "PAUSADO"
                                OutlinedButton(onClick = {
                                    scope.launch {
                                        val auth = Session.bearer(context) ?: return@launch
                                        ApiClient.service.editarInmueble(
                                            auth, anuncio.id,
                                            EditarInmuebleRequest(estado = if (pausado) "DISPONIBLE" else "PAUSADO")
                                        )
                                        recargar()
                                    }
                                }) { Text(if (pausado) "Activar" else "Pausar") }
                            }
                        }
                        if (anuncio.estado == "DISPONIBLE" && anuncio.ocultoPorAdmin != true) {
                            TextButton(onClick = {
                                scope.launch {
                                    val auth = Session.bearer(context) ?: return@launch
                                    try {
                                        val respuesta = ApiClient.service.editarInmueble(auth, anuncio.id, EditarInmuebleRequest(estado = "ARRENDADO"))
                                        if (respuesta.isSuccessful) recargar()
                                        else Toast.makeText(context, ApiClient.errorMessage(respuesta.errorBody()?.string(), "No se pudo marcar como arrendado"), Toast.LENGTH_LONG).show()
                                    } catch (_: IOException) {
                                        Toast.makeText(context, "Sin conexión con el servidor", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }) { Text("Marcar como arrendado") }
                        }
                    }
                }
            }
        }
    }

    anuncios.firstOrNull { it.id == gestionFotosId }?.let { anuncio ->
        val fotos = anuncio.fotos.orEmpty().filter { it.tipo != "PANORAMA" }
        AlertDialog(
            onDismissRequest = { gestionFotosId = null },
            title = { Text("Fotos del anuncio") },
            text = {
                Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                    if (fotos.isEmpty()) Text("Todavía no hay fotos.")
                    fotos.forEachIndexed { indice, foto ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            coil3.compose.AsyncImage(
                                model = ApiClient.urlArchivo(foto.url),
                                contentDescription = "Foto ${indice + 1}",
                                modifier = Modifier.size(56.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            TextButton(onClick = {
                                scope.launch {
                                    val auth = Session.bearer(context) ?: return@launch
                                    try {
                                        val r = ApiClient.service.borrarFoto(auth, foto.id)
                                        if (r.isSuccessful) recargar()
                                        else Toast.makeText(context, ApiClient.errorMessage(r.errorBody()?.string(), "No se pudo quitar la foto"), Toast.LENGTH_LONG).show()
                                    } catch (_: IOException) {
                                        Toast.makeText(context, "Sin conexión con el servidor", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }) { Text("Quitar foto ${indice + 1}") }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { idParaFotos = anuncio.id; selectorFotos.launch("image/*") }) {
                    Text("Agregar fotos")
                }
            },
            dismissButton = { TextButton(onClick = { gestionFotosId = null }) { Text("Cerrar") } }
        )
    }

    editando?.let { anuncio ->
        var precio by rememberSaveable(anuncio.id) { mutableStateOf(anuncio.price.toString()) }
        var titulo by rememberSaveable(anuncio.id) { mutableStateOf(anuncio.title) }
        var tipo by rememberSaveable(anuncio.id) { mutableStateOf(anuncio.type) }
        var direccion by rememberSaveable(anuncio.id) { mutableStateOf(anuncio.location.orEmpty()) }
        var deposito by rememberSaveable(anuncio.id) { mutableStateOf(anuncio.deposit?.toString() ?: "0") }
        var descripcion by rememberSaveable(anuncio.id) { mutableStateOf(anuncio.description.orEmpty()) }
        var contacto by rememberSaveable(anuncio.id) { mutableStateOf(anuncio.contacto.orEmpty()) }
        var reglas by rememberSaveable(anuncio.id) { mutableStateOf(anuncio.reglas.orEmpty()) }
        var dormitorios by rememberSaveable(anuncio.id) { mutableStateOf((anuncio.beds ?: 1).toString()) }
        var servicios by rememberSaveable(anuncio.id) { mutableStateOf(anuncio.services.orEmpty().joinToString(", ")) }
        var guardando by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        val nuevoPrecio = precio.toDoubleOrNull()
        val nuevoDeposito = deposito.toDoubleOrNull()
        val nuevosDormitorios = dormitorios.toIntOrNull()
        AlertDialog(
            onDismissRequest = { if (!guardando) editando = null },
            title = { Text("Editar anuncio") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(titulo, { titulo = it.take(120) }, label = { Text("Título") }, singleLine = true)
                    TIPOS_VIVIENDA.forEach { opcion ->
                        FilterChip(selected = tipo == opcion, onClick = { tipo = opcion }, label = { Text(opcion) })
                    }
                    OutlinedTextField(direccion, { direccion = it.take(200) }, label = { Text("Dirección") })
                    OutlinedTextField(descripcion, { descripcion = it }, label = { Text("Descripción") })
                    OutlinedTextField(precio, { precio = it }, label = { Text("Arriendo mensual") }, singleLine = true)
                    OutlinedTextField(deposito, { deposito = it }, label = { Text("Depósito") }, singleLine = true)
                    OutlinedTextField(dormitorios, { dormitorios = it }, label = { Text("Dormitorios") }, singleLine = true)
                    OutlinedTextField(servicios, { servicios = it }, label = { Text("Servicios separados por comas") })
                    OutlinedTextField(contacto, { contacto = it.take(120) }, label = { Text("Contacto") }, singleLine = true)
                    OutlinedTextField(reglas, { reglas = it.take(1500) }, label = { Text("Reglas de la vivienda") })
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !guardando && titulo.isNotBlank() && nuevoPrecio?.let { it.isFinite() && it >= 0 } == true &&
                        nuevoDeposito?.let { it.isFinite() && it >= 0 } == true && (nuevosDormitorios ?: 0) > 0,
                    onClick = {
                        val auth = Session.bearer(context) ?: return@TextButton
                        val cambios = EditarInmuebleRequest(
                            titulo = titulo.trim(), tipo = tipo, direccion = direccion.trim(), descripcion = descripcion.trim(),
                            precio = nuevoPrecio, deposito = nuevoDeposito, dormitorios = nuevosDormitorios,
                            servicios = servicios.split(',').map { it.trim() }.filter { it.isNotEmpty() },
                            contacto = contacto.trim().ifEmpty { null }, reglas = reglas.trim()
                        )
                        guardando = true
                        scope.launch {
                            try {
                                val r = ApiClient.service.editarInmueble(auth, anuncio.id, cambios)
                                if (r.isSuccessful) {
                                    editando = null
                                    recargar()
                                } else {
                                    error = ApiClient.errorMessage(r.errorBody()?.string(), "No se pudo guardar")
                                }
                            } catch (_: IOException) {
                                error = "Sin conexión con el servidor"
                            } finally {
                                guardando = false
                            }
                        }
                    }
                ) { Text(if (guardando) "Guardando…" else "Guardar") }
            },
            dismissButton = { TextButton(onClick = { editando = null }, enabled = !guardando) { Text("Cancelar") } }
        )
    }
}

// RF05: valida tipo y tamaño en el teléfono antes de enviar (la API vuelve a validar)
internal fun prepararFotos(context: Context, uris: List<Uri>): List<MultipartBody.Part> {
    val partes = mutableListOf<MultipartBody.Part>()
    uris.take(5).forEachIndexed { i, uri ->
        val tipo = context.contentResolver.getType(uri)
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@forEachIndexed
        if (tipo !in TIPOS_FOTO || bytes.size > MAX_FOTO_BYTES) {
            Toast.makeText(context, "Foto ${i + 1}: solo JPEG, PNG o WebP de máximo 5 MB", Toast.LENGTH_LONG).show()
            return@forEachIndexed
        }
        val cuerpo = bytes.toRequestBody(tipo!!.toMediaTypeOrNull())
        partes += MultipartBody.Part.createFormData("fotos", "foto$i", cuerpo)
    }
    return partes
}

private fun etiquetaEstado(estado: String?) = when (estado) {
    "BORRADOR" -> "Borrador"
    "EN_AUDITORIA" -> "En auditoría"
    "DISPONIBLE" -> "Visible"
    "PAUSADO" -> "Pausado"
    "ARRENDADO" -> "Arrendado"
    else -> estado ?: ""
}

private fun colorEstado(estado: String?): Color = when (estado) {
    "DISPONIBLE" -> Color(0xFF047857)
    "EN_AUDITORIA" -> Color(0xFFB45309)
    else -> TextSecondary
}
