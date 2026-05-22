package com.example.parental_watch.ui.player

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlayerScreen(
    videoId: String,
    title: String,
    onRelatedVideoClicked: (String) -> Unit,
    onBack: () -> Unit
) {
    val embedUrl = "https://www.youtube.com/embed/$videoId" +
            "?rel=0&modestbranding=1&autoplay=1"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest
                        ): Boolean {
                            val url = request.url.toString()

                            // Intercept navigasi ke video lain
                            val relatedVideoId = extractVideoId(url)
                            if (relatedVideoId != null && relatedVideoId != videoId) {
                                onRelatedVideoClicked(relatedVideoId)
                                return true
                            }

                            // Block semua navigasi keluar dari YouTube
                            if (!url.contains("youtube.com") && !url.contains("youtu.be")) {
                                return true
                            }

                            return false
                        }
                    }

                    loadUrl(embedUrl)
                }
            }
        )
    }
}

private fun extractVideoId(url: String): String? {
    return try {
        when {
            url.contains("youtube.com/watch") -> {
                val uri = android.net.Uri.parse(url)
                uri.getQueryParameter("v")
            }
            url.contains("youtu.be/") -> {
                url.substringAfter("youtu.be/").substringBefore("?")
            }
            url.contains("youtube.com/embed/") -> {
                url.substringAfter("youtube.com/embed/").substringBefore("?")
            }
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}
