package com.example.parental_watch.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.parental_watch.data.db.WatchHistory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoHistoryScreen(
    onBack: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val filter by viewModel.filter.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Video") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter == HistoryFilter.ALL,
                    onClick = { viewModel.setFilter(HistoryFilter.ALL) },
                    label = { Text("Semua") }
                )
                FilterChip(
                    selected = filter == HistoryFilter.PLAYED,
                    onClick = { viewModel.setFilter(HistoryFilter.PLAYED) },
                    label = { Text("Ditonton") }
                )
                FilterChip(
                    selected = filter == HistoryFilter.BLOCKED,
                    onClick = { viewModel.setFilter(HistoryFilter.BLOCKED) },
                    label = { Text("Diblokir") }
                )
            }

            when (val s = state) {
                is DashboardState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is DashboardState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Belum ada riwayat video",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is DashboardState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(s.history) { item ->
                            WatchHistoryItem(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WatchHistoryItem(item: WatchHistory) {
    val isBlocked = item.action == "BLOCKED"
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            if (item.thumbnailUrl.isNotBlank()) {
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(width = 100.dp, height = 56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(width = 100.dp, height = 56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.channelTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dateFormat.format(Date(item.watchedAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Status badge
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (isBlocked) Icons.Default.Warning
                                  else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isBlocked) MaterialTheme.colorScheme.error
                           else Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (isBlocked) "Blokir" else "Tonton",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isBlocked) MaterialTheme.colorScheme.error
                            else Color(0xFF4CAF50)
                )
                if (isBlocked && item.offensiveRatio > 0f && item.offensiveRatio < 1f) {
                    Text(
                        text = "${(item.offensiveRatio * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
