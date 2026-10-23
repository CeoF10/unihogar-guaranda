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
        _uiState.update { it.copy(email = email.trim(), errorMessage = null, successMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null, successMessage = null) }
    }

    fun onConfirmPasswordChanged(password: String) {
        _uiState.update { it.copy(confirmPassword = password, errorMessage = null) }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, errorMessage = null) }
    }

    fun onPhoneChanged(phone: String) {
        _uiState.update { it.copy(phone = phone, errorMessage = null) }
    }

    fun onRoleChanged(role: UserRole) {
        val suggestedEmail = if (role == UserRole.ESTUDIANTE) {
            "neicer.jimenez@ueb.edu.ec"
        } else {
            "neycerjimenezf10@gmail.com"
        }
        _uiState.update { 
            it.copy(
                role = role, 
                email = suggestedEmail,
                errorMessage = null,
                successMessage = null
            ) 
        }
    }

    fun toggleAuthMode() {
        _uiState.update {
            it.copy(
                isRegisterMode = !it.isRegisterMode,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun openForgotPasswordDialog() {
        _uiState.update { 
            it.copy(
                showForgotPasswordDialog = true, 
                forgotPasswordEmail = it.email,
                forgotPasswordMessage = null
            ) 
        }
    }

    fun closeForgotPasswordDialog() {
        _uiState.update { it.copy(showForgotPasswordDialog = false, forgotPasswordMessage = null) }
    }

    fun onForgotPasswordEmailChanged(email: String) {
        _uiState.update { it.copy(forgotPasswordEmail = email.trim(), forgotPasswordMessage = null) }
    }

    fun submitForgotPassword() {
        val email = _uiState.value.forgotPasswordEmail
        if (email.isBlank() || !email.contains("@")) {
            _uiState.update { it.copy(forgotPasswordMessage = "Ingrese un correo electrónico válido.") }
            return
        }

        _uiState.update { 
            it.copy(
                forgotPasswordMessage = "Enlace temporal de un solo uso enviado a: $email. Revise su bandeja de entrada."
            ) 
        }
    }

    fun resetState() {
        _uiState.update { 
            LoginUiState(email = "neicer.jimenez@ueb.edu.ec") 
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isRegisterMode) {
            register()
        } else {
            login()
        }
    }

    private fun login() {
        val currentState = _uiState.value

        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingrese su correo electrónico.") }
            return
        }

        if (currentState.role == UserRole.ESTUDIANTE) {
            if (!currentState.email.endsWith("@ueb.edu.ec", ignoreCase = true)) {
                _uiState.update { 
                    it.copy(errorMessage = "El rol estudiante requiere correo institucional (@ueb.edu.ec).") 
                }
                return
            }
        } else {
            if (!currentState.email.contains("@") || !currentState.email.contains(".")) {
                _uiState.update { 
                    it.copy(errorMessage = "Ingrese un correo electrónico válido para propietario.") 
                }
                return
            }
        }

        if (currentState.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            delay(700)
            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }

    private fun register() {
        val currentState = _uiState.value

        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingrese su nombre completo.") }
            return
        }

        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingrese su correo electrónico.") }
            return
        }

        if (currentState.role == UserRole.ESTUDIANTE) {
            if (!currentState.email.endsWith("@ueb.edu.ec", ignoreCase = true)) {
                _uiState.update { 
                    it.copy(errorMessage = "El registro de estudiante exige correo @ueb.edu.ec") 
                }
                return
            }
        }

        if (currentState.phone.length < 9) {
            _uiState.update { it.copy(errorMessage = "Ingrese un número de teléfono válido.") }
            return
        }

        if (currentState.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres.") }
            return
        }

        if (currentState.password != currentState.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Las contraseñas no coinciden.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            delay(800)
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    isRegisterMode = false,
                    successMessage = "Cuenta registrada. Se ha enviado el enlace de verificación al correo. Ya puede iniciar sesión."
                ) 
            }
        }
    }
}
