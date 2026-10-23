package com.example.unihogarguaranda.ui.login

/**
 * Roles de usuario en el ecosistema UniHogar Guaranda.
 * - ESTUDIANTE: Requiere validación de correo institucional (@ueb.edu.ec).
 * - PROPIETARIO: Requiere acreditación jurídica, biometría KYC y geocercado GPS en Guaranda.
 */
enum class UserRole(val label: String) {
    ESTUDIANTE("Estudiante"),
    PROPIETARIO("Propietario")
}

/**
 * Tipos de documentos legales admitidos para acreditar propiedad en el Cantón Guaranda.
 */
enum class LegalDocumentType(val label: String, val description: String) {
    PREDIAL_MUNICIPAL(
        "Impuesto Predial GAD Guaranda",
        "Carta de pago o comprobante de catastro municipal actualizado"
    ),
    ESCRITURA_PUBLICA(
        "Escritura Pública Notariada",
        "Título de dominio inscrito en el Registro de la Propiedad de Guaranda"
    ),
    CERTIFICADO_GRAVAMEN(
        "Certificado Registro Propiedad",
        "Certificado histórico de propiedad y gravámenes de Bolívar"
    ),
    PODER_NOTARIAL(
        "Poder Notarial de Administración",
        "Poder especial notariado para administrar y arrendar el inmueble"
    )
}

/**
 * Estados del pipeline de verificación y auditoría jurídica del arrendador.
 */
enum class VerificationStatus(val label: String) {
    SIN_DOCUMENTOS("Sin Documentos"),
    PENDIENTE_AUDITORIA("En Auditoría Jurídica"),
    VERIFICADO("Acreditado Legalmente"),
    RECHAZADO("Observado / Rechazado")
}

/**
 * Estado inmutable de la interfaz de autenticación y registro (MVI / MVVM).
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

    // Modo Registro (RF01)
    val isRegisterMode: Boolean = false,
    val name: String = "",
    val phone: String = "",
    val confirmPassword: String = "",

    // ========================================================================
    // 1. IDENTIFICACIÓN Y CONEXIÓN CON REGISTRO CIVIL DEL ECUADOR
    // ========================================================================
    val identificationNumber: String = "", // Cédula ecuatoriana de 10 dígitos
    val isCedulaValid: Boolean = false,
    val registroCivilNombres: String? = null,
    val isConsultandoRegistroCivil: Boolean = false,

    // ========================================================================
    // 2. GEOCERCADO GPS EN TIEMPO REAL (CANTÓN GUARANDA)
    // ========================================================================
    val gpsLatitude: Double? = null,
    val gpsLongitude: Double? = null,
    val isGpsInsideGuaranda: Boolean = false,
    val gpsDistanceKm: Double? = null,
    val gpsAddressLabel: String? = null,

    // ========================================================================
    // 3. BIOMETRÍA KYC: SELFIE SOSTENIENDO CÉDULA AL LADO DEL ROSTRO
    // ========================================================================
    val isSelfieWithCedulaTaken: Boolean = false,
    val biometricScore: Int = 0, // Porcentaje de similitud facial (0 - 100%)
    val isBiometricApproved: Boolean = false,

    // ========================================================================
    // 4. ACREDITACIÓN JURÍDICA Y EXPEDIENTE DIGITAL
    // ========================================================================
    val ownerAddress: String = "",
    val cadastralCode: String = "",
    val legalDocType: LegalDocumentType = LegalDocumentType.PREDIAL_MUNICIPAL,

    val idDocUploaded: Boolean = false,
    val idDocFileName: String? = null,

    val propertyDocUploaded: Boolean = false,
    val propertyDocFileName: String? = null,

    val utilityBillUploaded: Boolean = false,
    val utilityBillFileName: String? = null,

    val isOwnershipDeclared: Boolean = false,
    val verificationStatus: VerificationStatus = VerificationStatus.PENDIENTE_AUDITORIA,

    // Recuperación de Acceso (RF02)
    val showForgotPasswordDialog: Boolean = false,
    val forgotPasswordEmail: String = "",
    val forgotPasswordMessage: String? = null
)
