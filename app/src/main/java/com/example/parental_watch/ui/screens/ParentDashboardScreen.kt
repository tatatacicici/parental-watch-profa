package com.example.parental_watch.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.parental_watch.data.preference.PreferencesManager
import com.example.parental_watch.ui.dashboard.DashboardState
import com.example.parental_watch.ui.dashboard.DashboardViewModel
import com.example.parental_watch.ui.theme.ParentalWatchTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    prefManager: PreferencesManager,
    onLogout: () -> Unit,
    onChangePinClick: () -> Unit,
    onVideoHistoryClick: () -> Unit = {},
    onStudyScheduleClick: () -> Unit = {},
    onGoogleLogoutClick: () -> Unit = {},
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val summary by dashboardViewModel.summary.collectAsState()
    val historyState by dashboardViewModel.state.collectAsState()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                dashboardViewModel.loadHistory()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ── Entry Animations ──────────────────────────────────────
    val headerAlpha = remember { Animatable(0f) }
    val headerOffsetY = remember { Animatable(20f) }
    val summaryAlpha = remember { Animatable(0f) }
    val summaryOffsetY = remember { Animatable(20f) }
    val menu1Alpha = remember { Animatable(0f) }
    val menu1OffsetY = remember { Animatable(20f) }
    val menu2Alpha = remember { Animatable(0f) }
    val menu2OffsetY = remember { Animatable(20f) }
    val menu3Alpha = remember { Animatable(0f) }
    val menu3OffsetY = remember { Animatable(20f) }
    val menu4Alpha = remember { Animatable(0f) }
    val menu4OffsetY = remember { Animatable(20f) }
    val logoutAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        headerAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        headerOffsetY.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(100)
        summaryAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        summaryOffsetY.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(200)
        menu1Alpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        menu1OffsetY.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(300)
        menu2Alpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        menu2OffsetY.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(400)
        menu3Alpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        menu3OffsetY.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(500)
        menu4Alpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        menu4OffsetY.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(600)
        logoutAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
    }

    // ── Idle Animations (Decorative) ──────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "decoDashboard")
    val cloudOffsetX1 by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dashCloud1"
    )
    val cloudOffsetX2 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dashCloud2"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Dashboard Orang Tua",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Keluar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ── Decorative Bokeh Circles ──────────────────────
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- 1. Compact Status Header ---
                Box(
                    modifier = Modifier
                        .alpha(headerAlpha.value)
                        .graphicsLayer { translationY = headerOffsetY.value }
                ) {
                    StatusHeaderCompact()
                }

                // --- 2. Summary Cards ---
                Column(
                    modifier = Modifier
                        .alpha(summaryAlpha.value)
                        .graphicsLayer { translationY = summaryOffsetY.value }
                ) {
                    Text(
                        text = "Ringkasan Hari Ini",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            label = "Ditonton",
                            value = summary.watchedToday.toString(),
                            icon = Icons.Default.PlayArrow,
                            color = Color(0xFF42A5F5),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            label = "Diblokir",
                            value = summary.blockedToday.toString(),
                            icon = Icons.Default.Block,
                            color = Color(0xFFE53935),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            label = "Jam Belajar",
                            value = if (prefManager.isStudyTimeNow()) "Aktif" else "Nonaktif",
                            icon = Icons.Default.School,
                            color = if (prefManager.isStudyModeEnabled()) Color(0xFF43A047) else Color(0xFFFB8C00),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp).alpha(summaryAlpha.value),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Text(
                    text = "Pengaturan & Fitur",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp).alpha(summaryAlpha.value)
                )


                MenuCard(
                    title = "Riwayat Video",
                    subtitle = "Video YouTube yang ditonton anak",
                    icon = Icons.Default.PlayArrow,
                    color = Color(0xFFD32F2F),
                    alpha = menu2Alpha.value,
                    offsetY = menu2OffsetY.value,
                    onClick = onVideoHistoryClick
                )

                MenuCard(
                    title = "Jadwal Belajar",
                    subtitle = "Atur waktu belajar anak",
                    icon = Icons.Default.Timer,
                    color = Color(0xFFFB8C00),
                    alpha = menu3Alpha.value,
                    offsetY = menu3OffsetY.value,
                    onClick = onStudyScheduleClick
                )

                MenuCard(
                    title = "Ganti PIN",
                    subtitle = "Ubah kode akses dashboard",
                    icon = Icons.Default.Lock,
                    color = Color(0xFF7B1FA2),
                    alpha = menu4Alpha.value,
                    offsetY = menu4OffsetY.value,
                    onClick = onChangePinClick
                )

                // --- 4. Extra Options ---
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = onGoogleLogoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(logoutAlpha.value),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout dari Google")
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    alpha: Float,
    offsetY: Float,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .graphicsLayer { translationY = offsetY },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun StatusHeaderCompact() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Perlindungan Aktif",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = "Pemantauan konten YouTube berjalan normal",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2E7D32).copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ParentDashboardScreenPreview() {
    val context = LocalContext.current
    val prefManager = PreferencesManager(context)
    ParentalWatchTheme {
        ParentDashboardScreen(
            prefManager = prefManager,
            onLogout = {},
            onChangePinClick = {},
            onVideoHistoryClick = {},
            onStudyScheduleClick = {},
            onGoogleLogoutClick = {}
        )
    }
}
