package com.example.parental_watch.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: LogEntity)

    @Query("SELECT * FROM detection_log ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<LogEntity>>

    @Query("SELECT * FROM detection_log ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 50): Flow<List<LogEntity>>

    @Query("DELETE FROM detection_log")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM detection_log")
    suspend fun getCount(): Int
}