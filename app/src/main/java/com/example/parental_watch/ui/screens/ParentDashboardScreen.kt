package com.example.parental_watch.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.parental_watch.data.preference.PreferencesManager
import com.example.parental_watch.service.ParentalDeviceAdminReceiver
import com.example.parental_watch.ui.theme.ParentalWatchTheme
import com.example.parental_watch.utils.AccessibilityUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    prefManager: PreferencesManager,
    onLogout: () -> Unit,
    onWhitelistClick: () -> Unit,
    onLogClick: () -> Unit,
    onChangePinClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isPreview = LocalInspectionMode.current

    var isServiceEnabled by remember { mutableStateOf(prefManager.isServiceEnabled()) }
    var isAccessibilityEnabled by remember {
        mutableStateOf(
            if (isPreview) false else AccessibilityUtils.isAccessibilityServiceEnabled(context)
        )
    }

    val devicePolicyManager = remember(context) {
        if (isPreview) null else context.getSystemService(DevicePolicyManager::class.java)
    }
    val adminComponent = remember(context) {
        if (isPreview) null 
        else ComponentName(context, ParentalDeviceAdminReceiver::class.java)
    }
    var isAdminActive by remember {
        mutableStateOf(
            if (isPreview || adminComponent == null) false 
            else devicePolicyManager?.isAdminActive(adminComponent) ?: false
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAccessibilityEnabled =
                    if (isPreview) false else AccessibilityUtils.isAccessibilityServiceEnabled(context)
                if (!isPreview && adminComponent != null) {
                    isAdminActive = devicePolicyManager?.isAdminActive(adminComponent) ?: false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Parental Dashboard",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. Visual Security Header (Urutan Kepentingan) ---
            val allActive = isServiceEnabled && isAccessibilityEnabled && isAdminActive
            SecurityStatusHeader(allActive = allActive)

            // --- 2. Service Control with Pulse Animation (Feedback) ---
            ServiceControlCard(
                isRunning = isServiceEnabled && isAccessibilityEnabled,
                isAccessibilityEnabled = isAccessibilityEnabled,
                onClick = {
                    if (!isAccessibilityEnabled) {
                        AccessibilityUtils.openAccessibilitySettings(context)
                    } else {
                        isServiceEnabled = !isServiceEnabled
                        prefManager.setServiceEnabled(isServiceEnabled)
                    }
                }
            )

            // --- Mini Status Summary ---
            StatusSummaryRow(
                isAccessibilityEnabled = isAccessibilityEnabled,
                isServiceEnabled = isServiceEnabled,
                isAdminActive = isAdminActive
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Text(
                text = "Pengaturan & Fitur",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // --- 3. Menu Items with Preview Data (Cognitive Load Reduction) ---
            val whitelistCount = prefManager.getWhitelist().size
            DashboardMenuItem(
                title = "Aplikasi yang Dipantau",
                subtitle = "Kelola daftar aplikasi yang diawasi",
                icon = Icons.AutoMirrored.Filled.List,
                previewText = if (whitelistCount > 0) "$whitelistCount Aplikasi" else "Belum ada",
                onClick = onWhitelistClick
            )

            DashboardMenuItem(
                title = "Riwayat Deteksi",
                subtitle = "Lihat log aktivitas yang diblokir",
                icon = Icons.Default.Info,
                previewText = "Lihat Log",
                onClick = onLogClick
            )

            DashboardMenuItem(
                title = "Keamanan PIN",
                subtitle = "Ubah kode akses orang tua",
                icon = Icons.Default.Lock,
                onClick = onChangePinClick
            )

            DashboardMenuItem(
                title = "Anti-Uninstall",
                subtitle = if (isAdminActive) "Aktif" else "Ketuk untuk aktifkan",
                icon = Icons.Default.Settings,
                highlight = !isAdminActive,
                onClick = {
                    if (!isAdminActive && adminComponent != null) {
                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                            putExtra(
                                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                "Mencegah aplikasi dihapus tanpa izin orang tua."
                            )
                        }
                        context.startActivity(intent)
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SecurityStatusHeader(allActive: Boolean) {
    val bgColor = if (allActive) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (allActive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
    val icon = if (allActive) Icons.Default.CheckCircle else Icons.Default.Warning

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.width(20.dp))
            Column {
                Text(
                    text = if (allActive) "Sistem Terlindungi" else "Proteksi Belum Lengkap",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor
                )
                Text(
                    text = if (allActive) "Semua fitur pengawasan aktif" else "Selesaikan setup untuk keamanan maksimal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun StatusSummaryRow(
    isAccessibilityEnabled: Boolean,
    isServiceEnabled: Boolean,
    isAdminActive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MiniStatusIndicator(label = "Akses", isActive = isAccessibilityEnabled)
        MiniStatusIndicator(label = "Monitoring", isActive = isServiceEnabled && isAccessibilityEnabled)
        MiniStatusIndicator(label = "Anti-Uninst", isActive = isAdminActive)
    }
}

@Composable
fun MiniStatusIndicator(label: String, isActive: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isActive) Color(0xFF4CAF50) else Color.LightGray)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall, 
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ServiceControlCard(
    isRunning: Boolean,
    isAccessibilityEnabled: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when {
        !isAccessibilityEnabled -> MaterialTheme.colorScheme.error
        isRunning -> Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 24.dp, horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isRunning) {
                PulseIndicator()
                Spacer(Modifier.width(12.dp))
            }
            
            Icon(
                imageVector = if (!isAccessibilityEnabled) Icons.Default.Warning 
                              else if (isRunning) Icons.Default.CheckCircle 
                              else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = when {
                    !isAccessibilityEnabled -> "Setup Aksesibilitas"
                    isRunning -> "Layanan Aktif & Berjalan"
                    else -> "Mulai Pengawasan"
                },
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun PulseIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(Color.White.copy(alpha = alpha), CircleShape)
                .padding(4.dp)
                .size(12.dp * scale)
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(Color.White, CircleShape)
        )
    }
}

@Composable
fun DashboardMenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    previewText: String? = null,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (highlight) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) 
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
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

            if (previewText != null) {
                Text(
                    text = previewText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ParentDashboardScreenPreview() {
    val context = LocalContext.current
    val prefManager = remember { PreferencesManager(context) }
    ParentalWatchTheme {
        ParentDashboardScreen(
            prefManager = prefManager,
            onLogout = {},
            onWhitelistClick = {},
            onLogClick = {},
            onChangePinClick = {}
        )
    }
}
