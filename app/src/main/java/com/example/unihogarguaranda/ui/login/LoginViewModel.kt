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

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email.trim(), errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onRoleChanged(role: UserRole) {
        _uiState.update { 
            it.copy(
                role = role, 
                errorMessage = null 
            ) 
        }
    }

    fun resetState() {
        _uiState.update { LoginUiState() }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login() {
        val currentState = _uiState.value

        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingrese su correo electrónico.") }
            return
        }

        // Validación funcional según credenciales registradas
        if (currentState.role == UserRole.ESTUDIANTE) {
            if (!currentState.email.equals("neicer.jimenez@ueb.edu.ec", ignoreCase = true)) {
                _uiState.update { it.copy(errorMessage = "Correo no registrado o no institucional. Usa: neicer.jimenez@ueb.edu.ec") }
                return
            }
        } else if (currentState.role == UserRole.PROPIETARIO) {
            if (!currentState.email.equals("neycerjimenezf10@gmail.com", ignoreCase = true)) {
                _uiState.update { it.copy(errorMessage = "Correo de propietario no registrado. Usa: neycerjimenezf10@gmail.com") }
                return
            }
        }

        if (currentState.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            delay(800)
            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }
}
