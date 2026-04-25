package com.example.parental_watch.data

import android.content.Context
import com.example.parental_watch.data.db.AppDatabase
import com.example.parental_watch.data.db.LogEntity
import kotlinx.coroutines.flow.Flow

class LogRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).logDao()

    suspend fun insert(log: LogEntity) = dao.insert(log)

    fun getAllLogs(): Flow<List<LogEntity>> = dao.getAllLogs()

    fun getRecentLogs(limit: Int = 50): Flow<List<LogEntity>> =
        dao.getRecentLogs(limit)

    suspend fun clearAll() = dao.clearAll()

    suspend fun getCount(): Int = dao.getCount()
}