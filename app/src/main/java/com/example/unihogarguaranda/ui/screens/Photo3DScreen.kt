package com.example.unihogarguaranda.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Photo3DScreen(url: String, photoNumber: Int, onBack: () -> Unit) {
    var tilt by rememberSaveable(url) { mutableFloatStateOf(0f) }
    var turn by rememberSaveable(url) { mutableFloatStateOf(0f) }

    BackHandler(onBack = onBack)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Foto $photoNumber en 3D") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver a la galería")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clipToBounds()
                    .pointerInput(url) {
                        detectDragGestures { change, delta ->
                            change.consume()
                            turn = (turn + delta.x * 70f / size.width).coerceIn(-42f, 42f)
                            tilt = (tilt - delta.y * 70f / size.height).coerceIn(-32f, 32f)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = url,
                    contentDescription = "Foto $photoNumber del inmueble",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(24.dp).graphicsLayer {
                        rotationX = tilt
                        rotationY = turn
                        cameraDistance = maxOf(size.width, size.height) * 1.25f
                    },
                    loading = { PhotoMessage("Cargando foto…") },
                    error = { PhotoMessage("No se pudo cargar la foto. Vuelve a la galería e inténtalo de nuevo.") }
                )
            }
            Text(
                "Arrastra la foto para inclinarla y cambiar la perspectiva.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
            ) {
                OutlinedButton(onClick = { turn = (turn - 15f).coerceAtLeast(-42f) }) { Text("Izquierda") }
                OutlinedButton(onClick = { turn = 0f; tilt = 0f }) { Text("Centrar") }
                OutlinedButton(onClick = { turn = (turn + 15f).coerceAtMost(42f) }) { Text("Derecha") }
            }
        }
    }
}

@Composable
private fun PhotoMessage(message: String) {
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
