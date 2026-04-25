package com.example.parental_watch.ui.screens

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.parental_watch.data.preference.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// App yang tidak boleh dimasukkan whitelist (sensitif / keamanan tinggi)
private val BLOCKED_FROM_WHITELIST = setOf(
    "com.bca",
    "com.mandiri",
    "id.co.bri.brimo",
    "com.bni.android",
    "com.cimb",
    "id.co.bankjatim",
    "com.danamon",
    "com.btpn",
    "id.dana",
    "com.gojek.app",        // GoJek (ada GoPay)
    "com.shopee.id",
    "com.tokopedia.tkpd",
    "id.co.bcamobile",
    "com.google.android.gms" // Google Play Services
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
    var checkedPackages by remember { mutableStateOf(prefManager.getWhitelist().toMutableSet()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)

            val userApps = installed
                .filter { app ->
                    // Hanya user app, bukan system, dan tidak ada di blocked list
                    (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0 &&
                            BLOCKED_FROM_WHITELIST.none { blocked ->
                                app.packageName.contains(blocked, ignoreCase = true)
                            } &&
                            app.packageName != context.packageName // Exclude diri sendiri
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
                appList = userApps
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aplikasi yang Dipantau") },
                navigationIcon = {
                    IconButton(onClick = {
                        prefManager.saveWhitelist(checkedPackages)
                        onBack()
                    }) { Text("←") }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Memuat daftar aplikasi...")
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Text(
                    text = "${checkedPackages.size} dari ${appList.size} aplikasi dipantau",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                HorizontalDivider()

                LazyColumn {
                    items(appList) { app ->
                        AppListItem(
                            app = app,
                            isChecked = checkedPackages.contains(app.packageName),
                            onCheckedChange = { checked ->
                                checkedPackages = checkedPackages.toMutableSet().apply {
                                    if (checked) add(app.packageName) else remove(app.packageName)
                                }
                                prefManager.saveWhitelist(checkedPackages)
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AppListItem(
    app: AppInfo,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        app.icon?.let { drawable ->
            Image(
                bitmap = drawable.toBitmap(48, 48).asImageBitmap(),
                contentDescription = app.name,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}