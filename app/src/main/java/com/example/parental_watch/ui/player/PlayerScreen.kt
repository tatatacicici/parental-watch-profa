package com.example.parental_watch.ui.player

import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.example.parental_watch.ui.theme.ParentalWatchTheme
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    videoId: String,
    title: String,
    onRelatedVideoClicked: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val youTubePlayerView = remember(videoId) {
        YouTubePlayerView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            enableAutomaticInitialization = false

            val options = IFramePlayerOptions.Builder(context)
                .controls(1)
                .rel(0)
                .ivLoadPolicy(3)
                .fullscreen(0) // Fullscreen dinonaktifkan untuk mencegah force close
                .build()

            val listener = object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    Log.d("YT_PLAYER", "Player ready: $videoId")

                    // Debug dulu: cueVideo tidak autoplay.
                    youTubePlayer.cueVideo(videoId, 0f)

                    // Kalau nanti sudah tampil normal, boleh ganti ke:
                    // youTubePlayer.loadVideo(videoId, 0f)
                }

                override fun onStateChange(
                    youTubePlayer: YouTubePlayer,
                    state: PlayerConstants.PlayerState
                ) {
                    Log.d("YT_PLAYER", "State: $state")
                }

                override fun onError(
                    youTubePlayer: YouTubePlayer,
                    error: PlayerConstants.PlayerError
                ) {
                    Log.e("YT_PLAYER", "Error: $error")
                }

                override fun onVideoId(
                    youTubePlayer: YouTubePlayer,
                    loadedVideoId: String
                ) {
                    Log.d("YT_PLAYER", "Loaded videoId: $loadedVideoId")

                    if (loadedVideoId != videoId) {
                        Log.w("YT_PLAYER", "Related video clicked: $loadedVideoId")
                        onRelatedVideoClicked(loadedVideoId)
                        youTubePlayer.pause()
                    }
                }
            }

            initialize(listener, options)
        }
    }

    DisposableEffect(youTubePlayerView, lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(youTubePlayerView)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(youTubePlayerView)
            youTubePlayerView.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
        }
    ) { padding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            factory = {
                youTubePlayerView
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PlayerScreenPreview() {
    ParentalWatchTheme {
        // PlayerScreen menggunakan AndroidView (YouTubePlayerView)
        // Preview hanya menampilkan skeleton Scaffold
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Preview: Video Player") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("YouTube Player (tidak tersedia di Preview)")
            }
        }
    }
}