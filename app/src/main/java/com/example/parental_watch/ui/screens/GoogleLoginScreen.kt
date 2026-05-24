package com.example.parental_watch.ui.screens

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.parental_watch.R
import com.example.parental_watch.data.preference.PreferencesManager

// ── State ─────────────────────────────────────────────────
sealed class GoogleLoginState {
    object Prompt : GoogleLoginState()       // tampilkan penjelasan + tombol
    object Loading : GoogleLoginState()     // WebView login sedang jalan
    object Success : GoogleLoginState()     // cookie YouTube valid
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GoogleLoginScreen(
    prefManager: PreferencesManager,
    onLoginSuccess: () -> Unit
) {
    var state by remember { mutableStateOf<GoogleLoginState>(GoogleLoginState.Prompt) }

    when (state) {
        is GoogleLoginState.Prompt -> {
            LoginPromptContent(
                onStartLogin = { state = GoogleLoginState.Loading }
            )
        }
        is GoogleLoginState.Loading -> {
            GoogleWebViewLogin(
                onLoggedIn = {
                    prefManager.setGoogleLoggedIn(true)
                    state = GoogleLoginState.Success
                    onLoginSuccess()
                }
            )
        }
        is GoogleLoginState.Success -> {
            // Langsung navigate, tidak perlu tampilkan apapun
        }
    }
}

// ── Prompt Screen ─────────────────────────────────────────

@Composable
private fun LoginPromptContent(onStartLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Hero Section with Decorations
        DecorativeHeroIcon()

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Halo! Yuk, Siapkan Akunmu",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Supaya kamu bisa mulai nonton video seru, kita masuk ke Google sebentar ya. Ayah atau Bunda akan bantu kamu!",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.titleMedium.lineHeight * 1.3f
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Info card - Soft & Friendly
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                InfoRow(Icons.Default.PlayArrow, "Akses video YouTube yang aman")
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow(Icons.Default.Security, "Ayah & Bunda bantu jagain kamu")
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow(Icons.Default.Star, "Hanya video baik yang muncul")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onStartLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                "Mulai Masuk",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DecorativeHeroIcon() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_app_icon),
            contentDescription = "TontonAman",
            modifier = Modifier.size(130.dp)
        )
    }
}

// ── WebView Login ─────────────────────────────────────────

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun GoogleWebViewLogin(onLoggedIn: () -> Unit) {
    val loginUrl = "https://accounts.google.com/signin/v2/identifier" +
            "?service=youtube&continue=https://www.youtube.com"

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    allowContentAccess = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/120.0.0.0 Mobile Safari/537.36"
                }

                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this, true)

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        val url = request.url.toString()

                        // Deteksi berhasil redirect ke YouTube setelah login
                        if (url.contains("youtube.com") &&
                            !url.contains("accounts.google.com") &&
                            !url.contains("signin")) {

                            // Verifikasi cookie YouTube ada
                            val cookies = CookieManager.getInstance()
                                .getCookie("https://www.youtube.com")

                            if (!cookies.isNullOrBlank() &&
                                (cookies.contains("SID") || cookies.contains("HSID"))) {
                                onLoggedIn()
                                return true
                            }
                        }
                        return false
                    }
                }

                loadUrl(loginUrl)
            }
        }
    )
}
