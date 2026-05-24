package com.example.parental_watch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

    // Step completion
    val isPinFilled = pin.length >= 4 && pin == pinConfirm
    val isQuestionFilled = securityQuestion.isNotBlank() && securityAnswer.isNotBlank()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Header
            Text(
                text = "Setup Keamanan",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Buat PIN dan pertanyaan keamanan\nuntuk melindungi akses panel orang tua.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress stepper
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepDot(completed = isPinFilled, label = "PIN")
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(2.dp)
                        .background(
                            if (isPinFilled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
                StepDot(completed = isQuestionFilled, label = "Keamanan")
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Section 1: PIN
            SetupSectionCard(
                stepNumber = "1",
                title = "Buat PIN Baru",
                icon = Icons.Default.Lock,
                completed = isPinFilled
            ) {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 8) pin = it },
                    label = { Text("PIN Baru (min. 4 digit)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
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
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (pin.isNotEmpty() && pinConfirm.isNotEmpty() && pin != pinConfirm) {
                    Text(
                        text = "PIN tidak cocok",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Security Question
            SetupSectionCard(
                stepNumber = "2",
                title = "Pertanyaan Keamanan",
                icon = Icons.Default.Security,
                completed = isQuestionFilled
            ) {
                Text(
                    text = "Digunakan jika Anda lupa PIN.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                        shape = RoundedCornerShape(14.dp),
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
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save button
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
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = isPinFilled && isQuestionFilled,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = if (isPinFilled && isQuestionFilled)
                                Brush.horizontalGradient(listOf(Color(0xFF3B8BD4), Color(0xFF2A6CB0)))
                            else
                                Brush.horizontalGradient(listOf(Color(0xFFBDBDBD), Color(0xFF9E9E9E))),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Selesaikan Setup",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StepDot(completed: Boolean, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (completed) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            if (completed) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (completed) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (completed) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun SetupSectionCard(
    stepNumber: String,
    title: String,
    icon: ImageVector,
    completed: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (completed) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (completed) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (completed) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            stepNumber,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            content()
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
