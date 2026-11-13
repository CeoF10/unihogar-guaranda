package com.example.unihogarguaranda.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.unihogarguaranda.data.ApiClient
import com.example.unihogarguaranda.data.DecisionVisitaRequest
import com.example.unihogarguaranda.data.MensajeResponse
import com.example.unihogarguaranda.data.Session
import com.example.unihogarguaranda.data.VisitaResponse
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val horaEcuador = TimeZone.getTimeZone("America/Guayaquil")

@Composable
internal fun rememberVisitPicker(): ((String) -> Unit) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { onSelected ->
            val fecha = Calendar.getInstance(horaEcuador)
            DatePickerDialog(context, { _, year, month, day ->
                TimePickerDialog(context, { _, hour, minute ->
                    fecha.clear()
                    fecha.set(year, month, day, hour, minute)
                    if (fecha.timeInMillis <= System.currentTimeMillis()) {
                        Toast.makeText(context, "Elige una fecha y hora futura", Toast.LENGTH_LONG).show()
                    } else {
                        val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
                        iso.timeZone = horaEcuador
                        onSelected(iso.format(fecha.time))
                    }
                }, 9, 0, true).apply { setTitle("Hora de Ecuador") }.show()
            }, fecha.get(Calendar.YEAR), fecha.get(Calendar.MONTH), fecha.get(Calendar.DAY_OF_MONTH)).apply {
                datePicker.minDate = System.currentTimeMillis()
            }.show()
        }
    }
}

internal fun fechaEcuador(iso: String?): String? {
    if (iso == null) return null
    val entrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
    val fecha: Date = runCatching { entrada.parse(iso) }.getOrNull() ?: return null
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-EC")).apply {
        timeZone = horaEcuador
    }.format(fecha)
}

@Composable
internal fun rememberListingAction(): (suspend (String) -> Response<MensajeResponse>) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var enviando by remember { mutableStateOf(false) }
    return { accion ->
        if (!enviando) {
            val auth = Session.bearer(context)
            if (auth == null) {
                Toast.makeText(context, "Inicia sesión para continuar", Toast.LENGTH_SHORT).show()
            } else {
                enviando = true
                scope.launch {
                    try {
                        val r = accion(auth)
                        val mensaje = if (r.isSuccessful) r.body()?.mensaje ?: "Listo"
                            else ApiClient.errorMessage(r.errorBody()?.string(), "No se pudo completar la acción")
                        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                    } catch (_: IOException) {
                        Toast.makeText(context, "Sin conexión. Vuelve a intentarlo.", Toast.LENGTH_LONG).show()
                    } finally {
                        enviando = false
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VisitsScreen(propietario: Boolean, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val elegirFecha = rememberVisitPicker()
    var lista by remember { mutableStateOf<List<VisitaResponse>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var actualizando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    suspend fun cargar() {
        val auth = Session.bearer(context)
        error = null
        try {
            if (auth == null) {
                error = "Inicia sesión para consultar tus visitas."
                return
            }
            val r = ApiClient.service.visitas(auth, if (propietario) "PROPIETARIO" else "ESTUDIANTE")
            if (r.isSuccessful) lista = r.body().orEmpty()
            else error = ApiClient.errorMessage(r.errorBody()?.string(), "No se pudieron cargar las visitas.")
        } catch (_: IOException) {
            error = "Sin conexión con el servidor."
        } finally {
            cargando = false
        }
    }

    fun responder(visita: VisitaResponse, accion: String, fecha: String? = null) {
        if (actualizando) return
        val auth = Session.bearer(context) ?: return
        actualizando = true
        scope.launch {
            try {
                val r = ApiClient.service.decidirVisita(auth, visita.id, DecisionVisitaRequest(accion, visita.version, fecha))
                cargar()
                if (!r.isSuccessful) error = ApiClient.errorMessage(r.errorBody()?.string(), "No se pudo actualizar la visita.")
            } catch (_: IOException) {
                error = "Sin conexión. Revisa el estado antes de volver a intentar."
            } finally {
                actualizando = false
            }
        }
    }

    BackHandler(onBack = onBack)
    LaunchedEffect(propietario) { cargar() }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Solicitudes de visita") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            },
            actions = {
                TextButton(onClick = { cargando = true; scope.launch { cargar() } }, enabled = !actualizando && !cargando) {
                    Text("Actualizar")
                }
            }
        )
    }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (cargando) item { CircularProgressIndicator() }
            error?.let { mensaje -> item { Text(mensaje, color = MaterialTheme.colorScheme.error) } }
            if (!cargando && error == null && lista.isEmpty()) item { Text("Todavía no hay solicitudes de visita.") }
            items(lista, key = { it.id }) { visita ->
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(visita.titulo, style = MaterialTheme.typography.titleMedium)
                        Text(visita.conQuien)
                        Text(fechaEcuador(visita.fechaPropuesta)?.let { "$it · Ecuador" } ?: "Fecha por confirmar")
                        Text(when (visita.estado) {
                            "PENDIENTE" -> "Pendiente"
                            "ACEPTADA" -> "Aceptada"
                            "RECHAZADA" -> "Rechazada"
                            "REPROGRAMACION_PROPUESTA" -> "Nueva fecha propuesta"
                            "CANCELADA" -> "Cancelada"
                            "REALIZADA" -> "Realizada"
                            else -> visita.estado
                        }, style = MaterialTheme.typography.labelLarge)
                        val puedeResponder = (propietario && visita.estado == "PENDIENTE") ||
                            (!propietario && visita.estado == "REPROGRAMACION_PROPUESTA")
                        if (puedeResponder) {
                            Row {
                                TextButton(onClick = { responder(visita, "ACEPTAR") }, enabled = !actualizando) { Text("Aceptar") }
                                TextButton(onClick = { responder(visita, "RECHAZAR") }, enabled = !actualizando) { Text("Rechazar") }
                            }
                        }
                        if (propietario && visita.estado == "PENDIENTE") {
                            TextButton(onClick = { elegirFecha { responder(visita, "REPROGRAMAR", it) } }, enabled = !actualizando) {
                                Text("Proponer otra fecha")
                            }
                        }
                        if (visita.estado in setOf("PENDIENTE", "ACEPTADA", "REPROGRAMACION_PROPUESTA")) {
                            TextButton(onClick = { responder(visita, "CANCELAR") }, enabled = !actualizando) { Text("Cancelar visita") }
                        }
                    }
                }
            }
        }
    }
}
