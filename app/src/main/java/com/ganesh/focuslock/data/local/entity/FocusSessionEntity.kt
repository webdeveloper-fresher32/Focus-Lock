package com.ganesh.focuslock.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val sessionName: String,
    val startTimeEpochMs: Long,
    val targetEndTimeEpochMs: Long,
    val actualEndTimeEpochMs: Long? = null,
    val durationMinutes: Int,
    val strictnessLevel: String,
    val sessionStatus: String,
    val distractionsBlockedCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

