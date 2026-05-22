package com.example.parental_watch.data.db

import androidx.room.*

@Dao
interface WatchHistoryDao {

    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC")
    suspend fun getAll(): List<WatchHistory>

    @Query("SELECT * FROM watch_history WHERE videoId = :videoId ORDER BY watchedAt DESC LIMIT 1")
    suspend fun getByVideoId(videoId: String): WatchHistory?

    @Insert
    suspend fun insert(history: WatchHistory)

    @Query("DELETE FROM watch_history")
    suspend fun deleteAll()
}
