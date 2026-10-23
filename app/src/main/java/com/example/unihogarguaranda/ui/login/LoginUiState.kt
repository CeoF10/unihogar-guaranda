package com.example.unihogarguaranda.ui.login

enum class UserRole(val label: String) {
    ESTUDIANTE("Estudiante"),
    PROPIETARIO("Propietario")
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val role: UserRole = UserRole.ESTUDIANTE,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isSuccess: Boolean = false,

    // Modo Registro (RF01)
    val isRegisterMode: Boolean = false,
    val name: String = "",
    val phone: String = "",
    val confirmPassword: String = "",

    // Requisitos adicionales para Propietario / Arrendador
    val identificationNumber: String = "", // Cédula de identidad (10 dígitos)
    val ownerAddress: String = "", // Dirección domiciliaria o sector
    val isOwnershipDeclared: Boolean = false, // Declaración de legitimidad

    // Recuperación de Acceso (RF02)
    val showForgotPasswordDialog: Boolean = false,
    val forgotPasswordEmail: String = "",
    val forgotPasswordMessage: String? = null
)
