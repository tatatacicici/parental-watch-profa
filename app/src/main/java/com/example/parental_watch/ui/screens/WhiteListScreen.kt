package com.example.parental_watch.ui.screens

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.parental_watch.data.preference.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val BLOCKED_FROM_WHITELIST = setOf(
    "com.android.settings",
    "com.android.systemui",
    "com.example.parental_watch"
)

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: android.graphics.drawable.Drawable?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhitelistScreen(
    prefManager: PreferencesManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var appList by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var filteredList by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var checkedPackages by remember { mutableStateOf(prefManager.getWhitelist().toMutableSet()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            // Ambil semua aplikasi, jangan hanya non-system agar WA/Browser muncul
            val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)

            val apps = installed
                .filter { app ->
                    val isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val isUpdatedSystemApp = (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                    
                    // Tampilkan jika: Bukan app sistem penting, ATAU app sistem yang bisa diupdate (WA/Chrome dll)
                    (!isSystemApp || isUpdatedSystemApp || app.packageName.contains("whatsapp", true)) &&
                    BLOCKED_FROM_WHITELIST.none { app.packageName.contains(it) }
                }
                .map { app ->
                    AppInfo(
                        name = pm.getApplicationLabel(app).toString(),
                        packageName = app.packageName,
                        icon = try { pm.getApplicationIcon(app.packageName) } catch (e: Exception) { null }
                    )
                }
                .sortedBy { it.name }

            withContext(Dispatchers.Main) {
                appList = apps
                filteredList = apps
                isLoading = false
            }
        }
    }

    LaunchedEffect(searchQuery, appList) {
        filteredList = if (searchQuery.isEmpty()) {
            appList
        } else {
            appList.filter { 
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.packageName.contains(searchQuery, ignoreCase = true) 
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Aplikasi Terpantau", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = {
                        prefManager.saveWhitelist(checkedPackages)
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            WhitelistHeader(checkedCount = checkedPackages.size)
            SearchBarField(query = searchQuery, onQueryChange = { searchQuery = it })

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredList, key = { it.packageName }) { app ->
                        AppCardItem(
                            app = app,
                            isChecked = checkedPackages.contains(app.packageName),
                            onCheckedChange = { checked ->
                                checkedPackages = checkedPackages.toMutableSet().apply {
                                    if (checked) add(app.packageName) else remove(app.packageName)
                                }
                                prefManager.saveWhitelist(checkedPackages)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ... (Sisanya tetap sama, saya hanya ubah logika filter di atas)

@Composable
fun WhitelistHeader(checkedCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text("Pilih aplikasi yang ingin dipantau.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun SearchBarField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Cari aplikasi...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
fun AppCardItem(app: AppInfo, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        onClick = { onCheckedChange(!isChecked) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            app.icon?.let { drawable ->
                Image(bitmap = drawable.toBitmap(64, 64).asImageBitmap(), contentDescription = null, modifier = Modifier.size(36.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(app.name, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(app.packageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Checkbox(checked = isChecked, onCheckedChange = onCheckedChange)
        }
    }
}
