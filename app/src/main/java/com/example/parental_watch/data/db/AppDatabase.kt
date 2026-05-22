package com.example.parental_watch.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [
        LogEntity::class,
        VideoCache::class,
        WatchHistory::class
    ],
    version = 2,                    // naik dari 1 ke 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun logDao(): LogDao
    abstract fun videoCacheDao(): VideoCacheDao
    abstract fun watchHistoryDao(): WatchHistoryDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "parental_watch_db"
                )
                .fallbackToDestructiveMigration() // dev mode — reset kalau schema berubah
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
