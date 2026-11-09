package com.example.unihogarguaranda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unihogarguaranda.data.ApiClient
import com.example.unihogarguaranda.data.ConversacionResponse
import com.example.unihogarguaranda.data.ChatMensaje
import com.example.unihogarguaranda.data.Session
import com.example.unihogarguaranda.data.TextoRequest
import com.example.unihogarguaranda.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// Fechas ISO de la API ("2026-11-09T21:55:47.768Z") → "16:55" si es de hoy, "09/11" si no
private fun horaCorta(iso: String): String = runCatching {
    val fecha = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US).parse(iso) ?: return ""
    val dia = SimpleDateFormat("yyyyMMdd", Locale.US)
    val patron = if (dia.format(fecha) == dia.format(java.util.Date())) "HH:mm" else "dd/MM"
    SimpleDateFormat(patron, Locale.getDefault()).format(fecha)
}.getOrDefault("")

/** Bandeja de conversaciones del usuario. Se actualiza cada 10 s mientras está abierta. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    onBack: () -> Unit,
    esPropietario: Boolean = false,
    onAbrirConversacion: (Long, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var conversaciones by remember { mutableStateOf<List<ConversacionResponse>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            val auth = Session.bearer(context)
            if (auth != null) {
                try {
                    val r = ApiClient.service.getConversaciones(auth)
                    if (r.isSuccessful) { conversaciones = r.body(); error = null }
                    else error = ApiClient.errorMessage(r.errorBody()?.string(), "No se pudieron cargar los mensajes")
                } catch (e: Exception) {
                    error = "Sin conexión con el servidor UniHogar"
                }
            }
            delay(10_000)
        }
    }

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
    ) { padding ->
        val lista = conversaciones
        when {
            lista == null && error == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BlueUEB)
            }
            lista.isNullOrEmpty() -> Column(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(Modifier.size(88.dp).clip(CircleShape).background(BlueUEB.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ChatBubbleOutline, null, tint = BlueUEB, modifier = Modifier.size(38.dp))
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    error ?: if (esPropietario) "Aún no tienes mensajes" else "Aún no has escrito a nadie",
                    fontWeight = FontWeight.Bold, fontSize = 19.sp, color = if (error != null) RedUEB else TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (esPropietario) "Cuando un estudiante te escriba desde tu anuncio, la conversación aparecerá aquí."
                    else "Abre un anuncio y toca “Enviar mensaje” para hablar con el propietario.",
                    fontSize = 14.sp, lineHeight = 21.sp, color = TextSecondary, textAlign = TextAlign.Center
                )
            }
            else -> LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(lista, key = { it.id }) { c ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onAbrirConversacion(c.id, c.titulo) }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(48.dp).clip(CircleShape).background(BlueUEB),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                c.conQuien.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() },
                                color = Color.White, fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.conQuien, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(c.titulo, fontSize = 12.sp, color = RedUEB, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                (if (c.ultimoEsMio == true) "Tú: " else "") + (c.ultimoTexto ?: ""),
                                fontSize = 13.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                fontWeight = if (c.noLeidos > 0) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(horaCorta(c.actualizado), fontSize = 12.sp, color = TextSecondary)
                            Spacer(Modifier.height(6.dp))
                            if (c.noLeidos > 0) {
                                Box(Modifier.clip(CircleShape).background(RedUEB).padding(horizontal = 7.dp, vertical = 2.dp)) {
                                    Text("${c.noLeidos}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = DividerColor, modifier = Modifier.padding(start = 82.dp))
                }
            }
        }
    }
}

/** Conversación abierta. Pide los mensajes nuevos cada 4 s. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(conversacionId: Long, titulo: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mensajes = remember { mutableStateListOf<ChatMensaje>() }
    var texto by remember { mutableStateOf("") }
    var enviando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    suspend fun traerNuevos() {
        val auth = Session.bearer(context) ?: return
        try {
            val r = ApiClient.service.getMensajes(auth, conversacionId, mensajes.lastOrNull()?.id ?: 0)
            if (r.isSuccessful) {
                val nuevos = r.body()?.mensajes.orEmpty().filter { n -> mensajes.none { it.id == n.id } }
                if (nuevos.isNotEmpty()) mensajes.addAll(nuevos)
                error = null
            } else error = ApiClient.errorMessage(r.errorBody()?.string(), "No se pudo cargar la conversación")
        } catch (e: Exception) {
            error = "Sin conexión con el servidor UniHogar"
        }
    }

    LaunchedEffect(conversacionId) {
        while (true) {
            traerNuevos()
            delay(4_000)
        }
    }
    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) listState.animateScrollToItem(mensajes.lastIndex)
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BlueUEB, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = BlueUEB)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(color = Color.White, shadowElevation = 8.dp) {
                Column(Modifier.navigationBarsPadding().imePadding()) {
                    error?.let { Text(it, color = RedUEB, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 8.dp)) }
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = texto,
                            onValueChange = { if (it.length <= 1000) texto = it },
                            placeholder = { Text("Escribe un mensaje") },
                            maxLines = 4,
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        FilledIconButton(
                            enabled = texto.isNotBlank() && !enviando,
                            onClick = {
                                val auth = Session.bearer(context) ?: return@FilledIconButton
                                val envio = texto.trim()
                                enviando = true
                                scope.launch {
                                    try {
                                        val r = ApiClient.service.enviarMensaje(auth, conversacionId, TextoRequest(envio))
                                        if (r.isSuccessful) {
                                            texto = ""
                                            r.body()?.let { m -> if (mensajes.none { it.id == m.id }) mensajes.add(m) }
                                        } else error = ApiClient.errorMessage(r.errorBody()?.string(), "No se pudo enviar")
                                    } catch (e: Exception) {
                                        error = "Sin conexión: el mensaje no se envió"
                                    } finally {
                                        enviando = false
                                    }
                                }
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = RedUEB),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Color.White)
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (mensajes.isEmpty()) {
                item {
                    Text(
                        "Escribe tu primer mensaje al propietario. Por ejemplo: si sigue disponible o cuándo puedes visitarlo.",
                        fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(24.dp)
                    )
                }
            }
            items(mensajes, key = { it.id }) { m ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.mio) Arrangement.End else Arrangement.Start) {
                    Column(
                        Modifier
                            .widthIn(max = 290.dp)
                            .clip(RoundedCornerShape(
                                topStart = 18.dp, topEnd = 18.dp,
                                bottomStart = if (m.mio) 18.dp else 4.dp, bottomEnd = if (m.mio) 4.dp else 18.dp
                            ))
                            .background(if (m.mio) BlueUEB else Color.White)
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Text(m.texto, color = if (m.mio) Color.White else TextPrimary, fontSize = 15.sp, lineHeight = 20.sp)
                        Text(
                            horaCorta(m.creado), fontSize = 10.sp,
                            color = if (m.mio) Color.White.copy(alpha = 0.7f) else TextSecondary,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}
