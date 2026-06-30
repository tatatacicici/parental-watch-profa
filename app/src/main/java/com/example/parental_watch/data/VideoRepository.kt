package com.example.parental_watch.data

import android.content.Context
import android.util.Log
import com.example.parental_watch.data.db.AppDatabase
import com.example.parental_watch.data.db.VideoCache
import com.example.parental_watch.data.db.WatchHistory
import com.example.parental_watch.network.ApiClient
import com.example.parental_watch.network.BatchClassifyRequest
import com.example.parental_watch.network.YoutubeApiClient
import com.example.parental_watch.utils.DummyClassifier
import com.google.gson.Gson

data class VideoItem(
    val videoId: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String
)

sealed class GateResult {
    object Clean : GateResult()
    data class Blocked(val reason: String, val ratio: Float = 0f) : GateResult()
    data class Error(val message: String) : GateResult()
}

class VideoRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val cacheDao = db.videoCacheDao()
    private val historyDao = db.watchHistoryDao()
    private val fallbackClassifier = DummyClassifier()
    private val TAG = "VideoRepository"

    // ─── Search ───────────────────────────────────────────────

    suspend fun searchVideos(query: String): List<VideoItem> {
        return try {
            // Step 1: Search
            val searchResponse = YoutubeApiClient.apiService.searchVideos(
                query = query,
                apiKey = YoutubeApiClient.API_KEY
            )

            val items = searchResponse.items.mapNotNull { item ->
                val videoId = item.id.videoId ?: return@mapNotNull null
                VideoItem(
                    videoId = videoId,
                    title = item.snippet.title,
                    channelTitle = item.snippet.channelTitle,
                    thumbnailUrl = item.snippet.thumbnails.medium?.url ?: ""
                )
            }

            if (items.isEmpty()) return emptyList()

            // Step 2: Cek embeddable status — batch satu request
            val videoIds = items.joinToString(",") { it.videoId }
            val detailResponse = YoutubeApiClient.apiService.getVideoDetails(
                videoId = videoIds,
                apiKey = YoutubeApiClient.API_KEY
            )

            val embeddableIds = detailResponse.items
                .filter { it.status.embeddable }
                .map { it.id }
                .toSet()

            Log.d(TAG, "Embeddable: ${embeddableIds.size}/${items.size}")

            // Step 3: Filter hanya yang embeddable
            items.filter { it.videoId in embeddableIds }

        } catch (e: Exception) {
            Log.e(TAG, "Search error: ${e.message}")
            emptyList()
        }
    }

    // ─── Gate 1: Cek Judul ────────────────────────────────────

    suspend fun checkTitle(video: VideoItem): GateResult {
        // Cek cache dulu
        val cached = cacheDao.getValid(video.videoId)
        if (cached != null) {
            Log.d(TAG, "Cache hit gate1: ${video.videoId} → ${cached.gate1Result}")
            return if (cached.gate1Result == "BLOCKED") {
                GateResult.Blocked("TITLE")
            } else {
                GateResult.Clean
            }
        }

        return try {
            val response = ApiClient.apiService.classify(
                com.example.parental_watch.network.ClassifyRequest(video.title)
            )
            if (response.isOffensive) {
                // Simpan cache blocked
                cacheDao.insert(VideoCache(
                    videoId = video.videoId,
                    title = video.title,
                    channelTitle = video.channelTitle,
                    thumbnailUrl = video.thumbnailUrl,
                    gate1Result = "BLOCKED",
                    gate2Result = "SKIP",
                    offensiveRatio = 1f,
                    offensiveWords = Gson().toJson(listOf(video.title))
                ))
                GateResult.Blocked("TITLE")
            } else {
                GateResult.Clean
            }
        } catch (e: Exception) {
            Log.w(TAG, "Gate1 server error, using local fallback: ${e.message}")
            // Fallback: klasifikasi lokal dengan keyword matching
            val localResult = fallbackClassifier.classify(video.title)
            if (localResult.isOffensive) {
                cacheDao.insert(VideoCache(
                    videoId = video.videoId,
                    title = video.title,
                    channelTitle = video.channelTitle,
                    thumbnailUrl = video.thumbnailUrl,
                    gate1Result = "BLOCKED",
                    gate2Result = "SKIP",
                    offensiveRatio = 1f,
                    offensiveWords = Gson().toJson(listOf(video.title))
                ))
                GateResult.Blocked("TITLE")
            } else {
                GateResult.Clean
            }
        }
    }

    // ─── Gate 2: Cek Komentar ─────────────────────────────────

    suspend fun checkComments(video: VideoItem): GateResult {
        // Cek cache dulu
        val cached = cacheDao.getValid(video.videoId)
        if (cached != null && cached.gate2Result != "SKIP") {
            Log.d(TAG, "Cache hit gate2: ${video.videoId} → ${cached.gate2Result}")
            return if (cached.gate2Result == "BLOCKED") {
                GateResult.Blocked("COMMENTS", cached.offensiveRatio)
            } else {
                GateResult.Clean
            }
        }

        return try {
            // Ambil top 20 + recent 20 dengan try-catch terpisah
            val topComments = try {
                YoutubeApiClient.apiService.getTopComments(
                    videoId = video.videoId,
                    apiKey = YoutubeApiClient.API_KEY
                ).items.map { it.snippet.topLevelComment.snippet.textDisplay }
            } catch (e: Exception) {
                Log.w(TAG, "Top comments unavailable: ${e.message}")
                emptyList()
            }

            val recentComments = try {
                YoutubeApiClient.apiService.getRecentComments(
                    videoId = video.videoId,
                    apiKey = YoutubeApiClient.API_KEY
                ).items.map { it.snippet.topLevelComment.snippet.textDisplay }
            } catch (e: Exception) {
                Log.w(TAG, "Recent comments unavailable: ${e.message}")
                emptyList()
            }

            // Kalau komentar tidak bisa diambil sama sekali → fail safe, izinkan play
            if (topComments.isEmpty() && recentComments.isEmpty()) {
                Log.w(TAG, "No comments available for ${video.videoId}, allowing play")
                saveCache(video, gate2Result = "CLEAN", ratio = 0f, offensiveWords = emptyList())
                return GateResult.Clean
            }

            // Gabung + dedup + filter terlalu pendek
            val allComments = (topComments + recentComments)
                .distinct()
                .filter { it.trim().split(" ").size >= 3 }
                .take(40)

            if (allComments.isEmpty()) {
                // Tidak ada komentar setelah filter → anggap clean
                saveCache(video, gate2Result = "CLEAN", ratio = 0f, offensiveWords = emptyList())
                return GateResult.Clean
            }

            // Klasifikasi batch
            val batchResponse = ApiClient.apiService.classifyBatch(
                BatchClassifyRequest(allComments)
            )

            val offensiveCount = batchResponse.results.count { it.isOffensive }
            val ratio = offensiveCount.toFloat() / allComments.size

            Log.d(TAG, "Gate2: ${video.videoId} ratio=$ratio ($offensiveCount/${allComments.size})")

            // Kumpulkan kata offensive untuk logging
            val offensiveWords = allComments.zip(batchResponse.results)
                .filter { (_, result) -> result.isOffensive }
                .map { (text, _) -> text.take(50) } // truncate panjang

            return if (ratio > 0.20f) {
                saveCache(video, gate2Result = "BLOCKED", ratio = ratio, offensiveWords = offensiveWords)
                GateResult.Blocked("COMMENTS", ratio)
            } else {
                saveCache(video, gate2Result = "CLEAN", ratio = ratio, offensiveWords = offensiveWords)
                GateResult.Clean
            }

        } catch (e: Exception) {
            Log.w(TAG, "Gate2 server error, using local fallback: ${e.message}")
            // Fallback: klasifikasi lokal dengan keyword matching untuk setiap komentar
            try {
                val topComments = try {
                    YoutubeApiClient.apiService.getTopComments(
                        videoId = video.videoId,
                        apiKey = YoutubeApiClient.API_KEY
                    ).items.map { it.snippet.topLevelComment.snippet.textDisplay }
                } catch (_: Exception) { emptyList() }

                val recentComments = try {
                    YoutubeApiClient.apiService.getRecentComments(
                        videoId = video.videoId,
                        apiKey = YoutubeApiClient.API_KEY
                    ).items.map { it.snippet.topLevelComment.snippet.textDisplay }
                } catch (_: Exception) { emptyList() }

                val allComments = (topComments + recentComments)
                    .distinct()
                    .filter { it.trim().split(" ").size >= 3 }
                    .take(40)

                if (allComments.isEmpty()) {
                    saveCache(video, gate2Result = "CLEAN", ratio = 0f, offensiveWords = emptyList())
                    return GateResult.Clean
                }

                val localResults = allComments.map { fallbackClassifier.classify(it) }
                val offensiveCount = localResults.count { it.isOffensive }
                val ratio = offensiveCount.toFloat() / allComments.size
                val offensiveWords = allComments.zip(localResults)
                    .filter { (_, result) -> result.isOffensive }
                    .map { (text, _) -> text.take(50) }

                Log.d(TAG, "Gate2 fallback: ${video.videoId} ratio=$ratio ($offensiveCount/${allComments.size})")

                if (ratio > 0.20f) {
                    saveCache(video, gate2Result = "BLOCKED", ratio = ratio, offensiveWords = offensiveWords)
                    GateResult.Blocked("COMMENTS", ratio)
                } else {
                    saveCache(video, gate2Result = "CLEAN", ratio = ratio, offensiveWords = offensiveWords)
                    GateResult.Clean
                }
            } catch (fallbackError: Exception) {
                Log.e(TAG, "Gate2 fallback also failed: ${fallbackError.message}")
                GateResult.Error(fallbackError.message ?: "Unknown error")
            }
        }
    }

    // ─── Rekomendasi Alternatif ───────────────────────────────

    suspend fun getRecommendations(blockedVideo: VideoItem): List<VideoItem> {
        return try {
            // Bersihkan judul dari karakter aneh untuk query
            val cleanQuery = blockedVideo.title
                .replace(Regex("[^\\w\\s]"), " ")
                .trim()
                .take(50)

            val results = searchVideos(cleanQuery)

            // Filter hanya yang judulnya clean, ambil max 3
            val recommendations = mutableListOf<VideoItem>()
            for (video in results) {
                if (video.videoId == blockedVideo.videoId) continue
                val gate1 = checkTitle(video)
                if (gate1 is GateResult.Clean) {
                    recommendations.add(video)
                }
                if (recommendations.size >= 3) break
            }
            recommendations
        } catch (e: Exception) {
            Log.e(TAG, "Recommendation error: ${e.message}")
            emptyList()
        }
    }

    // ─── History ──────────────────────────────────────────────

    suspend fun saveHistory(
        video: VideoItem,
        action: String,
        blockedReason: String = "",
        ratio: Float = 0f
    ) {
        historyDao.insert(WatchHistory(
            videoId = video.videoId,
            title = video.title,
            channelTitle = video.channelTitle,
            thumbnailUrl = video.thumbnailUrl,
            action = action,
            blockedReason = blockedReason,
            offensiveRatio = ratio
        ))
    }

    suspend fun getHistory(): List<WatchHistory> = historyDao.getAll()

    // ─── Helper ───────────────────────────────────────────────

    private suspend fun saveCache(
        video: VideoItem,
        gate2Result: String,
        ratio: Float,
        offensiveWords: List<String>
    ) {
        val existing = cacheDao.getValid(video.videoId)
        cacheDao.insert(VideoCache(
            videoId = video.videoId,
            title = video.title,
            channelTitle = video.channelTitle,
            thumbnailUrl = video.thumbnailUrl,
            gate1Result = existing?.gate1Result ?: "CLEAN",
            gate2Result = gate2Result,
            offensiveRatio = ratio,
            offensiveWords = Gson().toJson(offensiveWords)
        ))
    }
}
