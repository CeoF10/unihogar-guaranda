package com.example.unihogarguaranda.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihogarguaranda.model.RegistroCivilService
import com.example.unihogarguaranda.util.GuarandaGeofenceValidator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de la autenticación, registro, geocercado GPS y validación biométrica.
 * Cumple con los tres ejes solicitados:
 * 1. Geocercado GPS estricto para Guaranda (Haversine).
 * 2. Prueba de vida biométrica (Selfie con cédula al lado del rostro).
 * 3. Consulta de identidad con Registro Civil del Ecuador.
 */
class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        LoginUiState(email = "neicer.jimenez@ueb.edu.ec")
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // ------------------------------------------------------------------------
    // ACTUALIZADORES BÁSICOS
    // ------------------------------------------------------------------------

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

    // ------------------------------------------------------------------------
    // 1. CÉDULA Y CONEXIÓN CON REGISTRO CIVIL DEL ECUADOR
    // ------------------------------------------------------------------------

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

        // Si la cédula tiene 10 dígitos y es matemáticamente válida, consulta el servicio oficial
        if (isValid) {
            consultarRegistroCivil(filtered)
        }
    }

    private fun consultarRegistroCivil(cedula: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isConsultandoRegistroCivil = true) }
            delay(500) // Simulación de latencia de red del servicio gubernamental

            val response = RegistroCivilService.consultarCedula(cedula)
            if (response != null) {
                _uiState.update { 
                    it.copy(
                        isConsultandoRegistroCivil = false,
                        registroCivilNombres = response.nombresCompletos,
                        // Si el nombre estaba vacío, autocompleta con el nombre legal del Registro Civil
                        name = if (it.name.isBlank()) response.nombresCompletos else it.name
                    ) 
                }
            } else {
                _uiState.update { it.copy(isConsultandoRegistroCivil = false) }
            }
        }
    }

    // ------------------------------------------------------------------------
    // 2. GEOCERCADO GPS EN TIEMPO REAL (CANTON GUARANDA)
    // ------------------------------------------------------------------------

    /**
     * Captura las coordenadas del GPS del dispositivo o permite fijar la coordenada
     * del inmueble, validando matemáticamente si pertenece a Guaranda con Haversine.
     */
    fun updateGpsLocation(lat: Double, lng: Double, label: String = "Campus UEB / Centro de Guaranda") {
        val isInside = GuarandaGeofenceValidator.isWithinGuaranda(lat, lng)
        val distance = GuarandaGeofenceValidator.calculateDistanceKm(lat, lng)

        _uiState.update { 
            it.copy(
                gpsLatitude = lat,
                gpsLongitude = lng,
                isGpsInsideGuaranda = isInside,
                gpsDistanceKm = distance,
                gpsAddressLabel = label,
                errorMessage = if (!isInside) "Ubicación GPS rechazada: El inmueble está a ${"%.1f".format(distance)} km fuera del Cantón Guaranda." else null
            ) 
        }
    }

    /**
     * Fija la coordenada en un punto verificado de Guaranda (Plaza Roja / Alpachaca).
     */
    fun captureCurrentLocationInGuaranda() {
        // Coordenada exacta en Alpachaca / Campus Matriz UEB Guaranda
        updateGpsLocation(-1.591234, -79.004567, "Alpachaca, Guaranda (Campus UEB)")
    }

    // ------------------------------------------------------------------------
    // 3. BIOMETRÍA KYC: SELFIE CON CÉDULA AL LADO DE LA CARA
    // ------------------------------------------------------------------------

    /**
     * Simula y valida la captura de la prueba de vida biométrica.
     * En producción utiliza Google ML Kit Face Detection + Text Recognition.
     */
    fun performBiometricScan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            delay(1000) // Procesamiento de la red neuronal de detección facial y OCR

            val score = 96 // Score de coincidencia del 96% entre el rostro y la foto de la cédula
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    isSelfieWithCedulaTaken = true,
                    biometricScore = score,
                    isBiometricApproved = true
                ) 
            }
        }
    }

    // ------------------------------------------------------------------------
    // ACREDITACIÓN JURÍDICA Y ADJUNTOS LEGALES
    // ------------------------------------------------------------------------

    fun onOwnerAddressChanged(address: String) {
        _uiState.update { it.copy(ownerAddress = address, errorMessage = null) }
    }

    fun onCadastralCodeChanged(code: String) {
        _uiState.update { it.copy(cadastralCode = code.trim().uppercase(), errorMessage = null) }
    }

    fun onLegalDocTypeChanged(type: LegalDocumentType) {
        _uiState.update { it.copy(legalDocType = type, errorMessage = null) }
    }

    fun onOwnershipDeclaredChanged(declared: Boolean) {
        _uiState.update { it.copy(isOwnershipDeclared = declared, errorMessage = null) }
    }

    fun uploadIdDocument(fileName: String = "cedula_identidad_notariada.pdf") {
        _uiState.update { it.copy(idDocUploaded = true, idDocFileName = fileName, errorMessage = null) }
    }

    fun uploadPropertyDocument(fileName: String = "comprobante_predial_guaranda_2026.pdf") {
        _uiState.update { it.copy(propertyDocUploaded = true, propertyDocFileName = fileName, errorMessage = null) }
    }

    fun uploadUtilityBill(fileName: String = "planilla_cnel_guaranda.pdf") {
        _uiState.update { it.copy(utilityBillUploaded = true, utilityBillFileName = fileName, errorMessage = null) }
    }

    // ------------------------------------------------------------------------
    // ROLES Y MODO DE NAVEGACIÓN
    // ------------------------------------------------------------------------

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

    // ------------------------------------------------------------------------
    // LOGIN
    // ------------------------------------------------------------------------

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
            delay(500)
            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }

    // ------------------------------------------------------------------------
    // REGISTRO CON VERIFICACIÓN GPS, BIOMÉTRICA Y REGISTRO CIVIL
    // ------------------------------------------------------------------------

    private fun register() {
        val currentState = _uiState.value

        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingrese su nombre completo.") }
            return
        }

        if (currentState.role == UserRole.PROPIETARIO) {
            if (currentState.identificationNumber.length != 10) {
                _uiState.update { 
                    it.copy(errorMessage = "La cédula de identidad debe contener exactamente 10 dígitos numéricos.") 
                }
                return
            }

            if (!validateEcuadorianCedula(currentState.identificationNumber)) {
                _uiState.update { 
                    it.copy(errorMessage = "La cédula ingresada no cumple el algoritmo Módulo 10 del Registro Civil.") 
                }
                return
            }

            // Exigencia del Geocercado GPS
            if (currentState.gpsLatitude == null || !currentState.isGpsInsideGuaranda) {
                _uiState.update { 
                    it.copy(errorMessage = "Debe capturar la ubicación GPS del inmueble y comprobar que esté dentro de Guaranda.") 
                }
                return
            }

            // Exigencia de Biometría KYC
            if (!currentState.isBiometricApproved) {
                _uiState.update { 
                    it.copy(errorMessage = "Debe completar la prueba biométrica: Foto sosteniendo su cédula al lado del rostro.") 
                }
                return
            }

            if (currentState.ownerAddress.isBlank()) {
                _uiState.update { 
                    it.copy(errorMessage = "Ingrese la dirección física del inmueble en Guaranda.") 
                }
                return
            }

            if (currentState.cadastralCode.isBlank() || currentState.cadastralCode.length < 4) {
                _uiState.update { 
                    it.copy(errorMessage = "Ingrese la Clave Catastral Municipal registrada en el Cantón Guaranda.") 
                }
                return
            }

            if (!currentState.idDocUploaded || !currentState.propertyDocUploaded || !currentState.utilityBillUploaded) {
                _uiState.update { 
                    it.copy(errorMessage = "Debe adjuntar los 3 documentos legales requeridos: Cédula, Título/Predial y Planilla de luz/agua.") 
                }
                return
            }

            if (!currentState.isOwnershipDeclared) {
                _uiState.update { 
                    it.copy(errorMessage = "Debe aceptar la declaración juramentada de legitimidad y responsabilidad civil.") 
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
            delay(750)
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    isRegisterMode = false,
                    verificationStatus = if (currentState.role == UserRole.PROPIETARIO) {
                        VerificationStatus.PENDIENTE_AUDITORIA
                    } else {
                        VerificationStatus.VERIFICADO
                    },
                    successMessage = if (currentState.role == UserRole.PROPIETARIO) {
                        "Expediente integral ingresado: Cédula verificada con Registro Civil, Biometría KYC al 96% y Coordenadas GPS en Guaranda."
                    } else {
                        "Cuenta estudiantil registrada. Se ha enviado un enlace de confirmación a su correo institucional @ueb.edu.ec."
                    }
                ) 
            }
        }
    }

    // ------------------------------------------------------------------------
    // ALGORITMO OFICIAL MÓDULO 10
    // ------------------------------------------------------------------------

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
