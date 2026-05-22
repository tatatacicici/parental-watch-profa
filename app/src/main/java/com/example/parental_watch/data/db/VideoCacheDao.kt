package com.example.parental_watch.data.db

import androidx.room.*

@Dao
interface VideoCacheDao {

    @Query("SELECT * FROM video_cache WHERE videoId = :videoId AND expiresAt > :now")
    suspend fun getValid(videoId: String, now: Long = System.currentTimeMillis()): VideoCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cache: VideoCache)

    @Query("DELETE FROM video_cache WHERE expiresAt < :now")
    suspend fun deleteExpired(now: Long = System.currentTimeMillis())
}
