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
    val isSuccess: Boolean = false
)
