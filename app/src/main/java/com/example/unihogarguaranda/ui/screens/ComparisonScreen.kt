package com.example.unihogarguaranda.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.unihogarguaranda.model.Property
import java.text.NumberFormat
import java.util.Locale

internal fun moneda(valor: Double): String = NumberFormat.getCurrencyInstance(Locale.US).format(valor)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ComparisonScreen(propiedades: List<Property>, onRemove: (Property) -> Unit, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Comparar viviendas") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        )
    }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (propiedades.isEmpty()) Text("Abre un anuncio y toca Agregar al comparador.")
            else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Spacer(Modifier.width(114.dp))
                    propiedades.forEach { propiedad ->
                        Column(Modifier.width(156.dp)) {
                            Text(propiedad.title, style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = { onRemove(propiedad) }) { Text("Quitar") }
                        }
                    }
                }
                val filas = listOf<Pair<String, (Property) -> String>>(
                    "Arriendo mensual" to { moneda(it.price) },
                    "Cargos al mes" to {
                        it.costos?.cargosRecurrentes?.let { cargos ->
                            if (cargos.isEmpty()) "Sin cargos declarados"
                            else cargos.joinToString("\n") { cargo ->
                                "${cargo.nombre} · ${cargo.monto?.let(::moneda) ?: "Por confirmar"}"
                            }
                        } ?: "Por confirmar"
                    },
                    "Total mensual" to { it.costos?.totalMensual?.let(::moneda) ?: "Por confirmar" },
                    "Depósito inicial" to { it.costos?.deposito?.let(::moneda) ?: "Por confirmar" },
                    "Servicios" to { it.services.joinToString(", ").ifBlank { "Sin información" } },
                    "Distancia al campus" to { it.campusDistance.ifBlank { "Sin información" } }
                )
                filas.forEach { (etiqueta, valor) ->
                    HorizontalDivider()
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(etiqueta, modifier = Modifier.width(114.dp), style = MaterialTheme.typography.labelLarge)
                        propiedades.forEach { Text(valor(it), modifier = Modifier.width(156.dp)) }
                    }
                }
            }
        }
    }
}
