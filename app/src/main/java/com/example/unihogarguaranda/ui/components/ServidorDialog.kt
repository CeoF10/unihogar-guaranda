package com.example.unihogarguaranda.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unihogarguaranda.data.ApiClient
import com.example.unihogarguaranda.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Cambia la dirección de la API sin recompilar: IP de la PC en la misma Wi-Fi
 * (p. ej. 192.168.1.5), 127.0.0.1 por cable con adb reverse, o un túnel HTTPS.
 * Antes de guardar comprueba que el servidor responda.
 */
@Composable
fun ServidorDialog(onDismiss: () -> Unit, onGuardado: () -> Unit) {
    val scope = rememberCoroutineScope()
    var texto by remember { mutableStateOf(ApiClient.baseUrl.removeSuffix("/api/")) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var probando by remember { mutableStateOf(false) }

    fun probarYGuardar(url: String?) {
        if (url == null) { mensaje = "Dirección no válida"; return }
        probando = true
        mensaje = null
        scope.launch {
            mensaje = try {
                if (ApiClient.crear(url).health().isSuccessful) {
                    ApiClient.guardarServidor(url)
                    onGuardado()
                    null
                } else "El servidor respondió con un error"
            } catch (e: Exception) {
                "No responde: ${url.removeSuffix("/api/")}. Revise que la API esté encendida y que el teléfono esté en la misma red."
            } finally {
                probando = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!probando) onDismiss() },
        title = { Text("Servidor de UniHogar", fontWeight = FontWeight.Bold, color = BlueUEB) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Escribe la IP de la PC donde corre la API (misma Wi-Fi), por ejemplo 192.168.1.5, o la dirección HTTPS de un túnel.",
                    fontSize = 13.sp, color = TextSecondary
                )
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it; mensaje = null },
                    singleLine = true,
                    label = { Text("IP o dirección") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth()
                )
                ApiClient.normalizar(texto)?.let { Text("Se usará: $it", fontSize = 11.sp, color = TextSecondary) }
                mensaje?.let { Text(it, fontSize = 12.sp, color = RedUEB) }
                if (probando) LinearProgressIndicator(Modifier.fillMaxWidth(), color = BlueUEB)
            }
        },
        confirmButton = {
            TextButton(enabled = !probando, onClick = { probarYGuardar(ApiClient.normalizar(texto)) }) {
                Text("Probar y guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(enabled = !probando, onClick = { probarYGuardar(ApiClient.urlPorDefecto) }) {
                Text("Usar predeterminado")
            }
        }
    )
}
