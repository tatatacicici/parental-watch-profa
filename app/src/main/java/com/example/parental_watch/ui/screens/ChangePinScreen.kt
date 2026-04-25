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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.parental_watch.data.preference.PreferencesManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePinScreen(
    prefManager: PreferencesManager,
    onBack: () -> Unit
) {
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var newPinConfirm by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Ubah PIN") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Masukkan PIN lama dan PIN baru",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = oldPin,
                onValueChange = { if (it.length <= 8) oldPin = it },
                label = { Text("PIN Lama") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = newPin,
                onValueChange = { if (it.length <= 8) newPin = it },
                label = { Text("PIN Baru (min. 4 digit)") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = newPinConfirm,
                onValueChange = { if (it.length <= 8) newPinConfirm = it },
                label = { Text("Konfirmasi PIN Baru") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    when {
                        oldPin.isEmpty() || newPin.isEmpty() || newPinConfirm.isEmpty() -> {
                            scope.launch { snackbarHostState.showSnackbar("Semua field harus diisi") }
                        }
                        !prefManager.validatePin(oldPin) -> {
                            scope.launch { snackbarHostState.showSnackbar("PIN lama salah") }
                            oldPin = ""
                        }
                        newPin.length < 4 -> {
                            scope.launch { snackbarHostState.showSnackbar("PIN baru minimal 4 digit") }
                        }
                        newPin != newPinConfirm -> {
                            scope.launch { snackbarHostState.showSnackbar("PIN baru tidak cocok") }
                            newPinConfirm = ""
                        }
                        newPin == oldPin -> {
                            scope.launch { snackbarHostState.showSnackbar("PIN baru tidak boleh sama dengan PIN lama") }
                        }
                        else -> {
                            prefManager.savePin(newPin)
                            scope.launch {
                                snackbarHostState.showSnackbar("PIN berhasil diubah")
                            }
                            onBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Simpan PIN Baru")
            }
        }
    }
}