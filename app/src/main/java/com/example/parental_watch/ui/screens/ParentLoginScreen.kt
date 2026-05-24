package com.example.parental_watch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parental_watch.data.preference.PreferencesManager
import com.example.parental_watch.ui.theme.ParentalWatchTheme
import kotlinx.coroutines.launch

private const val MAX_ATTEMPTS = 5

@Composable
fun ParentLoginScreen(
    prefManager: PreferencesManager,
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var failedAttempts by remember { mutableIntStateOf(0) }
    var isLocked by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Gradient header strip
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF3B8BD4),
                                        Color(0xFF2A6CB0)
                                    )
                                ),
                                shape = RoundedCornerShape(
                                    topStart = 28.dp,
                                    topEnd = 28.dp,
                                    bottomStart = 0.dp,
                                    bottomEnd = 0.dp
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = Color.White
                            )
                        }
                    }

                    // Content
                    Column(
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Login Orang Tua",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Masukkan PIN rahasia Anda",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // PIN dots indicator
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            repeat(6) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (index < pin.length) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outlineVariant
                                        )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = pin,
                            onValueChange = { if (it.length <= 8) pin = it },
                            label = { Text("PIN Keamanan") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            enabled = !isLocked,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Feedback
                        if (failedAttempts > 0 && !isLocked) {
                            Text(
                                text = "Sisa percobaan: ${MAX_ATTEMPTS - failedAttempts}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(start = 4.dp, top = 8.dp)
                            )
                        }

                        if (isLocked) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Akses ditangguhkan. Silakan mulai ulang aplikasi.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (pin.isEmpty()) {
                                    scope.launch { snackbarHostState.showSnackbar("Silakan masukkan PIN") }
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
                                        scope.launch { snackbarHostState.showSnackbar("PIN yang Anda masukkan salah") }
                                    }
                                }
                            },
                            enabled = !isLocked,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = if (!isLocked)
                                            Brush.horizontalGradient(
                                                colors = listOf(Color(0xFF3B8BD4), Color(0xFF2A6CB0))
                                            )
                                        else
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFFBDBDBD), Color(0xFF9E9E9E)
                                                )
                                            ),
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Buka Panel Kontrol",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = onForgotPasswordClick,
                            enabled = !isLocked
                        ) {
                            Text(
                                text = "Lupa PIN?",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
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
            onLoginSuccess = {},
            onForgotPasswordClick = {}
        )
    }
}
