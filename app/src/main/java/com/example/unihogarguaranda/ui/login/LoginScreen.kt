package com.example.unihogarguaranda.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unihogarguaranda.R
import com.example.unihogarguaranda.ui.theme.*

/**
 * Pantalla principal de Acceso y Registro de UniHogar Guaranda.
 * Implementa la interfaz para:
 * 1. Acceso de estudiantes mediante correo institucional (@ueb.edu.ec).
 * 2. Registro y Acreditación Jurídica de Arrendadores (Cédula ecuatoriana, Clave Catastral,
 *    y carga de documentos notariales/prediales en PDF para auditoría legal).
 * 3. Recuperación de contraseña mediante modal interactivo.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: (UserRole, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // Disparar navegación cuando la autenticación es exitosa
    if (uiState.isSuccess) {
        onLoginSuccess(uiState.role, uiState.email)
    }

    // Modal de Recuperación de Contraseña (RF02)
    if (uiState.showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeForgotPasswordDialog() },
            title = {
                Text(
                    text = "Recuperar Contraseña",
                    fontWeight = FontWeight.Bold,
                    color = BlueUEB,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Ingrese su correo registrado para recibir un enlace temporal de recuperación institucional.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = uiState.forgotPasswordEmail,
                        onValueChange = { viewModel.onForgotPasswordEmailChanged(it) },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    uiState.forgotPasswordMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = if (msg.contains("enviado")) Color(0xFF10B981) else RedUEB,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.submitForgotPassword() },
                    colors = ButtonDefaults.buttonColors(containerColor = RedUEB)
                ) {
                    Text("Enviar enlace")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeForgotPasswordDialog() }) {
                    Text("Cerrar", color = TextSecondary)
                }
            }
        )
    }

    // Contenedor principal con fondo limpio institucional
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Isotipo oficial de la aplicación
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Logo UniHogar",
                modifier = Modifier
                    .size(95.dp)
                    .clip(RoundedCornerShape(24.dp))
            )

            // Identidad corporativa UEB
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Uni",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = BlueUEB
                    )
                    Text(
                        text = "Hogar",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = RedUEB
                    )
                }
                Text(
                    text = "Guaranda",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BlueUEB,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (uiState.isRegisterMode) {
                        if (uiState.role == UserRole.PROPIETARIO) "Registro y Acreditación de Arrendador" else "Registro de Cuenta Estudiantil"
                    } else "Tu espacio cerca de la UEB",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Selector de Rol: Estudiante / Propietario
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(5.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    UserRole.values().forEach { role ->
                        val isSelected = uiState.role == role
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) RedUEB else Color.Transparent)
                                .clickable { viewModel.onRoleChanged(role) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = role.label,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextPrimary
                            )
                        }
                    }
                }
            }

            // Texto guía según rol y modo
            val helperText = if (uiState.role == UserRole.ESTUDIANTE) {
                if (uiState.isRegisterMode) "Registro con validación de correo institucional @ueb.edu.ec" else "Acceso institucional (@ueb.edu.ec)"
            } else {
                if (uiState.isRegisterMode) "Acreditación con Cédula, Clave Catastral y Títulos Legales" else "Acceso para propietarios acreditados"
            }

            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = BlueUEB,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            // Campos del formulario en Modo Registro
            if (uiState.isRegisterMode) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.onNameChanged(it) },
                    label = { Text("Nombre completo") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = BlueUEB,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BlueUEB,
                        unfocusedBorderColor = DividerColor,
                        focusedLabelColor = BlueUEB,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                // ============================================================
                // SECCION DE ACREDITACION LEGAL EXCLUSIVA PARA PROPIETARIOS
                // ============================================================
                if (uiState.role == UserRole.PROPIETARIO) {
                    // 1. Cédula con validador Módulo 10 en tiempo real
                    OutlinedTextField(
                        value = uiState.identificationNumber,
                        onValueChange = { viewModel.onIdentificationChanged(it) },
                        label = { Text("Cédula de identidad (10 dígitos)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = BlueUEB,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (uiState.isCedulaValid) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Cédula Válida",
                                    tint = Color(0xFF10B981)
                                )
                            }
                        },
                        supportingText = {
                            if (uiState.identificationNumber.length == 10) {
                                Text(
                                    text = if (uiState.isCedulaValid) "Cédula ecuatoriana válida (Módulo 10)" else "Cédula no válida según Registro Civil",
                                    color = if (uiState.isCedulaValid) Color(0xFF10B981) else RedUEB,
                                    fontSize = 11.sp
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BlueUEB,
                            unfocusedBorderColor = DividerColor,
                            focusedLabelColor = BlueUEB,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // 2. Dirección física en Guaranda
                    OutlinedTextField(
                        value = uiState.ownerAddress,
                        onValueChange = { viewModel.onOwnerAddressChanged(it) },
                        label = { Text("Dirección del inmueble en Guaranda") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = BlueUEB,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        placeholder = { Text("ej. Av. Guayaquil y Calle Sucre") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BlueUEB,
                            unfocusedBorderColor = DividerColor,
                            focusedLabelColor = BlueUEB,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // 3. Clave Catastral Municipal
                    OutlinedTextField(
                        value = uiState.cadastralCode,
                        onValueChange = { viewModel.onCadastralCodeChanged(it) },
                        label = { Text("Clave Catastral Municipal (GAD Guaranda)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.HomeWork,
                                contentDescription = null,
                                tint = BlueUEB,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        placeholder = { Text("ej. 02-01-05-12-004") },
                        supportingText = {
                            Text("Código predial registrado en el catastro de Guaranda", fontSize = 11.sp, color = TextSecondary)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BlueUEB,
                            unfocusedBorderColor = DividerColor,
                            focusedLabelColor = BlueUEB,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // 4. Selector de Tipo de Documento Legal Acreditante
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Documento acreditante de dominio:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LegalDocumentType.values().forEach { docType ->
                            val isSelected = uiState.legalDocType == docType
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) BlueUEB.copy(alpha = 0.08f) else Color.White,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) BlueUEB else DividerColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { viewModel.onLegalDocTypeChanged(docType) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.onLegalDocTypeChanged(docType) },
                                        colors = RadioButtonDefaults.colors(selectedColor = BlueUEB)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = docType.label,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) BlueUEB else TextPrimary
                                        )
                                        Text(
                                            text = docType.description,
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. Contenedor de Carga de Expediente Digital (PDF)
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, DividerColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Expediente Digital de Verificación",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = BlueUEB
                            )
                            Text(
                                text = "Adjunte los 3 soportes obligatorios en PDF para la auditoría de Bienestar Estudiantil UEB.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )

                            // Adjunto 1: Cédula
                            DocumentUploadItem(
                                title = "1. Cédula de Identidad",
                                isUploaded = uiState.idDocUploaded,
                                fileName = uiState.idDocFileName,
                                onUpload = { viewModel.uploadIdDocument() }
                            )

                            // Adjunto 2: Título de Propiedad / Predial
                            DocumentUploadItem(
                                title = "2. ${uiState.legalDocType.label}",
                                isUploaded = uiState.propertyDocUploaded,
                                fileName = uiState.propertyDocFileName,
                                onUpload = { viewModel.uploadPropertyDocument() }
                            )

                            // Adjunto 3: Planilla de Servicios Básicos
                            DocumentUploadItem(
                                title = "3. Planilla Luz (CNEL) / Agua (EMAPAG)",
                                isUploaded = uiState.utilityBillUploaded,
                                fileName = uiState.utilityBillFileName,
                                onUpload = { viewModel.uploadUtilityBill() }
                            )
                        }
                    }
                }

                // Teléfono de contacto
                OutlinedTextField(
                    value = uiState.phone,
                    onValueChange = { viewModel.onPhoneChanged(it) },
                    label = { Text("Teléfono / Celular de contacto") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = BlueUEB,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BlueUEB,
                        unfocusedBorderColor = DividerColor,
                        focusedLabelColor = BlueUEB,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // Campo de Correo Electrónico
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChanged(it) },
                label = { Text(if (uiState.role == UserRole.ESTUDIANTE) "Correo institucional UEB" else "Correo personal del arrendador") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = BlueUEB,
                        modifier = Modifier.size(18.dp)
                    )
                },
                placeholder = { 
                    Text(if (uiState.role == UserRole.ESTUDIANTE) "ej. neicer.jimenez@ueb.edu.ec" else "ej. neycerjimenezf10@gmail.com") 
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BlueUEB,
                    unfocusedBorderColor = DividerColor,
                    focusedLabelColor = BlueUEB,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            // Campo de Contraseña
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChanged(it) },
                label = { Text("Contraseña") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = BlueUEB,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { viewModel.togglePasswordVisibility() }) {
                        Text(
                            text = if (uiState.isPasswordVisible) "Ocultar" else "Ver",
                            color = BlueUEB,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BlueUEB,
                    unfocusedBorderColor = DividerColor,
                    focusedLabelColor = BlueUEB,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (uiState.isRegisterMode) ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.submit()
                    }
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            // Confirmación de Contraseña (solo en Modo Registro)
            if (uiState.isRegisterMode) {
                OutlinedTextField(
                    value = uiState.confirmPassword,
                    onValueChange = { viewModel.onConfirmPasswordChanged(it) },
                    label = { Text("Confirmar contraseña") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = BlueUEB,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BlueUEB,
                        unfocusedBorderColor = DividerColor,
                        focusedLabelColor = BlueUEB,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.submit()
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                // Declaración Juramentada de Arrendamiento para PROPIETARIOS
                if (uiState.role == UserRole.PROPIETARIO) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onOwnershipDeclaredChanged(!uiState.isOwnershipDeclared) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = uiState.isOwnershipDeclared,
                                onCheckedChange = { viewModel.onOwnershipDeclaredChanged(it) },
                                colors = CheckboxDefaults.colors(checkedColor = RedUEB)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Declaro bajo juramento que los documentos cargados son fidedignos y que ostento legítimo derecho para dar en arriendo este inmueble a estudiantes en el Cantón Guaranda.",
                                fontSize = 12.sp,
                                color = Color(0xFF92400E),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Mensajes informativos de Respuesta (Éxito o Error)
            uiState.successMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFD1FAE5),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        color = Color(0xFF065F46),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = RedUEB,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Botón de Envío
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.submit()
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RedUEB,
                    contentColor = Color.White
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (uiState.isRegisterMode) {
                            if (uiState.role == UserRole.PROPIETARIO) "Ingresar Expediente Legal" else "Crear Cuenta Estudiantil"
                        } else "Comenzar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Enlace de Olvido de Contraseña (solo en modo Login)
            if (!uiState.isRegisterMode) {
                TextButton(
                    onClick = { viewModel.openForgotPasswordDialog() }
                ) {
                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        fontSize = 13.sp,
                        color = BlueUEB,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Alternar entre Inicio de Sesión y Registro
            TextButton(
                onClick = { viewModel.toggleAuthMode() }
            ) {
                Text(
                    text = if (uiState.isRegisterMode) "¿Ya tienes una cuenta? Inicia sesión" else "¿No tienes una cuenta? Regístrate",
                    fontSize = 13.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Componente modular para la carga interactiva de documentos del expediente legal.
 * Muestra el estado del archivo (Pendiente / Adjuntado) con retroalimentación visual clara.
 */
@Composable
fun DocumentUploadItem(
    title: String,
    isUploaded: Boolean,
    fileName: String?,
    onUpload: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isUploaded) Color(0xFFD1FAE5) else Color.White,
        border = BorderStroke(
            1.dp,
            if (isUploaded) Color(0xFF10B981) else DividerColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUpload() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isUploaded) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                    contentDescription = null,
                    tint = if (isUploaded) Color(0xFF065F46) else BlueUEB,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isUploaded) Color(0xFF065F46) else TextPrimary
                    )
                    if (isUploaded && fileName != null) {
                        Text(
                            text = fileName,
                            fontSize = 10.sp,
                            color = Color(0xFF047857)
                        )
                    }
                }
            }
            Text(
                text = if (isUploaded) "Adjuntado" else "Cargar PDF",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUploaded) Color(0xFF065F46) else RedUEB
            )
        }
    }
}
