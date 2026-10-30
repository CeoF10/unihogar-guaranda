package com.example.unihogarguaranda.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihogarguaranda.data.ApiClient
import com.example.unihogarguaranda.data.EmailRequest
import com.example.unihogarguaranda.data.LoginRequest
import com.example.unihogarguaranda.data.RegisterRequest
import com.example.unihogarguaranda.data.Session
import com.example.unihogarguaranda.util.CedulaValidator
import com.example.unihogarguaranda.model.RegistroCivilService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * ViewModel enfocado en la Fase 1: Autenticación, Creación de Cuenta y
 * Validación de Identidad del Usuario (Registro Civil y Biometría).
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private companion object {
        const val SIN_CONEXION = "No hay conexión con el servidor UniHogar. Verifique su internet."
    }

    private val _uiState = MutableStateFlow(
        LoginUiState()
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
        val isValid = CedulaValidator.esValida(filtered)

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
        // Al cambiar de rol se limpia el correo: cada rol usa un dominio distinto
        _uiState.update { 
            it.copy(
                role = role, 
                email = "",
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

        // RF02: la API responde igual exista o no el correo
        viewModelScope.launch {
            try {
                val response = ApiClient.service.recuperarClave(EmailRequest(email))
                val mensaje = response.body()?.mensaje ?: "No se pudo enviar el enlace."
                _uiState.update { it.copy(forgotPasswordMessage = mensaje) }
            } catch (e: IOException) {
                _uiState.update { it.copy(forgotPasswordMessage = SIN_CONEXION) }
            }
        }
    }

    fun resetState() {
        _uiState.update { 
            LoginUiState() 
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
            try {
                val response = ApiClient.service.login(LoginRequest(currentState.email, currentState.password))
                val body = response.body()
                if (!response.isSuccessful || body == null) {
                    val message = ApiClient.errorMessage(response.errorBody()?.string(), "No se pudo iniciar sesión.")
                    _uiState.update { it.copy(isLoading = false, errorMessage = message) }
                    return@launch
                }

                // La cuenta debe tener el rol con el que se está entrando
                val requiredRole = if (currentState.role == UserRole.ESTUDIANTE) "ROLE_ESTUDIANTE" else "ROLE_PROPIETARIO"
                if (requiredRole !in body.user.roles) {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Esta cuenta no tiene el rol de ${currentState.role.label}.")
                    }
                    return@launch
                }

                Session.save(getApplication(), body.token, body.user)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: IOException) {
                android.util.Log.e("UniHogarAPI", "Login sin conexión", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = SIN_CONEXION
                    )
                }
            }
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

            if (!CedulaValidator.esValida(currentState.identificationNumber)) {
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

        if (currentState.password.length < 8) {
            _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 8 caracteres.") }
            return
        }

        if (currentState.password != currentState.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Las contraseñas no coinciden.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        // RF01: la cuenta se crea en la API; no puede iniciar sesión hasta confirmar el correo
        viewModelScope.launch {
            try {
                val response = ApiClient.service.register(
                    RegisterRequest(
                        nombre = currentState.name.trim(),
                        email = currentState.email,
                        password = currentState.password,
                        rol = if (currentState.role == UserRole.PROPIETARIO) "PROPIETARIO" else "ESTUDIANTE",
                        telefono = currentState.phone,
                        cedula = currentState.identificationNumber.takeIf { currentState.role == UserRole.PROPIETARIO }
                    )
                )
                if (!response.isSuccessful) {
                    val message = ApiClient.errorMessage(response.errorBody()?.string(), "No se pudo crear la cuenta.")
                    _uiState.update { it.copy(isLoading = false, errorMessage = message) }
                    return@launch
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRegisterMode = false,
                        identityStatus = OwnerIdentityStatus.VERIFICADO,
                        successMessage = "Cuenta creada. Revise su correo (${currentState.email}) para confirmarla antes de iniciar sesión."
                    )
                }
            } catch (e: IOException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = SIN_CONEXION) }
            }
        }
    }
}
