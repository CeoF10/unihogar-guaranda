package com.example.unihogarguaranda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihogarguaranda.ui.login.LoginScreen
import com.example.unihogarguaranda.ui.login.LoginViewModel
import com.example.unihogarguaranda.ui.login.UserRole
import com.example.unihogarguaranda.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniHogarGuarandaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var loggedInUser by remember { mutableStateOf<Pair<UserRole, String>?>(null) }
                    val loginViewModel: LoginViewModel = viewModel()

                    if (loggedInUser == null) {
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = { role, email ->
                                loggedInUser = Pair(role, email)
                            }
                        )
                    } else {
                        val (role, email) = loggedInUser!!
                        LoginSuccessScreen(
                            userRole = role,
                            email = email,
                            onLogout = {
                                loginViewModel.resetState()
                                loggedInUser = null
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoginSuccessScreen(
    userRole: UserRole,
    email: String,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Uni",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = BlueUEB
                    )
                    Text(
                        text = "Hogar",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = RedUEB
                    )
                }

                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Cerrar sesión",
                        tint = RedUEB
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(BlueUEB),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Text(
                    text = "Bienvenido",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = email,
                    fontSize = 14.sp,
                    color = TextSecondary
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (userRole == UserRole.ESTUDIANTE) BlueUEB.copy(alpha = 0.1f) else RedUEB.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = userRole.label,
                        color = if (userRole == UserRole.ESTUDIANTE) BlueUEB else RedUEB,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = onLogout,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedUEB)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cerrar sesión", fontSize = 14.sp)
                }
            }
        }
    }
}
