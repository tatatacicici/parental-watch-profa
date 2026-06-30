package com.example.parental_watch.ui.player

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.parental_watch.data.GateResult
import com.example.parental_watch.data.VideoItem
import com.example.parental_watch.data.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class VideoCheckState {
    object Idle : VideoCheckState()
    object CheckingTitle : VideoCheckState()
    object CheckingComments : VideoCheckState()
    data class Approved(val video: VideoItem) : VideoCheckState()
    data class Blocked(
        val video: VideoItem,
        val reason: String,
        val ratio: Float,
        val recommendations: List<VideoItem>
    ) : VideoCheckState()
    data class Error(val message: String) : VideoCheckState()
    object LoadingRecommendations : VideoCheckState()
    data class RecommendationsLoaded(val recommendations: List<VideoItem>) : VideoCheckState()
}

class VideoCheckViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = VideoRepository(app)

    private val _state = MutableStateFlow<VideoCheckState>(VideoCheckState.Idle)
    val state: StateFlow<VideoCheckState> = _state

    fun checkVideo(video: VideoItem) {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            Log.d("LATENCY_TEST", "Mulai pengecekan video: ${video.videoId}")

            // Gate 1 — cek judul
            _state.value = VideoCheckState.CheckingTitle

            when (val gate1 = repository.checkTitle(video)) {
                is GateResult.Blocked -> {
                    repository.saveHistory(
                        video = video,
                        action = "BLOCKED",
                        blockedReason = "TITLE",
                        ratio = 1f
                    )
                    val recommendations = repository.getRecommendations(video)
                    _state.value = VideoCheckState.Blocked(
                        video = video,
                        reason = "Judul video mengandung konten tidak pantas",
                        ratio = 1f,
                        recommendations = recommendations
                    )
                    
                    val endTime = System.currentTimeMillis()
                    Log.d("LATENCY_TEST", "Selesai (DIBLOKIR di Gate 1). Latensi: ${endTime - startTime} ms")
                    return@launch
                }
                is GateResult.Error -> {
                    // Kalau error, lanjut ke gate 2 — fail open
                    // supaya tidak block video tanpa alasan
                }
                is GateResult.Clean -> { /* lanjut */ }
            }

            // Gate 2 — cek komentar
            _state.value = VideoCheckState.CheckingComments

            when (val gate2 = repository.checkComments(video)) {
                is GateResult.Blocked -> {
                    repository.saveHistory(
                        video = video,
                        action = "BLOCKED",
                        blockedReason = "COMMENTS",
                        ratio = gate2.ratio
                    )
                    val recommendations = repository.getRecommendations(video)
                    _state.value = VideoCheckState.Blocked(
                        video = video,
                        reason = "Komentar video mengandung ${(gate2.ratio * 100).toInt()}% konten tidak pantas",
                        ratio = gate2.ratio,
                        recommendations = recommendations
                    )
                    val endTime = System.currentTimeMillis()
                    Log.d("LATENCY_TEST", "Selesai (DIBLOKIR di Gate 2). Latensi: ${endTime - startTime} ms")
                }
                is GateResult.Clean -> {
                    repository.saveHistory(
                        video = video,
                        action = "PLAYED"
                    )
                    _state.value = VideoCheckState.Approved(video)
                    val endTime = System.currentTimeMillis()
                    Log.d("LATENCY_TEST", "Selesai (AMAN di Gate 2). Latensi: ${endTime - startTime} ms")
                }
                is GateResult.Error -> {
                    // Kalau error di gate 2, fail open — izinkan play
                    repository.saveHistory(video = video, action = "PLAYED")
                    _state.value = VideoCheckState.Approved(video)
                    val endTime = System.currentTimeMillis()
                    Log.d("LATENCY_TEST", "Selesai (ERROR Gate 2, FAIL OPEN). Latensi: ${endTime - startTime} ms")
                }
            }
        }
    }

    fun loadRecommendations(video: VideoItem) {
        viewModelScope.launch {
            _state.value = VideoCheckState.LoadingRecommendations
            val recommendations = repository.getRecommendations(video)
            _state.value = VideoCheckState.RecommendationsLoaded(recommendations)
        }
    }

    // Dipanggil saat user klik related video di WebView
    fun checkRelatedVideo(videoId: String, title: String, channelTitle: String) {
        val video = VideoItem(
            videoId = videoId,
            title = title,
            channelTitle = channelTitle,
            thumbnailUrl = ""
        )
        checkVideo(video)
    }

    fun reset() {
        _state.value = VideoCheckState.Idle
    }
}
