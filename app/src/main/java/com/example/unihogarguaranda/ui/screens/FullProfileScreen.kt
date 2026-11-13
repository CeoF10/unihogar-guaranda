package com.example.unihogarguaranda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.unihogarguaranda.data.Session
import com.example.unihogarguaranda.data.PasswordRequest
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.unihogarguaranda.data.ApiClient
import com.example.unihogarguaranda.data.EmailRequest
import com.example.unihogarguaranda.data.PerfilRequest
import kotlinx.coroutines.launch
import java.io.IOException
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unihogarguaranda.ui.theme.*

data class ProfileOption(val icon: androidx.compose.ui.graphics.vector.ImageVector, val title: String)

val studentOptions = listOf(
    ProfileOption(Icons.Default.Article, "Mis publicaciones"),
    ProfileOption(Icons.Default.Favorite, "Mis favoritos"),
    ProfileOption(Icons.Default.ChatBubbleOutline, "Mensajes"),
    ProfileOption(Icons.Default.DateRange, "Visitas"),
    ProfileOption(Icons.Default.Settings, "Configuración"),
    ProfileOption(Icons.Default.PersonAdd, "Activar otro perfil"),
    ProfileOption(Icons.Default.HelpOutline, "Ayuda"),
    ProfileOption(Icons.Default.DeleteOutline, "Eliminar cuenta")
)

// Opciones exclusivas del propietario
val ownerOnlyOptions = listOf(
    ProfileOption(Icons.Default.Description, "Mi expediente")
)

@Composable
fun FullProfileScreen(
    email: String,
    roleTitle: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    onOptionClick: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val nombre = remember(email) {
        Session.nombre(context)?.takeIf { it.isNotBlank() } ?: email.substringBefore("@")
    }
    val iniciales = remember(nombre) {
        nombre.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    }
    var dialogo by rememberSaveable { mutableStateOf<String?>(null) }
    var mensajeClave by rememberSaveable { mutableStateOf<String?>(null) }
    var claveEliminar by remember { mutableStateOf("") }
    var errorEliminar by remember { mutableStateOf<String?>(null) }
    var eliminando by remember { mutableStateOf(false) }

    val enviarAccion = rememberListingAction()
    var correoPerfil by rememberSaveable { mutableStateOf("") }
    var cedulaPerfil by rememberSaveable { mutableStateOf("") }
    val agregarPropietario = roleTitle == "Estudiante"
    when (dialogo) {
        "Activar otro perfil" -> AlertDialog(
            onDismissRequest = { dialogo = null },
            title = { Text(if (agregarPropietario) "Activar perfil propietario" else "Activar perfil estudiante") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Confirma el segundo correo para usar ambos perfiles con tu cuenta.")
                    OutlinedTextField(
                        value = correoPerfil,
                        onValueChange = { correoPerfil = it.take(120) },
                        label = { Text(if (agregarPropietario) "Correo personal" else "Correo @ueb.edu.ec") },
                        singleLine = true
                    )
                    if (agregarPropietario) {
                        OutlinedTextField(
                            value = cedulaPerfil,
                            onValueChange = { cedulaPerfil = it.filter(Char::isDigit).take(10) },
                            label = { Text("Cédula") },
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = correoPerfil.isNotBlank() && (!agregarPropietario || cedulaPerfil.length == 10),
                    onClick = {
                        val body = PerfilRequest(
                            correoPerfil.trim(), if (agregarPropietario) "PROPIETARIO" else "ESTUDIANTE",
                            cedulaPerfil.takeIf { agregarPropietario }
                        )
                        enviarAccion { auth -> ApiClient.service.habilitarPerfil(auth, body) }
                        cedulaPerfil = ""
                        dialogo = null
                    }
                ) { Text("Enviar confirmación") }
            },
            dismissButton = { TextButton(onClick = { dialogo = null }) { Text("Cancelar") } }
        )
        "Configuración" -> AlertDialog(
            onDismissRequest = { dialogo = null; mensajeClave = null },
            title = { Text("Configuración", fontWeight = FontWeight.Bold, color = BlueUEB) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Cuenta: $email", fontSize = 14.sp)
                    Text("Rol: $roleTitle", fontSize = 14.sp)
                    Text(
                        mensajeClave ?: "Para cambiar su contraseña le enviaremos un enlace temporal a su correo.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        mensajeClave = try {
                            ApiClient.service.recuperarClave(EmailRequest(email)).body()?.mensaje
                                ?: "No se pudo enviar el enlace."
                        } catch (e: IOException) {
                            "Sin conexión con el servidor UniHogar."
                        }
                    }
                }) { Text("Cambiar contraseña") }
            },
            dismissButton = { TextButton(onClick = { dialogo = null; mensajeClave = null }) { Text("Cerrar") } }
        )
        "Eliminar cuenta" -> AlertDialog(
            onDismissRequest = { if (!eliminando) { dialogo = null; claveEliminar = ""; errorEliminar = null } },
            title = { Text("Eliminar cuenta", fontWeight = FontWeight.Bold, color = RedUEB) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Se borrarán para siempre tu cuenta, tus favoritos, tus anuncios con sus fotos y tu expediente. No se puede deshacer.",
                        fontSize = 13.sp, color = TextSecondary
                    )
                    OutlinedTextField(
                        value = claveEliminar,
                        onValueChange = { claveEliminar = it; errorEliminar = null },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    errorEliminar?.let { Text(it, color = RedUEB, fontSize = 12.sp) }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = claveEliminar.isNotEmpty() && !eliminando,
                    onClick = {
                        scope.launch {
                            eliminando = true
                            val bearer = Session.bearer(context)
                            try {
                                val r = if (bearer == null) null
                                else ApiClient.service.eliminarCuenta(bearer, PasswordRequest(claveEliminar))
                                if (r?.isSuccessful == true) {
                                    dialogo = null
                                    android.widget.Toast.makeText(context, "Tu cuenta fue eliminada", android.widget.Toast.LENGTH_LONG).show()
                                    onLogout()
                                } else {
                                    errorEliminar = ApiClient.errorMessage(r?.errorBody()?.string(), "No se pudo eliminar la cuenta.")
                                }
                            } catch (e: IOException) {
                                errorEliminar = "Sin conexión con el servidor UniHogar."
                            } finally {
                                eliminando = false
                            }
                        }
                    }
                ) { Text(if (eliminando) "Eliminando…" else "Eliminar", color = RedUEB, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(enabled = !eliminando, onClick = { dialogo = null; claveEliminar = ""; errorEliminar = null }) { Text("Cancelar") }
            }
        )
        "Ayuda" -> AlertDialog(
            onDismissRequest = { dialogo = null },
            title = { Text("Ayuda", fontWeight = FontWeight.Bold, color = BlueUEB) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Busque por zona o nombre y use los filtros de precio, tipo, servicios y distancia.", fontSize = 13.sp)
                    Text("• Toque el corazón para guardar una vivienda en Favoritos.", fontSize = 13.sp)
                    Text("• Solo se muestran viviendas verificadas por Bienestar Estudiantil UEB.", fontSize = 13.sp)
                    Text("• Propietarios: registren el inmueble, suban fotos y su expediente, y publíquenlo desde Mis publicaciones.", fontSize = 13.sp)
                    Text("¿Algún problema con un anuncio? Escriba a Bienestar Estudiantil de la UEB.", fontSize = 13.sp, color = TextSecondary)
                }
            },
            confirmButton = { TextButton(onClick = { dialogo = null }) { Text("Entendido") } }
        )
    }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(10.dp))
                // Avatar con iniciales/icono
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(BlueUEB),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iniciales, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )
                Text(
                    text = email,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Tarjeta azul del Rol Activo
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BlueUEB,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Rol activo", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text(roleTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Opciones del menú
        // Opciones según el rol: "Mis publicaciones" y "Mi expediente" son solo de propietarios
        val opciones = if (roleTitle == "Propietario") {
            val propias = studentOptions.filter { it.title == "Mis publicaciones" }
            propias + ownerOnlyOptions + studentOptions.filter { it.title !in setOf("Mis publicaciones", "Mis favoritos") }
        } else {
            studentOptions.filter { it.title != "Mis publicaciones" }
        }
        val otroRol = if (agregarPropietario) "ROLE_PROPIETARIO" else "ROLE_ESTUDIANTE"
        items(opciones.filter { it.title != "Activar otro perfil" || otroRol !in Session.roles(context) }) { option ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (option.title in setOf("Ayuda", "Configuración", "Eliminar cuenta", "Activar otro perfil")) dialogo = option.title
                        else onOptionClick(option.title)
                    },
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = option.title,
                            tint = BlueUEB,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = option.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Ir",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Botón Cerrar Sesión
        item {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RedUEB),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(RedUEB))
            ) {
                Text("Cerrar sesión", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
