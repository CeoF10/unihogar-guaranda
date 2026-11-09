package com.example.unihogarguaranda.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unihogarguaranda.R
import com.example.unihogarguaranda.ui.theme.*

@Composable
fun WelcomeScreen(onStart: () -> Unit, onHaveAccount: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Image(
            painterResource(R.drawable.campus_ueb),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
            listOf(BlueUEB.copy(alpha = 0.7f), BlueUEB.copy(alpha = 0.15f), BlueUEB)
        )))
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(
                    painterResource(R.drawable.app_logo), contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                )
                Column {
                    Text("UniHogar", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("GUARANDA · UEB", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, letterSpacing = 2.sp)
                }
            }
            Spacer(Modifier.height(180.dp))
            Surface(
                color = Color.White.copy(alpha = 0.16f),
                shape = RoundedCornerShape(50),
                modifier = Modifier.align(Alignment.Start)
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Tu nueva etapa empieza aquí", color = Color.White, fontSize = 12.sp)
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Un lugar para\nsentirte en casa.", fontSize = 30.sp, lineHeight = 37.sp, fontWeight = FontWeight.Bold, color = BlueUEB)
                    Text(
                        "Encuentra tu espacio en Guaranda y disfruta de la vida cerca de la UEB.",
                        color = TextSecondary, fontSize = 14.sp, lineHeight = 22.sp
                    )
                    Button(
                        onClick = onStart, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedUEB)
                    ) {
                        Text("Encontrar mi hogar", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(20.dp))
                    }
                    TextButton(onClick = onHaveAccount, modifier = Modifier.fillMaxWidth()) {
                        Text("Ya tengo una cuenta", fontWeight = FontWeight.SemiBold, color = BlueUEB)
                    }
                }
            }
        }
    }
}
