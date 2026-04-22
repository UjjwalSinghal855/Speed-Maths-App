package com.ujjwal.speedmath.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ujjwal.speedmath.data.dao.QuestionRecordDao
import com.ujjwal.speedmath.data.dao.SessionDao
import com.ujjwal.speedmath.data.entity.QuestionRecord
import com.ujjwal.speedmath.data.entity.Session

/**
 * The single Room database for SpeedMath.
 * Singleton pattern with double-checked locking ensures only one instance
 * across the app lifecycle (important for Coroutine safety).
 */
@Database(
    entities = [Session::class, QuestionRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao
    abstract fun questionRecordDao(): QuestionRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "speedmath.db"
                )
                    .fallbackToDestructiveMigration() // Re-create on version bump during dev
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
