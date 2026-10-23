package com.example.unihogarguaranda.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihogarguaranda.model.RegistroCivilService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel enfocado en la Fase 1: Autenticación, Creación de Cuenta y
 * Validación de Identidad del Usuario (Registro Civil y Biometría).
 */
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

    fun onIdentificationChanged(id: String) {
        val filtered = id.filter { it.isDigit() }.take(10)
        val isValid = validateEcuadorianCedula(filtered)

        _uiState.update { 
            it.copy(
                identificationNumber = filtered, 
                isCedulaValid = isValid,
                registroCivilNombres = null,
                errorMessage = null 
            ) 
        }

        if (isValid) {
            consultarRegistroCivil(filtered)
        }
    }

    private fun consultarRegistroCivil(cedula: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isConsultandoRegistroCivil = true) }
            delay(500)

            val response = RegistroCivilService.consultarCedula(cedula)
            if (response != null) {
                _uiState.update { 
                    it.copy(
                        isConsultandoRegistroCivil = false,
                        registroCivilNombres = response.nombresCompletos,
                        name = if (it.name.isBlank()) response.nombresCompletos else it.name
                    ) 
                }
            } else {
                _uiState.update { it.copy(isConsultandoRegistroCivil = false) }
            }
        }
    }

    fun performBiometricScan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            delay(800)

            _uiState.update { 
                it.copy(
                    isLoading = false,
                    isBiometricApproved = true,
                    biometricScore = 96
                ) 
            }
        }
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
                forgotPasswordMessage = "Enlace temporal enviado a: $email. Revise su bandeja de entrada."
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

    fun login() {
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
            delay(400)
            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }

    private fun register() {
        val currentState = _uiState.value

        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingrese su nombre completo.") }
            return
        }

        // Validación exclusiva de identidad en Fase 1
        if (currentState.role == UserRole.PROPIETARIO) {
            if (currentState.identificationNumber.length != 10) {
                _uiState.update { 
                    it.copy(errorMessage = "La cédula de identidad debe contener 10 dígitos.") 
                }
                return
            }

            if (!validateEcuadorianCedula(currentState.identificationNumber)) {
                _uiState.update { 
                    it.copy(errorMessage = "La cédula no cumple el algoritmo Módulo 10 del Registro Civil.") 
                }
                return
            }

            if (!currentState.isBiometricApproved) {
                _uiState.update { 
                    it.copy(errorMessage = "Debe completar la prueba biométrica facial para validar su identidad.") 
                }
                return
            }
        }

        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingrese su correo electrónico.") }
            return
        }

        if (currentState.role == UserRole.ESTUDIANTE) {
            if (!currentState.email.endsWith("@ueb.edu.ec", ignoreCase = true)) {
                _uiState.update { 
                    it.copy(errorMessage = "El registro de estudiante exige correo @ueb.edu.ec.") 
                }
                return
            }
        }

        if (currentState.phone.length < 9) {
            _uiState.update { it.copy(errorMessage = "Ingrese un número de teléfono o celular válido.") }
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
            delay(600)
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    isRegisterMode = false,
                    identityStatus = OwnerIdentityStatus.VERIFICADO,
                    successMessage = if (currentState.role == UserRole.PROPIETARIO) {
                        "Identidad validada con el Registro Civil y Biometría Facial al 96%. Cuenta creada exitosamente. Inicie sesión para registrar sus propiedades."
                    } else {
                        "Cuenta estudiantil registrada. Enlace de confirmación enviado a su correo institucional @ueb.edu.ec."
                    }
                ) 
            }
        }
    }

    private fun validateEcuadorianCedula(cedula: String): Boolean {
        if (cedula.length != 10 || !cedula.all { it.isDigit() }) return false
        val provincia = cedula.substring(0, 2).toIntOrNull() ?: return false
        if (provincia < 1 || provincia > 24) return false

        val tercerDigito = cedula[2].digitToInt()
        if (tercerDigito >= 6) return false

        val coeficientes = intArrayOf(2, 1, 2, 1, 2, 1, 2, 1, 2)
        var suma = 0
        for (i in 0 until 9) {
            var valor = cedula[i].digitToInt() * coeficientes[i]
            if (valor >= 10) valor -= 9
            suma += valor
        }
        val digitoVerificador = if (suma % 10 == 0) 0 else 10 - (suma % 10)
        return digitoVerificador == cedula[9].digitToInt()
    }
}
