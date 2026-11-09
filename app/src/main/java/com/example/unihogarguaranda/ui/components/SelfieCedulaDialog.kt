package com.example.unihogarguaranda.ui.components

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.unihogarguaranda.ui.theme.*
import com.example.unihogarguaranda.util.SelfieCedula

/**
 * Foto del propietario sosteniendo su cédula junto al rostro.
 * Se toma con la cámara del teléfono a resolución completa, se guarda en la app
 * y se envía a Bienestar UEB con el expediente para que verifique la identidad.
 */
@Composable
fun SelfieCedulaDialog(onDismiss: () -> Unit, onConfirmada: () -> Unit) {
    val context = LocalContext.current
    var vista by remember { mutableStateOf<Bitmap?>(SelfieCedula.vistaPrevia(context)) }

    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok && SelfieCedula.confirmarCaptura(context)) {
            vista = SelfieCedula.vistaPrevia(context)
        } else if (ok) {
            Toast.makeText(context, "No se pudo leer la foto. Intente otra vez.", Toast.LENGTH_SHORT).show()
        }
    }
    val abrirCamara = {
        try {
            camara.launch(SelfieCedula.uriCaptura(context))
        } catch (e: android.content.ActivityNotFoundException) {
            Toast.makeText(context, "Este teléfono no tiene una app de cámara disponible", Toast.LENGTH_LONG).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Foto con tu cédula", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = BlueUEB, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                    }
                }
                Text(
                    "Mira de frente a la cámara y sostén tu cédula junto a tu rostro, con los datos legibles. " +
                        "Bienestar UEB la compara con la foto de tu cédula del expediente.",
                    fontSize = 13.sp, lineHeight = 19.sp, color = TextSecondary
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (vista != null) Color.Black else ChipBackground),
                    contentAlignment = Alignment.Center
                ) {
                    val foto = vista
                    if (foto != null) {
                        Image(foto.asImageBitmap(), "Foto con la cédula", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PhotoCamera, null, tint = BlueUEB, modifier = Modifier.size(44.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Aún no hay foto", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }
                if (vista == null) {
                    Button(
                        onClick = abrirCamara,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueUEB)
                    ) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Abrir cámara", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = abrirCamara, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp)) {
                            Text("Repetir")
                        }
                        Button(
                            onClick = onConfirmada,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                        ) {
                            Text("Usar foto", fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}
