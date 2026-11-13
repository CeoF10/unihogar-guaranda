package com.example.unihogarguaranda.ui.screens

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PanoramaScreen(url: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val source = remember(url) { url.toHttpUrlOrNull() }
    val html = remember(source) {
        source?.let {
            val imageUrl = JSONObject.quote(it.toString()).replace("<", "\\u003c")
            context.assets.open("panorama.html").bufferedReader().use { template ->
                template.readText().replace("__PANORAMA_URL__", imageUrl)
            }
        }
    }
    BackHandler(onBack = onBack)
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Recorrido 360°") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver a la galería")
                }
            }
        )
    }) { padding ->
        if (html == null || source == null) {
            Text("El panorama no está disponible.", modifier = Modifier.padding(padding))
        } else {
            AndroidView(
                factory = {
                    WebView(it).apply {
                        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                        settings.javaScriptEnabled = true
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
                        loadDataWithBaseURL(source.toString(), html, "text/html", "UTF-8", null)
                    }
                },
                modifier = Modifier.fillMaxSize().padding(padding),
                onRelease = { it.destroy() },
                update = {}
            )
        }
    }
}
