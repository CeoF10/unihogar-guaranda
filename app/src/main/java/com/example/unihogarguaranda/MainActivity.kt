package com.example.unihogarguaranda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihogarguaranda.ui.home.OwnerHomeScreen
import com.example.unihogarguaranda.ui.home.StudentHomeScreen
import com.example.unihogarguaranda.data.Session
import com.example.unihogarguaranda.ui.login.LoginScreen
import com.example.unihogarguaranda.ui.login.LoginViewModel
import com.example.unihogarguaranda.ui.login.UserRole
import com.example.unihogarguaranda.ui.login.WelcomeScreen
import com.example.unihogarguaranda.ui.theme.UniHogarGuarandaTheme

/**
 * Actividad Principal (Punto de entrada de la aplicación UniHogar Guaranda).
 * 
 * Arquitectura y Principios de Diseño:
 * - Single Activity Architecture con Jetpack Compose (100% UI declarativa).
 * - Control de navegación reactiva basado en estado inmutable (MVI / State-driven navigation).
 * - Orquestación de sesiones según rol: Estudiante UEB vs. Arrendador / Propietario Acreditado.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Habilitar visualización edge-to-edge con diseño inmersivo
        enableEdgeToEdge()
        
        setContent {
            UniHogarGuarandaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Estado de sesión del usuario autenticado (Rol y Correo electrónico)
                    // Si es null -> muestra la pantalla de Login/Registro.
                    // Si contiene datos -> enruta al dashboard respectivo.
                    var loggedInUser by remember { mutableStateOf<Pair<UserRole, String>?>(null) }
                    var showWelcome by remember { mutableStateOf(true) }
                    
                    // Instancia compartida del ViewModel de autenticación
                    val loginViewModel: LoginViewModel = viewModel()

                    if (loggedInUser == null && showWelcome) {
                        // Flujo 0: Bienvenida con foto del campus UEB
                        WelcomeScreen(
                            onStart = {
                                // "Comenzar" abre el registro de cuenta nueva
                                if (!loginViewModel.uiState.value.isRegisterMode) loginViewModel.toggleAuthMode()
                                showWelcome = false
                            },
                            onHaveAccount = { showWelcome = false }
                        )
                    } else if (loggedInUser == null) {
                        // Flujo 1: Pantalla de Acceso Institucional y Acreditación Jurídica
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = { role, email ->
                                loggedInUser = Pair(role, email)
                            }
                        )
                    } else {
                        val (role, email) = loggedInUser!!
                        
                        // Flujo 2: Enrutamiento según perfil del usuario autenticado
                        if (role == UserRole.ESTUDIANTE) {
                            // Dashboard de Estudiante: Catálogo de viviendas verificadas en Guaranda
                            StudentHomeScreen(
                                studentEmail = email,
                                onLogout = {
                                    Session.clear(this@MainActivity)
                                    loginViewModel.resetState()
                                    loggedInUser = null
                                }
                            )
                        } else {
                            // Dashboard de Propietario: Estado de acreditación legal y gestión de anuncios
                            OwnerHomeScreen(
                                ownerEmail = email,
                                onLogout = {
                                    Session.clear(this@MainActivity)
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
}
