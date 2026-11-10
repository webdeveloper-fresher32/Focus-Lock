package com.ganesh.focuslock.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ganesh.focuslock.data.local.dao.AppDao
import com.ganesh.focuslock.data.local.dao.DailyUsageDao
import com.ganesh.focuslock.data.local.dao.FocusSessionDao
import com.ganesh.focuslock.data.local.entity.DailyUsageEntity
import com.ganesh.focuslock.data.local.entity.FocusSessionEntity
import com.ganesh.focuslock.data.local.entity.RestrictedAppEntity

@Database(
    entities = [
        RestrictedAppEntity::class,
        FocusSessionEntity::class,
        DailyUsageEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FocusLockDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun dailyUsageDao(): DailyUsageDao

    companion object {
        @Volatile
        private var INSTANCE: FocusLockDatabase? = null

        fun getInstance(context: Context): FocusLockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FocusLockDatabase::class.java,
                    "focuslock_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
