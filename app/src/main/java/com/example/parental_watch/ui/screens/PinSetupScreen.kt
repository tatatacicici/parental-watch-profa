package com.example.parental_watch.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlinx.coroutines.delay
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

    // ── Entry Animations ──────────────────────────────────────
    val headerIconScale = remember { Animatable(0.8f) }
    val headerIconAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val stepperAlpha = remember { Animatable(0f) }
    val card1Alpha = remember { Animatable(0f) }
    val card1OffsetY = remember { Animatable(20f) }
    val card2Alpha = remember { Animatable(0f) }
    val card2OffsetY = remember { Animatable(20f) }
    val ctaAlpha = remember { Animatable(0f) }

    // Header icon: scale + fade
    LaunchedEffect(Unit) {
        headerIconScale.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        headerIconAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
    }
    // Title + subtitle: fade
    LaunchedEffect(Unit) {
        delay(150)
        titleAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
    }
    // Progress stepper: fade
    LaunchedEffect(Unit) {
        delay(300)
        stepperAlpha.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
    }
    // Card 1: fade + slide up
    LaunchedEffect(Unit) {
        delay(400)
        card1Alpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(400)
        card1OffsetY.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
    }
    // Card 2: fade + slide up
    LaunchedEffect(Unit) {
        delay(550)
        card2Alpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(550)
        card2OffsetY.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
    }
    // CTA: fade
    LaunchedEffect(Unit) {
        delay(700)
        ctaAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
    }

    // ── Idle Animations (Decorative) ──────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "decoPinTransition")
    val cloudOffsetX1 by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pinCloud1"
    )
    val cloudOffsetX2 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pinCloud2"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ── Decorative Bokeh Circles ──────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 12.dp, y = 40.dp)
                    .graphicsLayer { translationX = cloudOffsetX1 }
                    .size(70.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-8).dp, y = 100.dp)
                    .graphicsLayer { translationX = cloudOffsetX2 }
                    .size(45.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 24.dp, y = (-120).dp)
                    .size(35.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                        shape = CircleShape
                    )
            )

            // ── Main Content ──────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Header Icon — visual anchor (NEW)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .scale(headerIconScale.value)
                        .alpha(headerIconAlpha.value)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Header
                Text(
                    text = "Setup Keamanan",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.alpha(titleAlpha.value)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Buat PIN dan pertanyaan keamanan\nuntuk melindungi akses panel orang tua.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(titleAlpha.value)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Progress stepper
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .alpha(stepperAlpha.value),
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
                Box(
                    modifier = Modifier
                        .alpha(card1Alpha.value)
                        .graphicsLayer { translationY = card1OffsetY.value }
                ) {
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
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Security Question
                Box(
                    modifier = Modifier
                        .alpha(card2Alpha.value)
                        .graphicsLayer { translationY = card2OffsetY.value }
                ) {
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
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Save button — 64dp konsisten dengan HomeScreen & GoogleLogin
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
                        .height(64.dp)
                        .alpha(ctaAlpha.value),
                    shape = RoundedCornerShape(20.dp),
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
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Selesaikan Setup",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
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
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
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
