package com.ganesh.focuslock.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_usage")
data class DailyUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val packageName: String,
    val dateString: String,
    val timeSpentEpochMs: Long,
    val lastUpdatedEpochMs: Long = System.currentTimeMillis()
)

