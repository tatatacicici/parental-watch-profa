package com.example.parental_watch.ui.screens

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.parental_watch.data.preference.PreferencesManager
import com.example.parental_watch.ui.theme.ParentalWatchTheme

@Composable
fun ParentDashboardScreen(
    prefManager: PreferencesManager,
    onLogout: () -> Unit
) {
    var isServiceEnabled by remember {
        mutableStateOf(prefManager.isServiceEnabled())
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

            Text(
                text = if (isServiceEnabled) "Layanan: Aktif ✓" else "Layanan: Nonaktif",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isServiceEnabled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Toggle service
            Button(
                onClick = {
                    isServiceEnabled = !isServiceEnabled
                    prefManager.setServiceEnabled(isServiceEnabled)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(if (isServiceEnabled) "Nonaktifkan Layanan" else "Aktifkan Layanan")
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Whitelist — Minggu 3
            OutlinedButton(
                onClick = { /* TODO: navigasi ke WhitelistScreen */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Aplikasi yang Dipantau")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Log — Minggu 7
            OutlinedButton(
                onClick = { /* TODO: navigasi ke LogScreen */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Riwayat Deteksi")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Ubah PIN — Minggu 8
            OutlinedButton(
                onClick = { /* TODO: navigasi ke ChangePinScreen */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Ubah PIN")
            }

            Spacer(modifier = Modifier.weight(1f))

            // Logout
            OutlinedButton(
                onClick = onLogout,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Keluar")
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
            onLogout = {}
        )
    }
}
