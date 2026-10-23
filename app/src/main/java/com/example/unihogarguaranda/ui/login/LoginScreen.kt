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
 * Pantalla de Acceso y Registro de Usuario (Fase 1: Persona Real).
 * Aquí solo se registra y valida a la persona:
 * 1. Estudiante: Valida correo @ueb.edu.ec.
 * 2. Propietario: Valida su cédula con el Registro Civil y su identidad con prueba facial.
 * (El registro de propiedades con GPS, clave catastral y títulos se realiza en su propio panel).
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: (UserRole, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    if (uiState.isSuccess) {
        onLoginSuccess(uiState.role, uiState.email)
    }

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
                        text = "Ingrese su correo registrado para recibir un enlace temporal de recuperación.",
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

            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Logo UniHogar",
                modifier = Modifier
                    .size(95.dp)
                    .clip(RoundedCornerShape(24.dp))
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Uni", fontSize = 28.sp, fontWeight = FontWeight.Black, color = BlueUEB)
                    Text(text = "Hogar", fontSize = 28.sp, fontWeight = FontWeight.Black, color = RedUEB)
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
                        if (uiState.role == UserRole.PROPIETARIO) "Registro y Validación de Arrendador" else "Registro de Estudiante"
                    } else "Tu espacio cerca de la UEB",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

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

            val helperText = if (uiState.role == UserRole.ESTUDIANTE) {
                if (uiState.isRegisterMode) "Registro con correo institucional @ueb.edu.ec" else "Acceso institucional (@ueb.edu.ec)"
            } else {
                if (uiState.isRegisterMode) "Paso 1: Validación de Identidad Civil y Biometría" else "Acceso para propietarios acreditados"
            }

            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = BlueUEB,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            // Formulario de Registro
            if (uiState.isRegisterMode) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.onNameChanged(it) },
                    label = { Text("Nombre completo") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = BlueUEB, modifier = Modifier.size(18.dp))
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

                // Validación de la Persona Propietaria (Fase 1)
                if (uiState.role == UserRole.PROPIETARIO) {

                    // Cédula y Registro Civil
                    OutlinedTextField(
                        value = uiState.identificationNumber,
                        onValueChange = { viewModel.onIdentificationChanged(it) },
                        label = { Text("Cédula de identidad (10 dígitos)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = BlueUEB, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (uiState.isConsultandoRegistroCivil) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = BlueUEB)
                            } else if (uiState.isCedulaValid) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Cédula Válida", tint = Color(0xFF10B981))
                            }
                        },
                        supportingText = {
                            if (uiState.identificationNumber.length == 10) {
                                if (uiState.registroCivilNombres != null) {
                                    Text(
                                        text = "Registro Civil: ${uiState.registroCivilNombres} (ACTIVO)",
                                        color = Color(0xFF065F46),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = if (uiState.isCedulaValid) "Algoritmo Módulo 10 válido" else "Cédula no válida en Registro Civil",
                                        color = if (uiState.isCedulaValid) Color(0xFF10B981) else RedUEB,
                                        fontSize = 11.sp
                                    )
                                }
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

                    // Prueba Biométrica KYC (Selfie con Cédula al lado del rostro)
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = if (uiState.isBiometricApproved) Color(0xFFD1FAE5) else Color.White),
                        border = BorderStroke(1.dp, if (uiState.isBiometricApproved) Color(0xFF10B981) else DividerColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = null,
                                        tint = if (uiState.isBiometricApproved) Color(0xFF065F46) else BlueUEB,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Prueba de Vida y Biometría Facial",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (uiState.isBiometricApproved) Color(0xFF065F46) else TextPrimary
                                    )
                                }
                                if (uiState.isBiometricApproved) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF10B981)) {
                                        Text("96% MATCH", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sostenga su cédula al lado de su rostro frente a la cámara para autenticar su identidad.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.performBiometricScan() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (uiState.isBiometricApproved) Color(0xFF047857) else BlueUEB
                                )
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (uiState.isBiometricApproved) "Identidad Biométrica Aprobada" else "Escanear Rostro y Cédula (Cámara)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.phone,
                    onValueChange = { viewModel.onPhoneChanged(it) },
                    label = { Text("Teléfono / Celular") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = BlueUEB, modifier = Modifier.size(18.dp))
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

            // Correo
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChanged(it) },
                label = { Text(if (uiState.role == UserRole.ESTUDIANTE) "Correo institucional UEB" else "Correo electrónico personal") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = BlueUEB, modifier = Modifier.size(18.dp))
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            // Contraseña
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChanged(it) },
                label = { Text("Contraseña") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = BlueUEB, modifier = Modifier.size(18.dp))
                },
                singleLine = true,
                visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { viewModel.togglePasswordVisibility() }) {
                        Text(text = if (uiState.isPasswordVisible) "Ocultar" else "Ver", color = BlueUEB, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
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

            // Confirmar contraseña
            if (uiState.isRegisterMode) {
                OutlinedTextField(
                    value = uiState.confirmPassword,
                    onValueChange = { viewModel.onConfirmPasswordChanged(it) },
                    label = { Text("Confirmar contraseña") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = BlueUEB, modifier = Modifier.size(18.dp))
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.submit()
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
            }

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
                colors = ButtonDefaults.buttonColors(containerColor = RedUEB, contentColor = Color.White)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (uiState.isRegisterMode) {
                            if (uiState.role == UserRole.PROPIETARIO) "Registrar y Validar Identidad" else "Crear Cuenta Estudiantil"
                        } else "Comenzar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!uiState.isRegisterMode) {
                TextButton(onClick = { viewModel.openForgotPasswordDialog() }) {
                    Text(text = "¿Olvidaste tu contraseña?", fontSize = 13.sp, color = BlueUEB, fontWeight = FontWeight.Medium)
                }
            }

            TextButton(onClick = { viewModel.toggleAuthMode() }) {
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
