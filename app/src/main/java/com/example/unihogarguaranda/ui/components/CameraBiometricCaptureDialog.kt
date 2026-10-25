package com.example.unihogarguaranda.ui.components

import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import com.example.unihogarguaranda.ui.theme.*

/**
 * Modal interactivo con acceso a la CÁMARA nativa del dispositivo.
 * Incluye fallback para emulador Android (donde el sensor emulado proyecta un cuadro verde/ajedrez por defecto).
 */
@Composable
fun CameraBiometricCaptureDialog(
    onDismiss: () -> Unit,
    onBiometricConfirmed: (Bitmap?) -> Unit
) {
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    // El placeholder de emulador solo se permite en builds debug, nunca en release
    val isDebugBuild = (LocalContext.current.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    // Launcher oficial de Android para capturar foto real con la Cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedBitmap = bitmap
        } else if (isDebugBuild) {
            // Generar avatar biométrico de prueba si la cámara emulada no entrega bitmap
            capturedBitmap = createBiometricPlaceholderBitmap()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Captura Biométrica con Cámara",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = BlueUEB
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                    }
                }

                Text(
                    text = "Enfoque su rostro frontal y sostenga su cédula visible junto a su mejilla.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                // Visor de foto capturada
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (capturedBitmap != null) Color.Black else Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    if (capturedBitmap != null) {
                        Image(
                            bitmap = capturedBitmap!!.asImageBitmap(),
                            contentDescription = "Foto capturada",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = BlueUEB, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Cámara lista para captura", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }

                if (isAnalyzing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = BlueUEB)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analizando rostro y OCR de cédula...", fontSize = 12.sp, color = BlueUEB, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Botones de acción de la Cámara
                if (capturedBitmap == null) {
                    Button(
                        onClick = { cameraLauncher.launch() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueUEB)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Abrir Cámara del Dispositivo")
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { cameraLauncher.launch() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Repetir")
                        }

                        Button(
                            onClick = {
                                isAnalyzing = true
                                onBiometricConfirmed(capturedBitmap)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                        ) {
                            Text("Confirmar")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Genera un Bitmap gráfico representativo para entornos de emulador
 * donde el sensor emulado no dispone de cámara física de escritorio conectada.
 */
private fun createBiometricPlaceholderBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = android.graphics.Color.DKGRAY
        style = Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, 400f, 300f, paint)

    paint.color = android.graphics.Color.WHITE
    paint.textSize = 22f
    paint.textAlign = Paint.Align.CENTER
    canvas.drawText("CAPTURA BIOMÉTRICA", 200f, 130f, paint)

    paint.textSize = 16f
    paint.color = android.graphics.Color.LTGRAY
    canvas.drawText("ROSTRO + CÉDULA OK", 200f, 170f, paint)
    return bitmap
}
