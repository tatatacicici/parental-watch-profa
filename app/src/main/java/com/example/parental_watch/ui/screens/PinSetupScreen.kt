package com.example.parental_watch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSetupScreen(
    prefManager: PreferencesManager,
    onPinSaved: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var pinConfirm by remember { mutableStateOf("") }
    var securityQuestion by remember { mutableStateOf("") }
    var securityAnswer by remember { mutableStateOf("") }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val questions = listOf(
        "Apa nama hewan peliharaan pertama Anda?",
        "Di kota mana Anda lahir?",
        "Apa nama sekolah dasar Anda?",
        "Siapa nama gadis ibu kandung Anda?",
        "Apa merek mobil pertama Anda?"
    )
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = "Setup Keamanan",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Buat PIN dan pertanyaan keamanan untuk melindungi akses panel orang tua.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // --- Bagian PIN ---
            Text(
                text = "1. Buat PIN Baru",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.align(Alignment.Start),
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 8) pin = it },
                label = { Text("PIN Baru (min. 4 digit)") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = pinConfirm,
                onValueChange = { if (it.length <= 8) pinConfirm = it },
                label = { Text("Konfirmasi PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Bagian Challenge ---
            Text(
                text = "2. Pertanyaan Keamanan (Challenge)",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.align(Alignment.Start),
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Digunakan jika Anda lupa PIN.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = securityQuestion,
                    onValueChange = { securityQuestion = it },
                    label = { Text("Pilih Pertanyaan") },
                    readOnly = false,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    questions.forEach { question ->
                        DropdownMenuItem(
                            text = { Text(question) },
                            onClick = {
                                securityQuestion = question
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = securityAnswer,
                onValueChange = { securityAnswer = it },
                label = { Text("Jawaban Keamanan") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    when {
                        pin.length < 4 -> {
                            scope.launch { snackbarHostState.showSnackbar("PIN minimal 4 digit") }
                        }
                        pin != pinConfirm -> {
                            scope.launch { snackbarHostState.showSnackbar("Konfirmasi PIN tidak cocok") }
                        }
                        securityQuestion.isBlank() || securityAnswer.isBlank() -> {
                            scope.launch { snackbarHostState.showSnackbar("Harap isi pertanyaan & jawaban keamanan") }
                        }
                        else -> {
                            prefManager.savePin(pin)
                            prefManager.saveSecurityQuestion(securityQuestion, securityAnswer)
                            onPinSaved()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Selesaikan Setup", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
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
