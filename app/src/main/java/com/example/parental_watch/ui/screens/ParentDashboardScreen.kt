package com.example.parental_watch.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.parental_watch.data.preference.PreferencesManager
import com.example.parental_watch.service.ParentalDeviceAdminReceiver
import com.example.parental_watch.utils.AccessibilityUtils

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

    var isServiceEnabled by remember { mutableStateOf(prefManager.isServiceEnabled()) }
    var isAccessibilityEnabled by remember {
        mutableStateOf(AccessibilityUtils.isAccessibilityServiceEnabled(context))
    }

    // Device Admin
    val devicePolicyManager = context.getSystemService(DevicePolicyManager::class.java)
    val adminComponent = ComponentName(context, ParentalDeviceAdminReceiver::class.java)
    var isAdminActive by remember {
        mutableStateOf(devicePolicyManager.isAdminActive(adminComponent))
    }

    // Re-cek status saat user balik dari Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAccessibilityEnabled =
                    AccessibilityUtils.isAccessibilityServiceEnabled(context)
                isAdminActive = devicePolicyManager.isAdminActive(adminComponent)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Text(
                text = "Panel Orang Tua",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Status accessibility
            Text(
                text = if (isAccessibilityEnabled) "Accessibility: Aktif ✓"
                else "Accessibility: Belum diaktifkan ⚠",
                style = MaterialTheme.typography.bodySmall,
                color = if (isAccessibilityEnabled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )

            // Status layanan
            Text(
                text = if (isServiceEnabled && isAccessibilityEnabled) "Layanan: Berjalan ✓"
                else "Layanan: Nonaktif",
                style = MaterialTheme.typography.bodySmall,
                color = if (isServiceEnabled && isAccessibilityEnabled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Status Device Admin
            Text(
                text = if (isAdminActive) "Proteksi Uninstall: Aktif ✓"
                else "Proteksi Uninstall: Nonaktif",
                style = MaterialTheme.typography.bodySmall,
                color = if (isAdminActive)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Tombol Accessibility ──────────────────────────
            Button(
                onClick = {
                    if (!isAccessibilityEnabled) {
                        AccessibilityUtils.openAccessibilitySettings(context)
                    } else {
                        isServiceEnabled = !isServiceEnabled
                        prefManager.setServiceEnabled(isServiceEnabled)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(
                    when {
                        !isAccessibilityEnabled -> "Aktifkan Accessibility Service →"
                        isServiceEnabled -> "Nonaktifkan Layanan"
                        else -> "Aktifkan Layanan"
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // ── Whitelist ─────────────────────────────────────
            OutlinedButton(
                onClick = onWhitelistClick,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Aplikasi yang Dipantau")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Log ───────────────────────────────────────────
            OutlinedButton(
                onClick = onLogClick,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Riwayat Deteksi")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Ubah PIN ──────────────────────────────────────
            OutlinedButton(
                onClick = onChangePinClick,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Ubah PIN")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Device Admin ──────────────────────────────────
            OutlinedButton(
                onClick = {
                    if (!isAdminActive) {
                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                            putExtra(
                                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                "Aktifkan untuk mencegah aplikasi diuninstall oleh anak."
                            )
                        }
                        context.startActivity(intent)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(if (isAdminActive) "Proteksi Uninstall: Aktif ✓" else "Aktifkan Anti-Uninstall")
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Logout ────────────────────────────────────────
            OutlinedButton(
                onClick = onLogout,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Keluar")
            }
        }
    }
}