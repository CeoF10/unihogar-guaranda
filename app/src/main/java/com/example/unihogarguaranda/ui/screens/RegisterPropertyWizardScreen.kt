package com.example.unihogarguaranda.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unihogarguaranda.ui.theme.*
import com.example.unihogarguaranda.util.GuarandaGeofenceValidator

/**
 * Pantalla de Registro de Propiedad por Pasos (Wizard Stepper).
 * Fase 2: El propietario autenticado y validado registra su inmueble:
 * - Paso 1: Ubicación Física y Geocercado GPS estricto en Guaranda.
 * - Paso 2: Clave Catastral Municipal y Título Legal de Dominio.
 * - Paso 3: Expediente Digital (PDF de escritura/predial y planilla de luz/agua).
 * - Paso 4: Declaración Juramentada y Envío a Auditoría.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPropertyWizardScreen(
    onDismiss: () -> Unit,
    onPropertySubmitted: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }

    // Paso 1: Ubicación y GPS
    var address by remember { mutableStateOf("") }
    var propertyType by remember { mutableStateOf("Habitación") }
    var monthlyPrice by remember { mutableStateOf("150") }
    var gpsLatitude by remember { mutableStateOf<Double?>(null) }
    var gpsLongitude by remember { mutableStateOf<Double?>(null) }
    var isGpsValid by remember { mutableStateOf(false) }

    // Paso 2: Catastro y Documento
    var cadastralCode by remember { mutableStateOf("") }
    var selectedDocType by remember { mutableStateOf("Impuesto Predial GAD Guaranda") }

    // Paso 3: Expediente PDF
    var idDocUploaded by remember { mutableStateOf(false) }
    var propertyDocUploaded by remember { mutableStateOf(false) }
    var utilityBillUploaded by remember { mutableStateOf(false) }

    // Paso 4: Declaración Juramentada
    var isOwnershipDeclared by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Registrar Inmueble", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BlueUEB)
                        Text("Paso $currentStep de 4", fontSize = 12.sp, color = TextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = BlueUEB)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundLight)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Indicador visual de pasos (Stepper)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("1. Ubicación GPS", "2. Catastro", "3. Expediente", "4. Juramento").forEachIndexed { index, label ->
                    val stepNum = index + 1
                    val isDone = stepNum < currentStep
                    val isCurrent = stepNum == currentStep

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = when {
                                isDone -> Color(0xFF10B981)
                                isCurrent -> RedUEB
                                else -> Color(0xFFE2E8F0)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isDone) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                } else {
                                    Text(
                                        text = "$stepNum",
                                        color = if (isCurrent) Color.White else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) RedUEB else TextSecondary
                        )
                    }
                }
            }

            HorizontalDivider(color = DividerColor)

            // ================================================================
            // PASO 1: UBICACIÓN Y GEOCERCADO GPS EN GUARANDA
            // ================================================================
            if (currentStep == 1) {
                Text(
                    text = "Paso 1: Datos del Inmueble y Ubicación Satelital",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = BlueUEB
                )
                Text(
                    text = "Ingrese la dirección y capture las coordenadas GPS para certificar que el inmueble está dentro de Guaranda.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it; errorMessage = null },
                    label = { Text("Dirección exacta en Guaranda") },
                    placeholder = { Text("ej. Av. Guayaquil y Sucre, sector Alpachaca") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = propertyType,
                        onValueChange = { propertyType = it },
                        label = { Text("Tipo") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                    )
                    OutlinedTextField(
                        value = monthlyPrice,
                        onValueChange = { monthlyPrice = it },
                        label = { Text("Canon ($/mes)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                    )
                }

                // Tarjeta de captura GPS
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isGpsValid) Color(0xFFD1FAE5) else Color.White),
                    border = BorderStroke(1.dp, if (isGpsValid) Color(0xFF10B981) else DividerColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GpsFixed, contentDescription = null, tint = if (isGpsValid) Color(0xFF065F46) else BlueUEB)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Geocercado Satelital de Guaranda",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isGpsValid) Color(0xFF065F46) else TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "El sistema valida mediante Haversine que las coordenadas estén dentro del Cantón Guaranda (<15 km).",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        if (gpsLatitude != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Coordenadas: Lat $gpsLatitude, Lng $gpsLongitude (Alpachaca, Guaranda)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                gpsLatitude = -1.591234
                                gpsLongitude = -79.004567
                                isGpsValid = GuarandaGeofenceValidator.isWithinGuaranda(gpsLatitude!!, gpsLongitude!!)
                                errorMessage = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isGpsValid) Color(0xFF047857) else BlueUEB),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isGpsValid) "Coordenadas de Guaranda Verificadas" else "Capturar Coordenadas GPS del Inmueble")
                        }
                    }
                }
            }

            // ================================================================
            // PASO 2: CATASTRO MUNICIPAL Y TÍTULO DE PROPIEDAD
            // ================================================================
            if (currentStep == 2) {
                Text(
                    text = "Paso 2: Registro Catastral y Acreditación de Dominio",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = BlueUEB
                )
                Text(
                    text = "Ingrese la Clave Catastral registrada ante el Municipio de Guaranda y el tipo de documento probatorio.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = cadastralCode,
                    onValueChange = { cadastralCode = it.uppercase(); errorMessage = null },
                    label = { Text("Clave Catastral Municipal (GAD Guaranda)") },
                    placeholder = { Text("ej. 02-01-05-12-004-001") },
                    leadingIcon = { Icon(Icons.Default.HomeWork, contentDescription = null, tint = BlueUEB) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )

                Text("Seleccione el título legal habilitante:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)

                listOf(
                    "Impuesto Predial GAD Guaranda",
                    "Escritura Pública Notariada",
                    "Certificado Registro Propiedad",
                    "Poder Notarial de Administración"
                ).forEach { doc ->
                    val isSelected = selectedDocType == doc
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) BlueUEB.copy(alpha = 0.08f) else Color.White,
                        border = BorderStroke(1.dp, if (isSelected) BlueUEB else DividerColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedDocType = doc }
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedDocType = doc },
                                colors = RadioButtonDefaults.colors(selectedColor = BlueUEB)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(doc, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }

            // ================================================================
            // PASO 3: EXPEDIENTE DIGITAL DE VERIFICACIÓN (PDF)
            // ================================================================
            if (currentStep == 3) {
                Text(
                    text = "Paso 3: Carga de Expediente Digital (PDF)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = BlueUEB
                )
                Text(
                    text = "Adjunte los soportes en formato digital para que el equipo de Bienestar Estudiantil UEB audite el inmueble.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                // 1. Cédula
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (idDocUploaded) Color(0xFFD1FAE5) else Color.White,
                    border = BorderStroke(1.dp, if (idDocUploaded) Color(0xFF10B981) else DividerColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { idDocUploaded = !idDocUploaded; errorMessage = null }
                ) {
                    Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (idDocUploaded) Icons.Default.CheckCircle else Icons.Default.UploadFile, contentDescription = null, tint = if (idDocUploaded) Color(0xFF065F46) else BlueUEB)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("1. Cédula de Identidad Notariada", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(if (idDocUploaded) "cedula_notariada.pdf adjuntado" else "Formato PDF o JPG nítido", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Text(if (idDocUploaded) "Listo" else "Subir PDF", color = if (idDocUploaded) Color(0xFF065F46) else RedUEB, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // 2. Título / Predial
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (propertyDocUploaded) Color(0xFFD1FAE5) else Color.White,
                    border = BorderStroke(1.dp, if (propertyDocUploaded) Color(0xFF10B981) else DividerColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { propertyDocUploaded = !propertyDocUploaded; errorMessage = null }
                ) {
                    Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (propertyDocUploaded) Icons.Default.CheckCircle else Icons.Default.UploadFile, contentDescription = null, tint = if (propertyDocUploaded) Color(0xFF065F46) else BlueUEB)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("2. $selectedDocType", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(if (propertyDocUploaded) "titulo_predial_guaranda.pdf adjuntado" else "Copia del documento de propiedad", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Text(if (propertyDocUploaded) "Listo" else "Subir PDF", color = if (propertyDocUploaded) Color(0xFF065F46) else RedUEB, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // 3. Planilla Luz CNEL / Agua EMAPAG
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (utilityBillUploaded) Color(0xFFD1FAE5) else Color.White,
                    border = BorderStroke(1.dp, if (utilityBillUploaded) Color(0xFF10B981) else DividerColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { utilityBillUploaded = !utilityBillUploaded; errorMessage = null }
                ) {
                    Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (utilityBillUploaded) Icons.Default.CheckCircle else Icons.Default.UploadFile, contentDescription = null, tint = if (utilityBillUploaded) Color(0xFF065F46) else BlueUEB)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("3. Planilla Luz (CNEL) / Agua (EMAPAG)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(if (utilityBillUploaded) "planilla_cnel_guaranda.pdf adjuntado" else "Certifica el suministro del inmueble", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Text(if (utilityBillUploaded) "Listo" else "Subir PDF", color = if (utilityBillUploaded) Color(0xFF065F46) else RedUEB, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // ================================================================
            // PASO 4: DECLARACIÓN JURAMENTADA Y ENVÍO A AUDITORÍA
            // ================================================================
            if (currentStep == 4) {
                Text(
                    text = "Paso 4: Declaración Juramentada y Resumen",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = BlueUEB
                )

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Resumen de Expediente para Auditoría UEB", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BlueUEB)
                        Text("• Dirección: $address (Guaranda)", fontSize = 12.sp)
                        Text("• Coordenadas GPS: Lat $gpsLatitude, Lng $gpsLongitude (Haversine OK)", fontSize = 12.sp)
                        Text("• Clave Catastral: $cadastralCode (GAD Guaranda)", fontSize = 12.sp)
                        Text("• Título: $selectedDocType (Expediente PDF completo)", fontSize = 12.sp)
                        Text("• Canon propuesto: $$monthlyPrice/mes", fontSize = 12.sp)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isOwnershipDeclared = !isOwnershipDeclared; errorMessage = null }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isOwnershipDeclared,
                            onCheckedChange = { isOwnershipDeclared = it; errorMessage = null },
                            colors = CheckboxDefaults.colors(checkedColor = RedUEB)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Declaro bajo juramento que los documentos cargados, las coordenadas GPS y los datos del inmueble son fidedignos y que ostento legítimo derecho para arrendarlo a estudiantes de la UEB.",
                            fontSize = 12.sp,
                            color = Color(0xFF92400E),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = RedUEB,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Botones de Navegación del Stepper
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep--; errorMessage = null },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Anterior")
                    }
                }

                Button(
                    onClick = {
                        when (currentStep) {
                            1 -> {
                                if (address.isBlank()) {
                                    errorMessage = "Ingrese la dirección física del inmueble en Guaranda."
                                } else if (!isGpsValid) {
                                    errorMessage = "Debe capturar las coordenadas GPS dentro de Guaranda."
                                } else {
                                    currentStep = 2
                                    errorMessage = null
                                }
                            }
                            2 -> {
                                if (cadastralCode.isBlank()) {
                                    errorMessage = "Ingrese la Clave Catastral Municipal de Guaranda."
                                } else {
                                    currentStep = 3
                                    errorMessage = null
                                }
                            }
                            3 -> {
                                if (!idDocUploaded || !propertyDocUploaded || !utilityBillUploaded) {
                                    errorMessage = "Debe adjuntar los 3 documentos del expediente digital."
                                } else {
                                    currentStep = 4
                                    errorMessage = null
                                }
                            }
                            4 -> {
                                if (!isOwnershipDeclared) {
                                    errorMessage = "Debe aceptar la declaración juramentada de titularidad."
                                } else {
                                    onPropertySubmitted()
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedUEB)
                ) {
                    Text(if (currentStep == 4) "Enviar a Auditoría UEB" else "Siguiente")
                }
            }
        }
    }
}
