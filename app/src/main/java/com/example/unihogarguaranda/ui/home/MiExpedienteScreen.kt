package com.example.unihogarguaranda.ui.home

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.unihogarguaranda.data.Session
import com.example.unihogarguaranda.ui.theme.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

// Mismos tipos de documento que acepta la API
private val TIPOS_DOCUMENTO = listOf(
    "IMPUESTO_PREDIAL" to "Impuesto predial",
    "ESCRITURA_PUBLICA" to "Escritura pública",
    "CERTIFICADO_REGISTRO" to "Certificado de registro",
    "PODER_ESPECIAL" to "Poder especial"
)
private const val MAX_BYTES = 5 * 1024 * 1024
private val MIMES = arrayOf("application/pdf", "image/jpeg", "image/png", "image/webp")

/**
 * RF01 / RF03: el propietario sube su expediente (cédula, título de propiedad y planilla)
 * para que Bienestar UEB lo audite.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiExpedienteScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var estado by remember { mutableStateOf<String?>(null) }
    var dictamen by remember { mutableStateOf<String?>(null) }
    var cedulaRuc by remember { mutableStateOf("") }
    var claveCatastral by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(TIPOS_DOCUMENTO.first().first) }
    var declarado by remember { mutableStateOf(false) }
    var enviando by remember { mutableStateOf(false) }
    var archivoCedula by remember { mutableStateOf<Uri?>(null) }
    var archivoTitulo by remember { mutableStateOf<Uri?>(null) }
    var archivoPlanilla by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        val auth = Session.bearer(context) ?: return@LaunchedEffect
        try {
            val r = ApiClient.service.getExpediente(auth).body()
            estado = r?.estado
            dictamen = r?.dictamen
            claveCatastral = r?.claveCatastral ?: ""
            tipo = r?.tipoDocumento ?: tipo
        } catch (e: IOException) {
            Toast.makeText(context, "Sin conexión con la API", Toast.LENGTH_SHORT).show()
        }
    }

    val selectorCedula = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { archivoCedula = it }
    val selectorTitulo = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { archivoTitulo = it }
    val selectorPlanilla = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { archivoPlanilla = it }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Mi expediente", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BlueUEB) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = BlueUEB)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            estado?.let {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Estado: ${etiqueta(it)}", fontWeight = FontWeight.Bold, color = BlueUEB)
                        dictamen?.let { d -> Text("Dictamen: $d", fontSize = 13.sp, color = TextSecondary) }
                    }
                }
            }

            OutlinedTextField(
                value = cedulaRuc,
                onValueChange = { cedulaRuc = it.filter(Char::isDigit).take(10) },
                label = { Text("Cédula de identidad") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = claveCatastral,
                onValueChange = { claveCatastral = it },
                label = { Text("Clave catastral (GAD Guaranda)") },
                placeholder = { Text("ej. 02-01-05-12-004-001") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Tipo de documento de propiedad", fontWeight = FontWeight.SemiBold, color = TextPrimary)
            TIPOS_DOCUMENTO.forEach { (valor, texto) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = tipo == valor, onClick = { tipo = valor })
                    Text(texto)
                }
            }

            Text("Documentos (PDF, JPEG, PNG o WebP, máximo 5 MB)", fontWeight = FontWeight.SemiBold, color = TextPrimary)
            ArchivoBoton("Cédula de identidad", archivoCedula != null) { selectorCedula.launch(MIMES) }
            ArchivoBoton("Título o escritura de la propiedad", archivoTitulo != null) { selectorTitulo.launch(MIMES) }
            ArchivoBoton("Planilla de luz o agua", archivoPlanilla != null) { selectorPlanilla.launch(MIMES) }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = declarado, onCheckedChange = { declarado = it })
                Text("Declaro bajo juramento que soy el titular de la propiedad.", fontSize = 13.sp)
            }

            Button(
                onClick = {
                    val auth = Session.bearer(context) ?: return@Button
                    val validacion = validar(context, cedulaRuc, claveCatastral, declarado, archivoCedula, archivoTitulo, archivoPlanilla)
                    if (validacion != null) {
                        Toast.makeText(context, validacion, Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    enviando = true
                    scope.launch {
                        try {
                            val partes = mutableListOf<MultipartBody.Part>()
                            partes += archivoParte(context, "cedula", archivoCedula!!)
                            partes += archivoParte(context, "titulo", archivoTitulo!!)
                            partes += archivoParte(context, "planilla", archivoPlanilla!!)
                            partes += MultipartBody.Part.createFormData("cedula_ruc", cedulaRuc)
                            partes += MultipartBody.Part.createFormData("clave_catastral", claveCatastral)
                            partes += MultipartBody.Part.createFormData("tipo_documento", tipo)
                            partes += MultipartBody.Part.createFormData("declaracion", "true")
                            val r = ApiClient.service.enviarExpediente(auth, partes)
                            if (r.isSuccessful) {
                                estado = "PENDIENTE_AUDITORIA"
                                dictamen = null
                                Toast.makeText(context, "Expediente enviado a auditoría", Toast.LENGTH_LONG).show()
                            } else {
                                val msg = ApiClient.errorMessage(r.errorBody()?.string(), "No se pudo enviar el expediente")
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        } catch (e: IOException) {
                            Toast.makeText(context, "Sin conexión con la API", Toast.LENGTH_SHORT).show()
                        } finally {
                            enviando = false
                        }
                    }
                },
                enabled = !enviando,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedUEB)
            ) {
                Text(if (enviando) "Enviando..." else "Enviar expediente", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ArchivoBoton(texto: String, cargado: Boolean, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(if (cargado) "✓ $texto" else texto)
    }
}

// Valida lo mismo que la API, para dar el aviso antes de enviar
private fun validar(
    context: Context,
    cedula: String,
    clave: String,
    declarado: Boolean,
    cedulaUri: Uri?,
    tituloUri: Uri?,
    planillaUri: Uri?
): String? {
    if (cedula.length != 10) return "La cédula debe tener 10 dígitos"
    if (!Regex("^[0-9-]{10,40}$").matches(clave)) return "Clave catastral inválida"
    if (!declarado) return "Debe aceptar la declaración juramentada"
    if (cedulaUri == null || tituloUri == null || planillaUri == null) return "Falta adjuntar un documento"
    listOf(cedulaUri, tituloUri, planillaUri).forEach { uri ->
        val tam = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        if (tam > MAX_BYTES) return "Cada documento debe pesar máximo 5 MB"
    }
    return null
}

private fun archivoParte(context: Context, campo: String, uri: Uri): MultipartBody.Part {
    val tipo = context.contentResolver.getType(uri) ?: "application/octet-stream"
    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
    return MultipartBody.Part.createFormData(campo, campo, bytes.toRequestBody(tipo.toMediaTypeOrNull()))
}

private fun etiqueta(estado: String) = when (estado) {
    "PENDIENTE_AUDITORIA" -> "En auditoría"
    "APROBADO" -> "Aprobado"
    "OBSERVADO" -> "Observado: corrija y vuelva a enviar"
    "RECHAZADO" -> "Rechazado"
    "SIN_EXPEDIENTE" -> "Sin expediente"
    else -> estado
}
