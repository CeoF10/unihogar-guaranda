package com.example.unihogarguaranda.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import com.example.unihogarguaranda.data.ApiClient
import com.example.unihogarguaranda.data.CargoResponse
import com.example.unihogarguaranda.data.NuevoInmuebleRequest
import com.example.unihogarguaranda.data.Session
import com.example.unihogarguaranda.ui.components.LocationPickerMap
import com.example.unihogarguaranda.ui.home.prepararFotos
import com.example.unihogarguaranda.ui.theme.*
import com.example.unihogarguaranda.util.GpsLocationProvider
import com.example.unihogarguaranda.util.GpsResult
import com.example.unihogarguaranda.util.GuarandaGeofenceValidator
import kotlinx.coroutines.launch
import java.io.IOException

private val VERDE = Color(0xFF047857)
private val SERVICIOS_INMUEBLE = listOf("WiFi", "Agua", "Luz", "Lavadora", "Amoblado", "Parqueadero", "Patio")
private const val MAX_FOTOS = 5

// Cargo mensual editable; monto vacío + "por confirmar" = costo desconocido (RF08)
private data class CargoForm(val nombre: String = "", val monto: String = "", val porConfirmar: Boolean = false)

/**
 * Publicar un inmueble en 4 pasos:
 * 1. Datos  2. Ubicación (GPS o mapa, solo dentro de Guaranda)  3. Precio y servicios  4. Fotos y publicar.
 * Al final se crea el inmueble, se suben las fotos y se envía a auditoría de Bienestar UEB.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RegisterPropertyWizardScreen(
    onDismiss: () -> Unit,
    onFinished: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(1) }
    var error by remember { mutableStateOf<String?>(null) }

    // Paso 1
    var titulo by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Habitación") }
    var direccion by remember { mutableStateOf("") }
    var dormitorios by remember { mutableStateOf(1) }
    var descripcion by remember { mutableStateOf("") }

    // Paso 2
    var lat by remember { mutableStateOf<Double?>(null) }
    var lng by remember { mutableStateOf<Double?>(null) }
    var precision by remember { mutableStateOf<Float?>(null) }
    var manual by remember { mutableStateOf(false) }
    var capturando by remember { mutableStateOf(false) }
    val dentro = lat != null && lng != null && GuarandaGeofenceValidator.isWithinGuaranda(lat!!, lng!!)

    // Paso 3
    var precio by remember { mutableStateOf("") }
    var deposito by remember { mutableStateOf("") }
    val servicios = remember { mutableStateListOf<String>() }
    val cargos = remember { mutableStateListOf<CargoForm>() }
    var contacto by remember { mutableStateOf("") }

    // Paso 4
    val fotos = remember { mutableStateListOf<Uri>() }
    var declarado by remember { mutableStateOf(false) }
    var enviando by remember { mutableStateOf(false) }
    var progreso by remember { mutableStateOf("") }
    var idCreado by remember { mutableStateOf<Long?>(null) }

    fun capturarGps() {
        capturando = true
        error = null
        GpsLocationProvider.requestCurrentLocation(context) { r ->
            capturando = false
            when (r) {
                is GpsResult.Success -> {
                    lat = r.latitude; lng = r.longitude; precision = r.accuracyMeters; manual = false
                }
                is GpsResult.Error -> error = r.message
            }
        }
    }

    val permisoUbicacion = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { g ->
        if (g[Manifest.permission.ACCESS_FINE_LOCATION] == true) capturarGps()
        else error = "Sin permiso de ubicación. Puede marcar el inmueble tocando el mapa."
    }

    val selectorFotos = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(MAX_FOTOS)
    ) { uris -> uris.forEach { if (fotos.size < MAX_FOTOS && it !in fotos) fotos.add(it) } }

    // Valida el paso actual; devuelve el mensaje de error o null
    fun validarPaso(): String? = when (step) {
        1 -> when {
            titulo.trim().length < 5 -> "Escriba un título de al menos 5 letras"
            direccion.isBlank() -> "Escriba la dirección del inmueble"
            else -> null
        }
        2 -> when {
            lat == null -> "Use su GPS o toque el mapa para ubicar el inmueble"
            !dentro -> "El inmueble debe estar dentro de Guaranda (15 km del centro)"
            else -> null
        }
        3 -> when {
            (precio.toDoubleOrNull() ?: 0.0) <= 0 -> "Escriba el arriendo mensual"
            cargos.any { it.nombre.isBlank() } -> "Cada cargo necesita un nombre"
            cargos.any { !it.porConfirmar && it.monto.toDoubleOrNull() == null } -> "Ponga el monto del cargo o márquelo como por confirmar"
            else -> null
        }
        else -> null
    }

    fun solicitud() = NuevoInmuebleRequest(
        titulo = titulo.trim(),
        tipo = tipo,
        direccion = direccion.trim(),
        descripcion = descripcion.trim().ifEmpty { null },
        precio = precio.toDouble(),
        deposito = deposito.toDoubleOrNull() ?: 0.0,
        dormitorios = dormitorios,
        servicios = servicios.toList(),
        lat = lat!!,
        lng = lng!!,
        precisionGpsM = if (manual) null else precision?.toDouble(),
        origen = if (manual) "MANUAL" else "GPS",
        cargos = cargos.map { CargoResponse(it.nombre.trim(), if (it.porConfirmar) null else it.monto.toDoubleOrNull()) }
    )

    // Crea el inmueble (una sola vez), sube las fotos y lo envía a auditoría
    fun publicar(soloBorrador: Boolean) {
        val auth = Session.bearer(context) ?: return
        enviando = true
        error = null
        scope.launch {
            try {
                val id = idCreado ?: run {
                    progreso = "Guardando datos..."
                    val r = ApiClient.service.crearInmueble(auth, solicitud())
                    if (!r.isSuccessful) throw IllegalStateException(ApiClient.errorMessage(r.errorBody()?.string(), "No se pudo guardar"))
                    r.body()!!.id.also { idCreado = it }
                }
                if (contacto.isNotBlank()) {
                    ApiClient.service.editarInmueble(auth, id, com.example.unihogarguaranda.data.EditarInmuebleRequest(contacto = contacto.trim()))
                }
                if (soloBorrador) {
                    onFinished("Borrador guardado. Puede subir fotos y publicarlo desde Mis publicaciones.")
                    return@launch
                }
                progreso = "Subiendo ${fotos.size} foto(s)..."
                val partes = prepararFotos(context, fotos.toList())
                if (partes.isEmpty()) throw IllegalStateException("Las fotos no son válidas (JPEG, PNG o WebP de máximo 5 MB)")
                val rf = ApiClient.service.subirFotos(auth, id, partes)
                if (!rf.isSuccessful) throw IllegalStateException(ApiClient.errorMessage(rf.errorBody()?.string(), "No se pudieron subir las fotos"))
                fotos.clear()
                progreso = "Enviando a auditoría..."
                val rp = ApiClient.service.publicar(auth, id)
                if (!rp.isSuccessful) throw IllegalStateException(ApiClient.errorMessage(rp.errorBody()?.string(), "No se pudo publicar"))
                onFinished("Inmueble enviado a auditoría. Bienestar UEB lo revisará antes de mostrarlo a los estudiantes.")
            } catch (e: IOException) {
                error = "Sin conexión con el servidor UniHogar"
            } catch (e: IllegalStateException) {
                error = e.message
            } finally {
                enviando = false
                progreso = ""
            }
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            Column(Modifier.background(Color.White)) {
                TopAppBar(
                    title = {
                        Column {
                            Text("Publicar inmueble", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BlueUEB)
                            Text(
                                "Paso $step de 4 · " + listOf("Datos", "Ubicación", "Precio y servicios", "Fotos y publicar")[step - 1],
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = BlueUEB) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                LinearProgressIndicator(
                    progress = { step / 4f },
                    modifier = Modifier.fillMaxWidth(),
                    color = RedUEB,
                    trackColor = ChipBackground
                )
            }
        },
        bottomBar = {
            Surface(color = Color.White, shadowElevation = 8.dp) {
                Column(
                    Modifier
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    error?.let {
                        Text(it, color = RedUEB, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    if (progreso.isNotEmpty()) {
                        Text(progreso, color = BlueUEB, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (step > 1) {
                            OutlinedButton(
                                onClick = { step--; error = null },
                                enabled = !enviando,
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(25.dp)
                            ) { Text("Atrás") }
                        }
                        Button(
                            onClick = {
                                if (step < 4) {
                                    val e = validarPaso()
                                    if (e == null) { step++; error = null } else error = e
                                } else if (fotos.isEmpty()) {
                                    error = "Agregue al menos una foto para publicar"
                                } else if (!declarado) {
                                    error = "Confirme la declaración para publicar"
                                } else {
                                    publicar(soloBorrador = false)
                                }
                            },
                            enabled = !enviando,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RedUEB)
                        ) {
                            if (enviando) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                            else Text(if (step < 4) "Siguiente" else "Publicar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (step) {
                // ---------------- PASO 1: DATOS ----------------
                1 -> {
                    Encabezado("¿Qué vas a arrendar?", "Estos datos verán los estudiantes en el catálogo.")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(
                            Triple("Habitación", Icons.Default.Bed, RedUEB),
                            Triple("Departamento", Icons.Default.Apartment, BlueUEB),
                            Triple("Casa", Icons.Default.Home, Color(0xFF10B981))
                        ).forEach { (t, icono, color) ->
                            TipoCard(t, icono, color, seleccionado = tipo == t, modifier = Modifier.weight(1f)) { tipo = t }
                        }
                    }
                    Campo(titulo, { titulo = it.take(120) }, "Título del anuncio", "ej. Habitación amoblada cerca de la UEB")
                    Campo(direccion, { direccion = it }, "Dirección", "ej. Av. Guayaquil y Sucre, sector Alpachaca", icono = Icons.Default.LocationOn)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Dormitorios", fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.weight(1f))
                        Contador(dormitorios, { dormitorios = it.coerceIn(1, 10) })
                    }
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it.take(600) },
                        label = { Text("Descripción (opcional)") },
                        placeholder = { Text("Cuente qué incluye, reglas de la casa, transporte cercano...") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = coloresCampo()
                    )
                }

                // ---------------- PASO 2: UBICACIÓN ----------------
                2 -> {
                    Encabezado("¿Dónde está?", "Use el GPS estando en el inmueble, o toque el mapa para marcarlo.")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.dp, DividerColor, RoundedCornerShape(18.dp))
                    ) {
                        LocationPickerMap(
                            latitude = lat,
                            longitude = lng,
                            onPick = { la, lo ->
                                lat = la; lng = lo; precision = null; manual = true; error = null
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        ExtendedFloatingActionButton(
                            onClick = {
                                val ok = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                                    PackageManager.PERMISSION_GRANTED
                                if (ok) capturarGps()
                                else permisoUbicacion.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                            },
                            containerColor = BlueUEB,
                            contentColor = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp)
                        ) {
                            if (capturando) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                            else Icon(Icons.Default.MyLocation, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (capturando) "Buscando señal GPS..." else "Usar mi ubicación")
                        }
                    }
                    EstadoUbicacion(lat, lng, precision, manual, dentro)
                }

                // ---------------- PASO 3: PRECIO Y SERVICIOS ----------------
                3 -> {
                    Encabezado("Precio y servicios", "El estudiante verá el arriendo, los cargos y el depósito por separado.")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Campo(precio, { precio = soloNumero(it) }, "Arriendo / mes", "150", Modifier.weight(1f), teclado = KeyboardType.Decimal, prefijo = "$")
                        Campo(deposito, { deposito = soloNumero(it) }, "Depósito", "0", Modifier.weight(1f), teclado = KeyboardType.Decimal, prefijo = "$")
                    }

                    Text("Servicios incluidos", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SERVICIOS_INMUEBLE.forEach { s ->
                            val marcado = s in servicios
                            FilterChip(
                                selected = marcado,
                                onClick = { if (marcado) servicios.remove(s) else servicios.add(s) },
                                label = { Text(s) },
                                leadingIcon = if (marcado) { { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) } } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BlueUEB,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                )
                            )
                        }
                    }

                    Text("Cargos mensuales aparte del arriendo", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("Si aún no sabe el valor (ej. internet), márquelo como por confirmar.", fontSize = 12.sp, color = TextSecondary)
                    cargos.forEachIndexed { i, c ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Campo(c.nombre, { cargos[i] = c.copy(nombre = it) }, "Cargo", "ej. Agua", Modifier.weight(1.3f))
                                    if (!c.porConfirmar) {
                                        Campo(c.monto, { cargos[i] = c.copy(monto = soloNumero(it)) }, "Monto", "5", Modifier.weight(1f),
                                            teclado = KeyboardType.Decimal, prefijo = "$")
                                    }
                                    IconButton(onClick = { cargos.removeAt(i) }) { Icon(Icons.Default.Delete, "Quitar cargo", tint = TextSecondary) }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = c.porConfirmar, onCheckedChange = { cargos[i] = c.copy(porConfirmar = it, monto = "") })
                                    Text("Monto por confirmar", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                    OutlinedButton(onClick = { cargos.add(CargoForm()) }, shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Agregar cargo")
                    }

                    Campo(contacto, { contacto = it.filter { ch -> ch.isDigit() }.take(10) }, "Celular de contacto (WhatsApp)", "09XXXXXXXX",
                        teclado = KeyboardType.Phone, icono = Icons.Default.Phone)
                    ResumenCostos(precio, deposito, cargos)
                }

                // ---------------- PASO 4: FOTOS Y PUBLICAR ----------------
                else -> {
                    Encabezado("Fotos", "Agregue hasta $MAX_FOTOS fotos (JPEG, PNG o WebP, máximo 5 MB). La primera será la portada.")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        fotos.forEachIndexed { i, uri ->
                            Box(Modifier.size(100.dp).clip(RoundedCornerShape(14.dp))) {
                                AsyncImage(model = uri, contentDescription = "Foto ${i + 1}", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                if (i == 0) {
                                    Surface(color = BlueUEB, shape = RoundedCornerShape(bottomEnd = 10.dp), modifier = Modifier.align(Alignment.TopStart)) {
                                        Text("Portada", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                IconButton(
                                    onClick = { fotos.removeAt(i) },
                                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(26.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.55f))
                                ) { Icon(Icons.Default.Close, "Quitar foto", tint = Color.White, modifier = Modifier.size(16.dp)) }
                            }
                        }
                        if (fotos.size < MAX_FOTOS) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(BorderStroke(1.5.dp, BlueUEB.copy(alpha = 0.4f)), RoundedCornerShape(14.dp))
                                    .background(Color.White)
                                    .clickable {
                                        selectorFotos.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AddAPhoto, null, tint = BlueUEB)
                                    Text("Agregar", fontSize = 12.sp, color = BlueUEB)
                                }
                            }
                        }
                    }

                    // Resumen antes de publicar
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Resumen", fontWeight = FontWeight.Bold, color = BlueUEB)
                            FilaResumen("Anuncio", "$tipo · ${titulo.trim()}")
                            FilaResumen("Dirección", direccion.trim())
                            FilaResumen("Ubicación", if (manual) "Marcada en el mapa" else "GPS (±${precision?.toInt() ?: 0} m)")
                            FilaResumen("Arriendo", "$${precio} / mes · depósito $${deposito.ifEmpty { "0" }}")
                            if (servicios.isNotEmpty()) FilaResumen("Servicios", servicios.joinToString(", "))
                        }
                    }

                    Row(verticalAlignment = Alignment.Top) {
                        Checkbox(checked = declarado, onCheckedChange = { declarado = it })
                        Text(
                            "Declaro que soy el propietario o tengo autorización para arrendar este inmueble y que los datos son verdaderos.",
                            fontSize = 13.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                    TextButton(onClick = { publicar(soloBorrador = true) }, enabled = !enviando, modifier = Modifier.fillMaxWidth()) {
                        Text("Guardar como borrador y terminar después", color = TextSecondary)
                    }
                }
            }
        }
    }
}

// ---------------- Componentes del asistente ----------------

@Composable
private fun Encabezado(titulo: String, subtitulo: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(titulo, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(subtitulo, fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp)
    }
}

@Composable
private fun coloresCampo() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = BlueUEB,
    unfocusedBorderColor = DividerColor,
    focusedLabelColor = BlueUEB
)

@Composable
private fun Campo(
    valor: String,
    onChange: (String) -> Unit,
    etiqueta: String,
    ejemplo: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    teclado: KeyboardType = KeyboardType.Text,
    icono: ImageVector? = null,
    prefijo: String? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        label = { Text(etiqueta) },
        placeholder = { Text(ejemplo, color = TextSecondary.copy(alpha = 0.6f)) },
        leadingIcon = icono?.let { { Icon(it, null, tint = BlueUEB) } },
        prefix = prefijo?.let { { Text(it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = coloresCampo()
    )
}

@Composable
private fun TipoCard(texto: String, icono: ImageVector, color: Color, seleccionado: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (seleccionado) color.copy(alpha = 0.10f) else Color.White,
        border = BorderStroke(if (seleccionado) 2.dp else 1.dp, if (seleccionado) color else DividerColor),
        modifier = modifier
    ) {
        Column(Modifier.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(if (seleccionado) color else color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Icon(icono, null, tint = if (seleccionado) Color.White else color) }
            Spacer(Modifier.height(6.dp))
            Text(texto, fontSize = 12.sp, fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium, color = TextPrimary)
        }
    }
}

@Composable
private fun Contador(valor: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val colores = IconButtonDefaults.filledTonalIconButtonColors(containerColor = ChipBackground, contentColor = BlueUEB)
        FilledTonalIconButton(onClick = { onChange(valor - 1) }, enabled = valor > 1, colors = colores) { Icon(Icons.Default.Remove, "Menos") }
        Text("$valor", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        FilledTonalIconButton(onClick = { onChange(valor + 1) }, enabled = valor < 10, colors = colores) { Icon(Icons.Default.Add, "Más") }
    }
}

private data class EstadoVisual(val fondo: Color, val acento: Color, val icono: ImageVector, val titulo: String, val detalle: String)

@Composable
private fun EstadoUbicacion(lat: Double?, lng: Double?, precision: Float?, manual: Boolean, dentro: Boolean) {
    val estado = when {
        lat == null || lng == null -> EstadoVisual(
            Color.White, TextSecondary, Icons.Default.TouchApp,
            "Sin ubicación", "Toque el mapa o use el botón de GPS."
        )
        !dentro -> EstadoVisual(
            Color(0xFFFEE2E2), RedUEB, Icons.Default.WrongLocation,
            "Fuera de Guaranda",
            "Está a %.1f km del centro. Solo se aceptan inmuebles a menos de 15 km.".format(GuarandaGeofenceValidator.calculateDistanceKm(lat, lng))
        )
        else -> EstadoVisual(
            Color(0xFFD1FAE5), VERDE, Icons.Default.CheckCircle,
            "Ubicación dentro de Guaranda",
            "%.5f, %.5f · a %.1f km del centro · %s".format(
                lat, lng, GuarandaGeofenceValidator.calculateDistanceKm(lat, lng),
                if (manual) "marcada en el mapa" else "GPS ±${precision?.toInt() ?: 0} m"
            )
        )
    }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = estado.fondo,
        border = BorderStroke(1.dp, if (lat == null) DividerColor else estado.acento),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(estado.icono, null, tint = estado.acento)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(estado.titulo, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                Text(estado.detalle, fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun ResumenCostos(precio: String, deposito: String, cargos: List<CargoForm>) {
    val arriendo = precio.toDoubleOrNull() ?: return
    val desconocidos = cargos.filter { it.porConfirmar }.map { it.nombre.ifBlank { "un cargo" } }
    val total = arriendo + cargos.filter { !it.porConfirmar }.sumOf { it.monto.toDoubleOrNull() ?: 0.0 }
    Surface(shape = RoundedCornerShape(14.dp), color = BlueUEB.copy(alpha = 0.06f), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text("Así lo verá el estudiante", fontSize = 12.sp, color = TextSecondary)
            Text(
                if (desconocidos.isEmpty()) "Total mensual: $${total.toInt()}" else "Total pendiente: falta ${desconocidos.joinToString(", ")}",
                fontWeight = FontWeight.Bold,
                color = if (desconocidos.isEmpty()) BlueUEB else RedUEB
            )
            Text("Depósito (una sola vez): $${(deposito.toDoubleOrNull() ?: 0.0).toInt()}", fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun FilaResumen(etiqueta: String, valor: String) {
    Row {
        Text("$etiqueta: ", fontSize = 13.sp, color = TextSecondary)
        Text(valor, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

private fun soloNumero(texto: String): String {
    val limpio = texto.filter { it.isDigit() || it == '.' }
    // Solo un punto decimal
    val i = limpio.indexOf('.')
    return if (i < 0) limpio else limpio.substring(0, i + 1) + limpio.substring(i + 1).replace(".", "")
}
