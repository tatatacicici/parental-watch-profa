package com.example.parental_watch.ui.screens

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.tooling.preview.Preview
import com.example.parental_watch.R
import com.example.parental_watch.data.preference.PreferencesManager
import com.example.parental_watch.ui.theme.ParentalWatchTheme
import com.example.parental_watch.ui.theme.SuccessGreen
import kotlinx.coroutines.delay

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
    // ── Entry Animations ──────────────────────────────────────
    val logoScale = remember { Animatable(0.8f) }
    val logoAlpha = remember { Animatable(0f) }
    val headingAlpha = remember { Animatable(0f) }
    val headingOffsetY = remember { Animatable(20f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val cardAlpha = remember { Animatable(0f) }
    val cardOffsetY = remember { Animatable(20f) }
    val badgeAlpha = remember { Animatable(0f) }
    val badgeScale = remember { Animatable(0.9f) }
    val ctaAlpha = remember { Animatable(0f) }
    val ctaOffsetY = remember { Animatable(30f) }

    // Logo: scale + fade
    LaunchedEffect(Unit) {
        logoScale.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
    }
    // Heading: fade + slide up
    LaunchedEffect(Unit) {
        delay(200)
        headingAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(200)
        headingOffsetY.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
    }
    // Subtitle: fade
    LaunchedEffect(Unit) {
        delay(350)
        subtitleAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
    }
    // Info Card: fade + slide up
    LaunchedEffect(Unit) {
        delay(450)
        cardAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(450)
        cardOffsetY.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
    }
    // Trust Badge: fade + scale
    LaunchedEffect(Unit) {
        delay(600)
        badgeAlpha.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(600)
        badgeScale.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
    }
    // CTA: fade + slide up
    LaunchedEffect(Unit) {
        delay(700)
        ctaAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(700)
        ctaOffsetY.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
    }

    // ── Idle Animations (Decorative) ──────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "decoLoginTransition")
    val cloudOffsetX1 by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loginCloud1"
    )
    val cloudOffsetX2 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loginCloud2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Decorative Bokeh Circles ──────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 15.dp, y = 50.dp)
                .graphicsLayer { translationX = cloudOffsetX1 }
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-10).dp, y = 120.dp)
                .graphicsLayer { translationX = cloudOffsetX2 }
                .size(50.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = (-160).dp)
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 40.dp, y = (-220).dp)
                .graphicsLayer { translationX = cloudOffsetX1 * 0.5f }
                .size(28.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
                    shape = CircleShape
                )
        )

        // ── Main Content ──────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Hero Logo with entry animation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_app_icon),
                    contentDescription = "Logo TontonAman",
                    modifier = Modifier
                        .size(130.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Heading — tone profesional untuk orang tua
            Text(
                text = "Siapkan Akun Google Anda",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .alpha(headingAlpha.value)
                    .graphicsLayer { translationY = headingOffsetY.value }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle — jelas, formal, informatif
            Text(
                text = "Login Google diperlukan untuk mengakses YouTube secara aman melalui TontonAman.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.titleMedium.lineHeight * 1.3f,
                modifier = Modifier.alpha(subtitleAlpha.value)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Info card — alasan login, trust signals
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(cardAlpha.value)
                    .graphicsLayer { translationY = cardOffsetY.value },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    InfoRow(Icons.Default.PlayArrow, "Akses video YouTube yang aman")
                    Spacer(modifier = Modifier.height(14.dp))
                    InfoRow(Icons.Default.Shield, "Konten difilter otomatis oleh AI")
                    Spacer(modifier = Modifier.height(14.dp))
                    InfoRow(Icons.Default.Lock, "Data tersimpan lokal di perangkat")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Trust Badge Pill (NEW) ────────────────────────
            Surface(
                shape = RoundedCornerShape(50),
                color = SuccessGreen.copy(alpha = 0.12f),
                modifier = Modifier
                    .alpha(badgeAlpha.value)
                    .scale(badgeScale.value)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        "Data Anda tidak di-upload ke server",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // CTA — gradient konsisten dengan HomeScreen
            Button(
                onClick = onStartLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .alpha(ctaAlpha.value)
                    .graphicsLayer { translationY = ctaOffsetY.value },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF3B8BD4),
                                    Color(0xFF2A6CB0)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Masuk dengan Google",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Icon container: 48dp — standar touch target
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
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

                // Pastikan cache dan history dibersihkan agar sesi lama benar-benar hilang
                clearCache(true)
                clearHistory()
                clearFormData()

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

@Preview(showBackground = true)
@Composable
fun GoogleLoginScreenPreview() {
    ParentalWatchTheme {
        LoginPromptContent(onStartLogin = {})
    }
}
