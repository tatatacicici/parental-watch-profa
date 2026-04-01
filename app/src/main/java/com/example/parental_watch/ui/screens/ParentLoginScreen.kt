package com.example.parental_watch.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.parental_watch.data.preference.PreferencesManager
import com.example.parental_watch.ui.theme.ParentalWatchTheme
import kotlinx.coroutines.launch

private const val MAX_ATTEMPTS = 5

@Composable
fun ParentLoginScreen(
    prefManager: PreferencesManager,
    onLoginSuccess: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var failedAttempts by remember { mutableIntStateOf(0) }
    var isLocked by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Login Orang Tua",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Masukkan PIN untuk melanjutkan",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 8) pin = it },
                label = { Text("PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                enabled = !isLocked,
                modifier = Modifier.fillMaxWidth()
            )

            // Tampilkan sisa percobaan kalau sudah pernah salah
            if (failedAttempts > 0 && !isLocked) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sisa percobaan: ${MAX_ATTEMPTS - failedAttempts}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (isLocked) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Terlalu banyak percobaan. Tutup dan buka ulang aplikasi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (pin.isEmpty()) {
                        scope.launch { snackbarHostState.showSnackbar("Masukkan PIN") }
                        return@Button
                    }

                    if (prefManager.validatePin(pin)) {
                        failedAttempts = 0
                        onLoginSuccess()
                    } else {
                        failedAttempts++
                        pin = ""

                        if (failedAttempts >= MAX_ATTEMPTS) {
                            isLocked = true
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("PIN salah")
                            }
                        }
                    }
                },
                enabled = !isLocked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Masuk")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ParentLoginScreenPreview() {
    val context = LocalContext.current
    val prefManager = PreferencesManager(context)
    ParentalWatchTheme {
        ParentLoginScreen(
            prefManager = prefManager,
            onLoginSuccess = {}
        )
    }
}
