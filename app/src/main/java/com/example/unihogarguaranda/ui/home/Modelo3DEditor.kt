package com.example.unihogarguaranda.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.unihogarguaranda.data.ApiClient
import com.example.unihogarguaranda.data.InmuebleResponse
import com.example.unihogarguaranda.data.Session
import com.example.unihogarguaranda.model.Modelo3D
import com.example.unihogarguaranda.ui.screens.Room3DScreen
import kotlinx.coroutines.launch
import java.io.IOException

/** Convierte "3,5" o "3.5" en metros. */
internal fun leerMetros(texto: String): Double? = texto.trim().replace(',', '.').toDoubleOrNull()

/**
 * RF14: el propietario describe la habitación (medidas, acabados y muebles) para el modelo 3D ilustrativo.
 * La app arma el modelo en el teléfono; la API valida y guarda solo estos datos.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Modelo3DDialog(anuncio: InmuebleResponse, onDismiss: () -> Unit, onChanged: suspend () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val inicial = anuncio.modelo3d ?: Modelo3D()
    var ancho by rememberSaveable(anuncio.id) { mutableStateOf(inicial.ancho.toString()) }
    var largo by rememberSaveable(anuncio.id) { mutableStateOf(inicial.largo.toString()) }
    var piso by rememberSaveable(anuncio.id) { mutableStateOf(inicial.piso) }
    var pared by rememberSaveable(anuncio.id) { mutableStateOf(inicial.pared) }
    var ropa by rememberSaveable(anuncio.id) { mutableStateOf(inicial.ropa) }
    var ventana by rememberSaveable(anuncio.id) { mutableStateOf(inicial.ventana) }
    var muebles by rememberSaveable(anuncio.id) { mutableStateOf(inicial.muebles) }
    var vistaPrevia by rememberSaveable(anuncio.id) { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val modelo = Modelo3D(
        ancho = leerMetros(ancho) ?: Double.NaN,
        largo = leerMetros(largo) ?: Double.NaN,
        piso = piso, pared = pared, ropa = ropa, ventana = ventana,
        // Conserva el orden del catálogo para que el modelo sea estable.
        muebles = Modelo3D.MUEBLES.keys.filter { it in muebles }
    )
    val problema = modelo.error()

    fun enviar(quitar: Boolean) {
        val auth = Session.bearer(context) ?: return
        guardando = true
        error = null
        scope.launch {
            try {
                val respuesta = if (quitar) ApiClient.service.quitarModelo3D(auth, anuncio.id)
                    else ApiClient.service.guardarModelo3D(auth, anuncio.id, modelo)
                if (respuesta.isSuccessful) {
                    onChanged()
                    onDismiss()
                } else {
                    error = ApiClient.errorMessage(respuesta.errorBody()?.string(), "No se pudo guardar el modelo 3D")
                }
            } catch (_: IOException) {
                error = "Sin conexión con el servidor"
            } finally {
                guardando = false
            }
        }
    }

    @Composable
    fun Opciones(titulo: String, opciones: Map<String, String>, elegido: String, onElegir: (String) -> Unit) {
        Text(titulo, style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            opciones.forEach { (clave, nombre) ->
                FilterChip(selected = elegido == clave, onClick = { onElegir(clave) }, label = { Text(nombre) })
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        title = { Text("Modelo 3D de la habitación") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Describe el cuarto como aparece en tus fotos. El estudiante verá un modelo ilustrativo.",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        ancho, { ancho = it.take(5) }, label = { Text("Ancho (m)") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        largo, { largo = it.take(5) }, label = { Text("Largo (m)") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f)
                    )
                }
                Text("Muebles", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Modelo3D.MUEBLES.forEach { (clave, nombre) ->
                        FilterChip(
                            selected = clave in muebles,
                            onClick = {
                                muebles = when {
                                    clave in muebles -> muebles - clave
                                    clave == "CAMA_SIMPLE" -> muebles - "CAMA_DOBLE" + clave
                                    clave == "CAMA_DOBLE" -> muebles - "CAMA_SIMPLE" + clave
                                    else -> muebles + clave
                                }
                            },
                            label = { Text(nombre) }
                        )
                    }
                }
                Opciones("Piso", Modelo3D.PISOS, piso) { piso = it }
                Opciones("Paredes", Modelo3D.PAREDES, pared) { pared = it }
                Opciones("Ropa de cama", Modelo3D.ROPAS, ropa) { ropa = it }
                Opciones("Ventana en la pared del fondo", Modelo3D.VENTANAS, ventana) { ventana = it }
                OutlinedButton(onClick = { vistaPrevia = true }, enabled = problema == null, modifier = Modifier.fillMaxWidth()) {
                    Text("Vista previa")
                }
                (problema ?: error)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (anuncio.modelo3d != null) {
                    TextButton(onClick = { enviar(quitar = true) }, enabled = !guardando) { Text("Quitar modelo 3D") }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { enviar(quitar = false) }, enabled = !guardando && problema == null) {
                Text(if (guardando) "Guardando…" else "Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !guardando) { Text("Cancelar") } }
    )

    if (vistaPrevia && problema == null) {
        Dialog(
            onDismissRequest = { vistaPrevia = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(Modifier.fillMaxSize()) {
                Room3DScreen("Vista previa · ${anuncio.title}", modelo, onBack = { vistaPrevia = false })
            }
        }
    }
}
