package com.example.unihogarguaranda.ui.screens

import android.net.Uri
import android.view.ViewGroup.LayoutParams
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import com.example.unihogarguaranda.model.Modelo3D
import com.google.gson.Gson

/** URL del visor local con el modelo en el fragmento: no sale del teléfono ni necesita red. */
internal fun urlModelo3D(modelo: Modelo3D): String =
    "file:///android_asset/room-3d.html#" + Uri.encode(Gson().toJson(modelo))

/** RF14: habitación ilustrativa a partir de las medidas y muebles que declaró el propietario. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Room3DScreen(titulo: String, modelo: Modelo3D, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val url = urlModelo3D(modelo)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                    settings.javaScriptEnabled = true
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.blockNetworkLoads = true
                    tag = url
                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize().padding(padding),
            onRelease = { it.destroy() },
            // WebView puede normalizar la URL; se compara con la última que se cargó.
            update = {
                if (it.tag != url) {
                    it.tag = url
                    it.loadUrl(url)
                }
            }
        )
    }
}
