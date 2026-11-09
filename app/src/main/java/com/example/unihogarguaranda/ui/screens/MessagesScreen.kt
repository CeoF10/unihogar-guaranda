package com.example.unihogarguaranda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unihogarguaranda.ui.theme.*

/**
 * Todavía no hay chat dentro de la app: el contacto entre estudiante y propietario
 * se hace por WhatsApp (o llamada) desde el anuncio. Esta pantalla lo explica.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(onBack: () -> Unit, esPropietario: Boolean = false) {
    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Mensajes", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BlueUEB) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = BlueUEB)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier.size(88.dp).clip(CircleShape).background(BlueUEB.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ChatBubbleOutline, null, tint = BlueUEB, modifier = Modifier.size(38.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text(
                if (esPropietario) "Los estudiantes te escriben por WhatsApp" else "Escribe directo al propietario",
                fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                if (esPropietario)
                    "Cuando un estudiante se interese en tu anuncio te contactará por WhatsApp o llamada al número que registraste."
                else
                    "Abre un anuncio y toca \u201cContactar propietario\u201d para escribirle por WhatsApp. Si no tienes WhatsApp, se abre el marcador.",
                fontSize = 14.sp, lineHeight = 21.sp, color = TextSecondary, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onBack,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedUEB, contentColor = Color.White),
                modifier = Modifier.height(52.dp)
            ) {
                Text(if (esPropietario) "Volver al panel" else "Ver alojamientos", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
