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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    var idParaFotos by remember { mutableStateOf<Long?>(null) }

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
                        Text("Fotos: ${anuncio.fotos?.size ?: 0}", fontSize = 12.sp, color = TextSecondary)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { editando = anuncio }) { Text("Editar") }
                            OutlinedButton(onClick = {
                                idParaFotos = anuncio.id
                                selectorFotos.launch("image/*")
                            }) { Text("Fotos") }
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
                            if (anuncio.estado == "DISPONIBLE" || anuncio.estado == "PAUSADO") {
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
                    }
                }
            }
        }
    }

    editando?.let { anuncio ->
        var precio by remember(anuncio.id) { mutableStateOf(anuncio.price.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { editando = null },
            title = { Text("Editar precio") },
            text = {
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Canon mensual ($)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val nuevo = precio.toDoubleOrNull()
                    if (nuevo == null || nuevo <= 0) {
                        Toast.makeText(context, "El precio debe ser mayor a 0", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    scope.launch {
                        val auth = Session.bearer(context) ?: return@launch
                        val r = ApiClient.service.editarInmueble(auth, anuncio.id, EditarInmuebleRequest(precio = nuevo))
                        val msg = if (r.isSuccessful) "Anuncio actualizado"
                        else ApiClient.errorMessage(r.errorBody()?.string(), "No se pudo guardar")
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        editando = null
                        recargar()
                    }
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { editando = null }) { Text("Cancelar") } }
        )
    }
}

// RF05: valida tipo y tamaño en el teléfono antes de enviar (la API vuelve a validar)
private fun prepararFotos(context: Context, uris: List<Uri>): List<MultipartBody.Part> {
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
