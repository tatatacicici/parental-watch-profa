package com.example.parental_watch.ui.screens

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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.parental_watch.R
import com.example.parental_watch.ui.theme.ParentalWatchTheme
import com.example.parental_watch.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun HomeScreen(
    onParentModeClick: () -> Unit,
    onChildModeClick: () -> Unit
) {
    // ── Entry Animations ──────────────────────────────────────
    val logoScale = remember { Animatable(0.8f) }
    val logoAlpha = remember { Animatable(0f) }
    val greetingAlpha = remember { Animatable(0f) }
    val greetingOffsetY = remember { Animatable(20f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val pillAlpha = remember { Animatable(0f) }
    val pillScale = remember { Animatable(0.9f) }
    val ctaAlpha = remember { Animatable(0f) }
    val ctaOffsetY = remember { Animatable(30f) }
    val parentCardAlpha = remember { Animatable(0f) }

    // Logo: scale + fade
    LaunchedEffect(Unit) {
        logoScale.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
    }
    // Greeting: fade + slide up
    LaunchedEffect(Unit) {
        delay(200)
        greetingAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(200)
        greetingOffsetY.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
    }
    // Subtitle: fade
    LaunchedEffect(Unit) {
        delay(400)
        subtitleAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
    }
    // Status pill: fade + scale
    LaunchedEffect(Unit) {
        delay(500)
        pillAlpha.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(500)
        pillScale.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
    }
    // CTA: fade + slide up
    LaunchedEffect(Unit) {
        delay(600)
        ctaAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(600)
        ctaOffsetY.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
    }
    // Parent card: fade
    LaunchedEffect(Unit) {
        delay(800)
        parentCardAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
    }

    // ── Idle Animations (Decorative) ──────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "decoTransition")
    val cloudOffsetX1 by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud1"
    )
    val cloudOffsetX2 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud2"
    )

    // ── Dynamic Greeting ──────────────────────────────────────
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = remember(hour) {
        when (hour) {
            in 5..10 -> "Selamat pagi! ☀️"
            in 11..14 -> "Selamat siang! 🌤️"
            in 15..17 -> "Selamat sore! 🌅"
            else -> "Selamat malam! 🌙"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Decorative Bokeh Circles ──────────────────────────
        // Large circle: top-right area
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 20.dp, y = 60.dp)
                .graphicsLayer { translationX = cloudOffsetX1 }
                .size(90.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    shape = CircleShape
                )
        )
        // Medium circle: top-left area
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-15).dp, y = 130.dp)
                .graphicsLayer { translationX = cloudOffsetX2 }
                .size(60.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f),
                    shape = CircleShape
                )
        )
        // Small circle: bottom-left area
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 30.dp, y = (-180).dp)
                .size(45.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
                    shape = CircleShape
                )
        )
        // Tiny accent: bottom-right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-40).dp, y = (-240).dp)
                .graphicsLayer { translationX = cloudOffsetX1 * 0.5f }
                .size(30.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
                    shape = CircleShape
                )
        )

        // ── Main Content ──────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .padding(top = 64.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo with entry animation
            Image(
                painter = painterResource(id = R.drawable.logo_app_nb_icon),
                contentDescription = "Logo TontonAman",
                modifier = Modifier
                    .size(140.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Dynamic greeting with slide-up animation
            Text(
                text = "$greeting\nMau nonton apa hari ini?",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .alpha(greetingAlpha.value)
                    .graphicsLayer { translationY = greetingOffsetY.value }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = "Cari video yang ingin kamu tonton.\nKami akan bantu cek dulu supaya tetap aman.",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp,
                modifier = Modifier.alpha(subtitleAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Protection Status Pill (NEW) ──────────────────
            Surface(
                shape = RoundedCornerShape(50),
                color = SuccessGreen.copy(alpha = 0.12f),
                modifier = Modifier
                    .alpha(pillAlpha.value)
                    .scale(pillScale.value)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        "Proteksi Aktif",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Primary CTA — gradient button ─────────────────
            Button(
                onClick = onChildModeClick,
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Mulai Cari Video",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ketik judul video yang ingin kamu tonton",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(ctaAlpha.value)
            )

            Spacer(modifier = Modifier.weight(1f))

            // ── Parent Area Card ──────────────────────────────
            // Corner radius 16dp — konsisten dengan card di ParentDashboard
            Card(
                onClick = onParentModeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(parentCardAlpha.value)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon container: 48dp — standar touch target Material Design
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Area Orang Tua",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Atur jadwal belajar dan lihat riwayat",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ParentalWatchTheme {
        HomeScreen(
            onParentModeClick = {},
            onChildModeClick = {}
        )
    }
}
