package com.example.unihogarguaranda.ui.login

/**
 * Roles de usuario en el ecosistema UniHogar Guaranda.
 */
enum class UserRole(val label: String) {
    ESTUDIANTE("Estudiante"),
    PROPIETARIO("Propietario")
}

/**
 * Estado del formulario de Registro e Inicio de Sesión de Usuario.
 * Flujo desacoplado:
 * - Fase 1 (Login/Registro): Registro del usuario; el propietario indica su cédula (validada con Módulo 10).
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

    // Recuperación de Acceso (RF02)
    val showForgotPasswordDialog: Boolean = false,
    val forgotPasswordEmail: String = "",
    val forgotPasswordMessage: String? = null
)
