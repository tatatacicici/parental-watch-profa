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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.parental_watch.data.preference.PreferencesManager
import com.example.parental_watch.service.ParentalDeviceAdminReceiver
import com.example.parental_watch.ui.dashboard.DashboardState
import com.example.parental_watch.ui.dashboard.DashboardViewModel
import com.example.parental_watch.ui.theme.ParentalWatchTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    prefManager: PreferencesManager,
    onLogout: () -> Unit,
    onWhitelistClick: () -> Unit,
    onLogClick: () -> Unit,
    onChangePinClick: () -> Unit,
    onVideoHistoryClick: () -> Unit = {},
    onStudyScheduleClick: () -> Unit = {},
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isPreview = LocalInspectionMode.current
    
    val summary by dashboardViewModel.summary.collectAsState()
    val historyState by dashboardViewModel.state.collectAsState()

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
                if (!isPreview && adminComponent != null) {
                    isAdminActive = devicePolicyManager?.isAdminActive(adminComponent) ?: false
                }
                dashboardViewModel.loadHistory()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. Compact Security Header ---
            SecurityStatusHeaderCompact(allActive = isAdminActive, context = context, adminComponent = adminComponent)

            // --- 2. Summary Cards ---
            Text(
                text = "Ringkasan Hari Ini",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
            
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

            val totalActivities = if (historyState is DashboardState.Success) {
                (historyState as DashboardState.Success).history.size
            } else 0

            DashboardMenuItem(
                title = "Riwayat Video YouTube",
                subtitle = "Pantau video yang ditonton & diblokir",
                icon = Icons.Default.PlayArrow,
                previewText = if (totalActivities > 0) "$totalActivities aktivitas" else "Lihat",
                onClick = onVideoHistoryClick
            )

            DashboardMenuItem(
                title = "Jam Belajar",
                subtitle = if (prefManager.isStudyModeEnabled()) {
                    "Aktif: ${prefManager.getStudyScheduleText()}"
                } else {
                    "Atur waktu pembatasan video"
                },
                icon = Icons.Default.DateRange,
                previewText = if (prefManager.isStudyModeEnabled()) "Aktif" else "Nonaktif",
                onClick = onStudyScheduleClick
            )

            DashboardMenuItem(
                title = "Perbarui PIN",
                subtitle = "Ubah kode akses orang tua",
                icon = Icons.Default.Lock,
                previewText = "Aktif",
                onClick = onChangePinClick
            )

            DashboardMenuItem(
                title = "Anti-Uninstall",
                subtitle = if (isAdminActive) "Layanan aktif" else "Mencegah aplikasi dihapus",
                icon = Icons.Default.Settings,
                highlight = !isAdminActive,
                previewText = if (isAdminActive) "Aktif" else "Belum aktif",
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
fun SummaryCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SecurityStatusHeaderCompact(
    allActive: Boolean,
    context: android.content.Context,
    adminComponent: ComponentName?
) {
    val bgColor = if (allActive) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
    val contentColor = if (allActive) Color(0xFF2E7D32) else Color(0xFFE65100)
    val icon = if (allActive) Icons.Default.CheckCircle else Icons.Default.Warning

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        onClick = {
            if (!allActive && adminComponent != null) {
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                }
                context.startActivity(intent)
            }
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = if (allActive) "Sistem Terlindungi" else "Status Proteksi",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = if (allActive) "Anti-uninstall aktif" else "Anti-uninstall belum aktif",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
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
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
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
                val statusColor = if (previewText == "Belum aktif" || previewText == "Nonaktif") Color(0xFFE53935) else MaterialTheme.colorScheme.primary
                Text(
                    text = previewText,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
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
            onChangePinClick = {},
            onVideoHistoryClick = {}
        )
    }
}
