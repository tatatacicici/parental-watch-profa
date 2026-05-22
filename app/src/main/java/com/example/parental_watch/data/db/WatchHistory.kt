package com.example.parental_watch.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val videoId: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val action: String,             // "PLAYED" atau "BLOCKED"
    val blockedReason: String = "", // "TITLE" atau "COMMENTS"
    val offensiveRatio: Float = 0f,
    val watchedAt: Long = System.currentTimeMillis()
)
