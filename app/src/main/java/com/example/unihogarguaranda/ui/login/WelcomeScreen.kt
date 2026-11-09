package com.example.unihogarguaranda.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unihogarguaranda.R
import com.example.unihogarguaranda.ui.theme.BlueUEB
import com.example.unihogarguaranda.ui.theme.RedUEB

/**
 * Bienvenida: foto del campus a pantalla completa con un degradado azul UEB.
 * La marca queda arriba y el mensaje con las acciones abajo, al alcance del pulgar.
 */
@Composable
fun WelcomeScreen(onStart: () -> Unit, onHaveAccount: () -> Unit) {
    Box(Modifier.fillMaxSize().background(BlueUEB)) {
        Image(
            painterResource(R.drawable.campus_ueb),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to BlueUEB.copy(alpha = 0.6f),
                    0.18f to Color.Transparent,
                    0.3f to BlueUEB.copy(alpha = 0.35f),
                    0.45f to BlueUEB.copy(alpha = 0.85f),
                    0.6f to BlueUEB.copy(alpha = 0.96f),
                    1f to BlueUEB
                )
            )
        )

        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = maxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Marca
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painterResource(R.drawable.app_logo),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Uni", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Hogar", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "GUARANDA",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }

                Spacer(Modifier.height(120.dp))

                // Mensaje y acciones
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    Text(
                        "UNIVERSIDAD ESTATAL DE BOLÍVAR",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Tu hogar,\ncerca de la UEB.",
                        color = Color.White,
                        fontSize = 36.sp,
                        lineHeight = 42.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Alojamientos en Guaranda revisados por Bienestar Estudiantil UEB.",
                        color = Color.White.copy(alpha = 0.82f),
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                    Spacer(Modifier.height(24.dp))

                    Row(
                        Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Beneficio(Icons.Default.Verified, "Verificados", Modifier.weight(1f))
                        Beneficio(Icons.Default.LocationOn, "Cerca del campus", Modifier.weight(1f))
                        Beneficio(Icons.Default.ChatBubbleOutline, "Contacto directo", Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(28.dp))
                    Button(
                        onClick = onStart,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedUEB, contentColor = Color.White)
                    ) {
                        Text("Encontrar mi hogar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(10.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onHaveAccount,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Ya tengo una cuenta", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun Beneficio(icon: ImageVector, texto: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(30.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            texto,
            color = Color.White,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
