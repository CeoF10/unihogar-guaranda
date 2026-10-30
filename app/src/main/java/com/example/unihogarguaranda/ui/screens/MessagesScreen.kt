package com.example.unihogarguaranda.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unihogarguaranda.ui.theme.*

data class Conversation(
    val name: String,
    val lastMessage: String,
    val time: String,
    val unread: Boolean
)

// Conversaciones de demostración (sin backend de mensajería todavía)
private val demoConversations = listOf(
    Conversation("Carlos Méndez", "¿Sigue disponible?", "10:24", unread = true),
    Conversation("Ana Torres", "Me interesa la vivienda", "09:45", unread = true),
    Conversation("Propietario", "Gracias por escribir", "Ayer", unread = false),
    Conversation("Luis López", "¿Podrías dar más información?", "Ayer", unread = false)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Mensajes", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BlueUEB) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = BlueUEB)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Button(
                onClick = { Toast.makeText(context, "Nuevo mensaje (próximamente)", Toast.LENGTH_SHORT).show() },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(20.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedUEB)
            ) {
                Text("Nuevo mensaje", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(demoConversations) { conversation ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            Toast.makeText(context, "Chat con ${conversation.name}", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ChipBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(conversation.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                        Text(
                            conversation.lastMessage,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(conversation.time, fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (conversation.unread) RedUEB else DividerColor)
                        )
                    }
                }
                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(start = 82.dp))
            }
        }
    }
}
