package com.example.unihogarguaranda.ui.login

/**
 * Roles de usuario en el ecosistema UniHogar Guaranda.
 */
enum class UserRole(val label: String) {
    ESTUDIANTE("Estudiante"),
    PROPIETARIO("Propietario")
}

/**
 * Estados de verificación de la identidad del Propietario.
 */
enum class OwnerIdentityStatus(val label: String) {
    PENDIENTE("Validación de Identidad Pendiente"),
    VERIFICADO("Identidad Civil y Biométrica Verificada"),
    RECHAZADO("Identidad No Coincidente")
}

/**
 * Estado del formulario de Registro e Inicio de Sesión de Usuario.
 * Flujo desacoplado:
 * - Fase 1 (Login/Registro): Registro del usuario, validación con Registro Civil y Biometría facial.
 * - Fase 2 (Post-Login): El registro del inmueble y los documentos prediales/GPS se realizan por pasos en su propio panel.
 */
data class LoginUiState(
    // Credenciales de acceso
    val email: String = "",
    val password: String = "",
    val role: UserRole = UserRole.ESTUDIANTE,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isSuccess: Boolean = false,

    // Modo Registro Básico (RF01)
    val isRegisterMode: Boolean = false,
    val name: String = "",
    val phone: String = "",
    val confirmPassword: String = "",

    // ========================================================================
    // VALIDACIÓN DE IDENTIDAD DEL PROPIETARIO (FASE 1: PERSONA REAL)
    // ========================================================================
    val identificationNumber: String = "", // Cédula ecuatoriana 10 dígitos
    val isCedulaValid: Boolean = false,
    val registroCivilNombres: String? = null,
    val isConsultandoRegistroCivil: Boolean = false,

    // Prueba de vida biométrica (Rostro + Cédula)
    val isBiometricApproved: Boolean = false,
    val biometricScore: Int = 0,

    val identityStatus: OwnerIdentityStatus = OwnerIdentityStatus.PENDIENTE,

    // Recuperación de Acceso (RF02)
    val showForgotPasswordDialog: Boolean = false,
    val forgotPasswordEmail: String = "",
    val forgotPasswordMessage: String? = null
)
