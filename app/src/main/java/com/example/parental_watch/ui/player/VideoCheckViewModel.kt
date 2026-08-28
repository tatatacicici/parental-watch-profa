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
            Log.d("LATENCY_TEST", "========================================")
            Log.d("LATENCY_TEST", "Mulai pengecekan video: ${video.videoId}")
            Log.d("LATENCY_TEST", "Judul: ${video.title}")

            // ── Gate 1 — cek judul ──────────────────────────────
            _state.value = VideoCheckState.CheckingTitle
            val gate1Start = System.currentTimeMillis()
            val gate1 = repository.checkTitle(video)
            val gate1Time = System.currentTimeMillis() - gate1Start
            Log.d("LATENCY_TEST", "Gate 1 (Title) selesai: ${gate1Time} ms → ${gate1::class.simpleName}")

            when (gate1) {
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
                    
                    val totalTime = System.currentTimeMillis() - startTime
                    Log.d("LATENCY_TEST", "HASIL: DIBLOKIR di Gate 1")
                    Log.d("LATENCY_TEST", "WAKTU: Total=${totalTime}ms | G1=${gate1Time}ms | G2=SKIP")
                    Log.d("LATENCY_TEST", "========================================")
                    return@launch
                }
                is GateResult.Error -> {
                    Log.d("LATENCY_TEST", "Gate 1 ERROR, lanjut ke Gate 2 (fail open)")
                    // Kalau error, lanjut ke gate 2 — fail open
                    // supaya tidak block video tanpa alasan
                }
                is GateResult.Clean -> { /* lanjut */ }
            }

            // ── Gate 2 — cek komentar ───────────────────────────
            _state.value = VideoCheckState.CheckingComments
            val gate2Start = System.currentTimeMillis()
            val gate2 = repository.checkComments(video)
            val gate2Time = System.currentTimeMillis() - gate2Start
            Log.d("LATENCY_TEST", "Gate 2 (Comments) selesai: ${gate2Time} ms → ${gate2::class.simpleName}")

            when (gate2) {
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
                    val totalTime = System.currentTimeMillis() - startTime
                    Log.d("LATENCY_TEST", "HASIL: DIBLOKIR di Gate 2 (ratio=${gate2.ratio})")
                    Log.d("LATENCY_TEST", "WAKTU: Total=${totalTime}ms | G1=${gate1Time}ms | G2=${gate2Time}ms")
                    Log.d("LATENCY_TEST", "========================================")
                }
                is GateResult.Clean -> {
                    repository.saveHistory(
                        video = video,
                        action = "PLAYED"
                    )
                    _state.value = VideoCheckState.Approved(video)
                    val totalTime = System.currentTimeMillis() - startTime
                    Log.d("LATENCY_TEST", "HASIL: AMAN (lolos Gate 1 + Gate 2)")
                    Log.d("LATENCY_TEST", "WAKTU: Total=${totalTime}ms | G1=${gate1Time}ms | G2=${gate2Time}ms")
                    Log.d("LATENCY_TEST", "========================================")
                }
                is GateResult.Error -> {
                    // Kalau error di gate 2, fail open — izinkan play
                    repository.saveHistory(video = video, action = "PLAYED")
                    _state.value = VideoCheckState.Approved(video)
                    val totalTime = System.currentTimeMillis() - startTime
                    Log.d("LATENCY_TEST", "HASIL: ERROR Gate 2 → FAIL OPEN (izinkan play)")
                    Log.d("LATENCY_TEST", "WAKTU: Total=${totalTime}ms | G1=${gate1Time}ms | G2=${gate2Time}ms")
                    Log.d("LATENCY_TEST", "========================================")
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
