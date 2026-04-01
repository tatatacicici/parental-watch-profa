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

@Composable
fun PinSetupScreen(
    prefManager: PreferencesManager,
    onPinSaved: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var pinConfirm by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }

    ) {innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment =  Alignment.CenterHorizontally
        ){
            Text(
                text = "Buat PIN",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text =  "PIN digunakan untuk mengakses panel orang tua",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = pin,
                onValueChange =  {if (it.length <= 8) pin = it},
                label = {Text("Masukkan PIN (min. 4 digit") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = pinConfirm,
                onValueChange = { if (it.length <= 8) pinConfirm = it },
                label = { Text("Konfirmasi PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    when {
                        pin.length < 4 -> {
                            scope.launch {
                                snackbarHostState.showSnackbar("PIN minimal 4 digit")
                            }
                        }
                        pin != pinConfirm -> {
                            scope.launch {
                                snackbarHostState.showSnackbar("PIN tidak cocok")
                            }
                        }
                        else -> {
                            prefManager.savePin(pin)
                            onPinSaved()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Simpan PIN")
            }

        }

    }
}

@Preview(showBackground = true)
@Composable
fun PinSetupScreenPreview() {
    val context = LocalContext.current
    val prefManager = PreferencesManager(context)
    ParentalWatchTheme {
        PinSetupScreen(
            prefManager = prefManager,
            onPinSaved = {}
        )
    }
}
