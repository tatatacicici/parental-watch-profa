package com.example.parental_watch.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parental_watch.ui.theme.ParentalWatchTheme
import kotlinx.coroutines.launch

data class OnboardingPage(
    val icon: ImageVector,
    val iconColor: Color,
    val iconBgColor: Color,
    val title: String,
    val description: String,
    val highlight: String
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            icon = Icons.Default.Shield,
            iconColor = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9),
            title = "Selamat Datang di\nPanduTonton! 👋",
            description = "Aplikasi ini membantu melindungi anak Anda dari konten YouTube yang tidak pantas secara otomatis.",
            highlight = "Aman & Otomatis"
        ),
        OnboardingPage(
            icon = Icons.Default.Search,
            iconColor = Color(0xFF1565C0),
            iconBgColor = Color(0xFFE3F2FD),
            title = "Anak Cari Video,\nKami yang Cek 🔍",
            description = "Saat anak mencari video, sistem akan menganalisis judul dan komentar untuk mendeteksi konten berbahaya sebelum video ditampilkan.",
            highlight = "Klasifikasi 2 Tahap"
        ),
        OnboardingPage(
            icon = Icons.Default.Lock,
            iconColor = Color(0xFF7B1FA2),
            iconBgColor = Color(0xFFF3E5F5),
            title = "Buat PIN\nOrang Tua 🔐",
            description = "Setelah ini, Anda akan diminta membuat PIN 4 digit. PIN ini untuk mengakses Dashboard Orang Tua dan mengatur fitur keamanan.",
            highlight = "Hanya Orang Tua"
        ),
        OnboardingPage(
            icon = Icons.Default.Timer,
            iconColor = Color(0xFFE65100),
            iconBgColor = Color(0xFFFFF3E0),
            title = "Atur Jadwal\nBelajar ⏰",
            description = "Di Dashboard, Anda bisa mengatur jam belajar. Selama jam belajar aktif, anak tidak bisa menonton video YouTube.",
            highlight = "Jadwal Fleksibel"
        ),
        OnboardingPage(
            icon = Icons.Default.PlayArrow,
            iconColor = Color(0xFFD32F2F),
            iconBgColor = Color(0xFFFFEBEE),
            title = "Pantau Riwayat\nTontonan 📊",
            description = "Lihat semua video yang ditonton atau diblokir dari Dashboard. Anda bisa memantau aktivitas anak kapan saja.",
            highlight = "Transparan & Terkontrol"
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.size - 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, end = 20.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (!isLastPage) {
                    TextButton(onClick = onFinish) {
                        Text(
                            "Lewati",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(pages[page])
            }

            // Bottom section: indicators + button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { index ->
                        val isActive = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (isActive) 28.dp else 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isActive) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action button
                Button(
                    onClick = {
                        if (isLastPage) {
                            onFinish()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
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
                                    colors = if (isLastPage) listOf(
                                        Color(0xFF2E7D32),
                                        Color(0xFF43A047)
                                    ) else listOf(
                                        Color(0xFF3B8BD4),
                                        Color(0xFF2A6CB0)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isLastPage) "Mulai Sekarang" else "Lanjut",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Icon(
                                imageVector = if (isLastPage) Icons.Default.Check
                                else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    // Entry animation
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        offsetY.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp)
            .alpha(alpha.value)
            .graphicsLayer { translationY = offsetY.value },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon circle
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(page.iconBgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = page.iconColor,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Title
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Highlight pill
        Surface(
            shape = RoundedCornerShape(50),
            color = page.iconBgColor
        ) {
            Text(
                text = page.highlight,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = page.iconColor
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 26.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    ParentalWatchTheme {
        OnboardingScreen(onFinish = {})
    }
}
