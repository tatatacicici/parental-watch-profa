package com.example.parental_watch.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_cache")
data class VideoCache(
    @PrimaryKey
    val videoId: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val gate1Result: String,        // "CLEAN" atau "BLOCKED"
    val gate2Result: String,        // "CLEAN", "BLOCKED", atau "SKIP"
    val offensiveRatio: Float,
    val offensiveWords: String,     // JSON array sebagai string
    val cachedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 86_400_000L // 24 jam
)
