package com.example.parental_watch.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "detection_log")
data class LogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
    val appPackage: String,
    val appName: String,
    val label: String,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis()
)