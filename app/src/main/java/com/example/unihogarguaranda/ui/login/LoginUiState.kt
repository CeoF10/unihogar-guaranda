package com.example.unihogarguaranda.ui.login

/**
 * Roles de usuario en el ecosistema UniHogar Guaranda.
 * - ESTUDIANTE: Requiere validacion de correo institucional (@ueb.edu.ec).
 * - PROPIETARIO: Requiere acreditacion juridica de identidad y titularidad del inmueble.
 */
enum class UserRole(val label: String) {
    ESTUDIANTE("Estudiante"),
    PROPIETARIO("Propietario")
}

/**
 * Tipos de documentos legales admitidos para acreditar propiedad en el Cantón Guaranda.
 * Cumple con la normativa civil y municipal de arrendamiento de viviendas para estudiantes.
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
 * Centraliza las validaciones de acceso institucional y acreditación legal.
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
    // MODULO DE ACREDITACION Y SEGURIDAD JURIDICA (PROPIETARIO / ARRENDADOR)
    // ========================================================================
    // 1. Identificación ciudadana validada mediante algoritmo Módulo 10
    val identificationNumber: String = "", // Cédula ecuatoriana de 10 dígitos
    val isCedulaValid: Boolean = false,

    // 2. Ubicación física real en Guaranda
    val ownerAddress: String = "", // Dirección o barrio en el Cantón Guaranda

    // 3. Clave catastral asignada por el GAD Municipal de Guaranda
    val cadastralCode: String = "", // Ej: 02-01-05-12-004

    // 4. Tipo de documento probatorio seleccionado
    val legalDocType: LegalDocumentType = LegalDocumentType.PREDIAL_MUNICIPAL,

    // 5. Carga de evidencias documentales obligatorias
    val idDocUploaded: Boolean = false,
    val idDocFileName: String? = null,

    val propertyDocUploaded: Boolean = false,
    val propertyDocFileName: String? = null,

    val utilityBillUploaded: Boolean = false,
    val utilityBillFileName: String? = null,

    // 6. Declaración juramentada de titularidad y responsabilidad legal
    val isOwnershipDeclared: Boolean = false,

    // 7. Estado resultante de la auditoría jurídica
    val verificationStatus: VerificationStatus = VerificationStatus.PENDIENTE_AUDITORIA,

    // Recuperación de Acceso (RF02)
    val showForgotPasswordDialog: Boolean = false,
    val forgotPasswordEmail: String = "",
    val forgotPasswordMessage: String? = null
)
