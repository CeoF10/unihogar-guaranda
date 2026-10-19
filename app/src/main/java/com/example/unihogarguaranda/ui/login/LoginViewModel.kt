package com.example.unihogarguaranda.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        LoginUiState(email = "neicer.jimenez@ueb.edu.ec")
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email.trim(), errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onRoleChanged(role: UserRole) {
        val defaultEmail = when (role) {
            UserRole.ESTUDIANTE -> "neicer.jimenez@ueb.edu.ec"
            UserRole.PROPIETARIO -> "neycerjimenezf10@gmail.com"
        }
        _uiState.update { 
            it.copy(
                role = role, 
                email = defaultEmail,
                errorMessage = null 
            ) 
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login() {
        val currentState = _uiState.value

        // Validación según la propuesta técnica (RF01 / RF02)
        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingrese su correo electrónico.") }
            return
        }

        if (currentState.role == UserRole.ESTUDIANTE && !currentState.email.endsWith("@ueb.edu.ec", ignoreCase = true)) {
            _uiState.update { it.copy(errorMessage = "El rol de estudiante requiere correo institucional (@ueb.edu.ec).") }
            return
        }

        if (currentState.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            // Simulación de autenticación (posteriormente conectada a Spring Boot / JWT)
            delay(1200)
            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }
}
