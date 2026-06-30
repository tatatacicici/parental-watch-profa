package com.example.parental_watch.ui.player

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.parental_watch.data.VideoItem
import com.example.parental_watch.ui.search.VideoListItem
import com.example.parental_watch.ui.theme.ParentalWatchTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockScreen(
    videoId: String,
    title: String,
    reason: String,
    ratio: Float,
    onRecommendationSelected: (VideoItem) -> Unit,
    onBack: () -> Unit,
    viewModel: VideoCheckViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // Load rekomendasi
    LaunchedEffect(videoId) {
        viewModel.loadRecommendations(
            VideoItem(
                videoId = videoId,
                title = title,
                channelTitle = "",
                thumbnailUrl = ""
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Video Diblokir") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Block info card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Video Tidak Tersedia",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )

                        if (ratio > 0f && ratio < 1f) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${(ratio * 100).toInt()}% komentar tidak pantas",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Rekomendasi
            item {
                Text(
                    text = "Video Alternatif",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            when (val s = state) {
                is VideoCheckState.LoadingRecommendations -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is VideoCheckState.RecommendationsLoaded -> {
                    if (s.recommendations.isEmpty()) {
                        item {
                            Text(
                                text = "Tidak ada rekomendasi tersedia",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(s.recommendations) { video ->
                            VideoListItem(
                                video = video,
                                onClick = { onRecommendationSelected(video) }
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BlockScreenPreview() {
    ParentalWatchTheme {
        BlockScreen(
            videoId = "test123",
            title = "Video Contoh",
            reason = "Konten tidak pantas terdeteksi pada komentar video.",
            ratio = 0.35f,
            onRecommendationSelected = {},
            onBack = {}
        )
    }
}
