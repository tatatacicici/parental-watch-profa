package com.example.parental_watch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.parental_watch.data.preference.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScheduleScreen(
    prefManager: PreferencesManager,
    onBack: () -> Unit
) {
    var enabled by remember { mutableStateOf(prefManager.isStudyModeEnabled()) }

    var startHour by remember { mutableStateOf("%02d".format(prefManager.getStudyStartHour())) }
    var startMinute by remember { mutableStateOf("%02d".format(prefManager.getStudyStartMinute())) }
    var endHour by remember { mutableStateOf("%02d".format(prefManager.getStudyEndHour())) }
    var endMinute by remember { mutableStateOf("%02d".format(prefManager.getStudyEndMinute())) }

    var message by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Jam Belajar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mode Jam Belajar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kunci fitur pencarian dan pemutaran video pada jam tertentu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = enabled,
                        onCheckedChange = { enabled = it }
                    )
                }
            }

            Text(
                text = "Waktu Mulai",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TimeInputField(
                    value = startHour,
                    onValueChange = { startHour = it },
                    label = "Jam",
                    modifier = Modifier.weight(1f)
                )
                Text(":", style = MaterialTheme.typography.headlineMedium)
                TimeInputField(
                    value = startMinute,
                    onValueChange = { startMinute = it },
                    label = "Menit",
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Waktu Selesai",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TimeInputField(
                    value = endHour,
                    onValueChange = { endHour = it },
                    label = "Jam",
                    modifier = Modifier.weight(1f)
                )
                Text(":", style = MaterialTheme.typography.headlineMedium)
                TimeInputField(
                    value = endMinute,
                    onValueChange = { endMinute = it },
                    label = "Menit",
                    modifier = Modifier.weight(1f)
                )
            }

            if (message.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.startsWith("Tersimpan")) 
                            Color(0xFFE8F5E9) else Color(0xFFFDECEA)
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (message.startsWith("Tersimpan")) 
                            Color(0xFF2E7D32) else Color(0xFFD32F2F)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                onClick = {
                    val sh = startHour.toIntOrNull()
                    val sm = startMinute.toIntOrNull()
                    val eh = endHour.toIntOrNull()
                    val em = endMinute.toIntOrNull()

                    if (sh == null || sm == null || eh == null || em == null) {
                        message = "Input harus berupa angka."
                        return@Button
                    }

                    if (sh !in 0..23 || eh !in 0..23 || sm !in 0..59 || em !in 0..59) {
                        message = "Jam harus 0-23 dan menit harus 0-59."
                        return@Button
                    }

                    prefManager.setStudyModeEnabled(enabled)
                    prefManager.saveStudySchedule(sh, sm, eh, em)

                    message = "Tersimpan: %02d:%02d - %02d:%02d".format(sh, sm, eh, em)
                }
            ) {
                Text("Simpan Pengaturan", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TimeInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val filtered = newValue.filter { it.isDigit() }.take(2)
            onValueChange(filtered)
        },
        label = { Text(label) },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    )
}
