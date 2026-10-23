package com.example.unihogarguaranda.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel encargado del flujo de autenticación, registro y verificación legal.
 * Implementa el patrón MVI/MVVM con StateFlow reactivo.
 *
 * Contiene la lógica de negocio para:
 * 1. Acceso institucional para estudiantes con validación de dominio @ueb.edu.ec.
 * 2. Validación legal de propietarios: Algoritmo Módulo 10 de cédula,
 *    acreditación de clave catastral municipal y expedientes probatorios (PDF).
 * 3. Recuperación de contraseñas mediante enlaces temporales de un solo uso.
 */
class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        LoginUiState(email = "neicer.jimenez@ueb.edu.ec")
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // ------------------------------------------------------------------------
    // ACTUALIZADORES DE ENTRADA (MUTADORES REACTIVOS)
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

    /**
     * Procesa la cédula ecuatoriana, limitándola a 10 dígitos y evaluando
     * en tiempo real su validez legal con el algoritmo Módulo 10 del Registro Civil.
     */
    fun onIdentificationChanged(id: String) {
        val filtered = id.filter { it.isDigit() }.take(10)
        val isValid = validateEcuadorianCedula(filtered)
        _uiState.update { 
            it.copy(
                identificationNumber = filtered, 
                isCedulaValid = isValid,
                errorMessage = null 
            ) 
        }
    }

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

    // ------------------------------------------------------------------------
    // GESTIÓN DE EXPEDIENTES Y DOCUMENTOS LEGALES (ADJUNTOS)
    // ------------------------------------------------------------------------

    fun uploadIdDocument(fileName: String = "cedula_identidad_notariada.pdf") {
        _uiState.update { 
            it.copy(
                idDocUploaded = true, 
                idDocFileName = fileName,
                errorMessage = null
            ) 
        }
    }

    fun uploadPropertyDocument(fileName: String = "comprobante_predial_guaranda_2026.pdf") {
        _uiState.update { 
            it.copy(
                propertyDocUploaded = true, 
                propertyDocFileName = fileName,
                errorMessage = null
            ) 
        }
    }

    fun uploadUtilityBill(fileName: String = "planilla_cnel_guaranda.pdf") {
        _uiState.update { 
            it.copy(
                utilityBillUploaded = true, 
                utilityBillFileName = fileName,
                errorMessage = null
            ) 
        }
    }

    // ------------------------------------------------------------------------
    // CONTROL DE ROLES Y MODOS DE NAVEGACION
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

    // ------------------------------------------------------------------------
    // RECUPERACIÓN DE ACCESO INSTITUCIONAL (RF02)
    // ------------------------------------------------------------------------

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
    // LÓGICA DE INICIO DE SESIÓN
    // ------------------------------------------------------------------------

    fun login() {
        val currentState = _uiState.value

        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingrese su correo electrónico.") }
            return
        }

        // Validación de pertenencia a la Universidad Estatal de Bolívar
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
            delay(600)
            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }

    // ------------------------------------------------------------------------
    // LÓGICA DE REGISTRO CON AUDITORÍA JURÍDICA PARA PROPIETARIOS
    // ------------------------------------------------------------------------

    private fun register() {
        val currentState = _uiState.value

        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingrese su nombre completo.") }
            return
        }

        // Reglas estrictas para el Arrendador / Propietario (Prevención de estafas estudiantiles)
        if (currentState.role == UserRole.PROPIETARIO) {
            if (currentState.identificationNumber.length != 10) {
                _uiState.update { 
                    it.copy(errorMessage = "La cédula de identidad debe contener exactamente 10 dígitos numéricos.") 
                }
                return
            }

            if (!validateEcuadorianCedula(currentState.identificationNumber)) {
                _uiState.update { 
                    it.copy(errorMessage = "La cédula ingresada no cumple el algoritmo Módulo 10 del Registro Civil ecuatoriano.") 
                }
                return
            }

            if (currentState.ownerAddress.isBlank()) {
                _uiState.update { 
                    it.copy(errorMessage = "Ingrese la dirección física exacta del inmueble en Guaranda.") 
                }
                return
            }

            if (currentState.cadastralCode.isBlank() || currentState.cadastralCode.length < 4) {
                _uiState.update { 
                    it.copy(errorMessage = "Ingrese la Clave Catastral Municipal registrada en el Cantón Guaranda.") 
                }
                return
            }

            // Exigencia de los 3 soportes documentales probatorios
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
                        "Expediente legal ingresado con éxito. Sus títulos serán auditados por la coordinación antes de habilitar publicaciones."
                    } else {
                        "Cuenta estudiantil registrada. Se ha enviado un enlace de confirmación a su correo institucional @ueb.edu.ec."
                    }
                ) 
            }
        }
    }

    // ------------------------------------------------------------------------
    // ALGORITMO OFICIAL: CÉDULA ECUATORIANA (MÓDULO 10)
    // ------------------------------------------------------------------------

    /**
     * Algoritmo de validación del Registro Civil del Ecuador (Módulo 10).
     * 1. Verifica los 2 dígitos de código de provincia (01 a 24).
     * 2. Verifica que el tercer dígito corresponda a persona natural (< 6).
     * 3. Multiplica los primeros 9 dígitos por coeficientes alternados [2, 1, 2, 1, 2, 1, 2, 1, 2].
     * 4. Si el producto es >= 10, se le resta 9.
     * 5. El residuo restado de la decena superior debe coincidir con el décimo dígito (verificador).
     */
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
