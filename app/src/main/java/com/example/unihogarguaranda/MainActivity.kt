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
import com.example.unihogarguaranda.ui.login.LoginScreen
import com.example.unihogarguaranda.ui.login.LoginViewModel
import com.example.unihogarguaranda.ui.login.UserRole
import com.example.unihogarguaranda.ui.theme.UniHogarGuarandaTheme

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
                        if (role == UserRole.ESTUDIANTE) {
                            StudentHomeScreen(
                                studentEmail = email,
                                onLogout = {
                                    loginViewModel.resetState()
                                    loggedInUser = null
                                }
                            )
                        } else {
                            OwnerHomeScreen(
                                ownerEmail = email,
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
}
