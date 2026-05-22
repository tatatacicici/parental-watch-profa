package com.example.parental_watch.ui.player

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun VideoCheckScreen(
    videoId: String,
    title: String,
    channelTitle: String,
    thumbnailUrl: String,
    onApproved: (videoId: String, title: String) -> Unit,
    onBlocked: (videoId: String, title: String, reason: String, ratio: Float) -> Unit,
    viewModel: VideoCheckViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // Trigger check saat screen pertama muncul
    LaunchedEffect(videoId) {
        viewModel.checkVideo(
            com.example.parental_watch.data.VideoItem(
                videoId = videoId,
                title = title,
                channelTitle = channelTitle,
                thumbnailUrl = thumbnailUrl
            )
        )
    }

    // Handle state changes
    LaunchedEffect(state) {
        when (val s = state) {
            is VideoCheckState.Approved -> {
                onApproved(s.video.videoId, s.video.title)
            }
            is VideoCheckState.Blocked -> {
                onBlocked(s.video.videoId, s.video.title, s.reason, s.ratio)
            }
            else -> {}
        }
    }

    // UI — loading screen
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            CircularProgressIndicator()

            Text(
                text = when (state) {
                    is VideoCheckState.CheckingTitle -> "Memeriksa judul video..."
                    is VideoCheckState.CheckingComments -> "Memeriksa komentar video..."
                    else -> "Memproses..."
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            if (title.isNotBlank()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}
