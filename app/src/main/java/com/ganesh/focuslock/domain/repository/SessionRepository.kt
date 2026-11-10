package com.ganesh.focuslock.domain.repository

import com.ganesh.focuslock.domain.model.FocusSession
import com.ganesh.focuslock.domain.model.SessionStatus
import com.ganesh.focuslock.domain.model.StrictnessLevel
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getActiveSession(): Flow<FocusSession?>
    suspend fun getActiveSessionSync(): FocusSession?
    suspend fun cleanupExpiredSessions()
    fun getAllSessions(): Flow<List<FocusSession>>
    fun getTotalDistractionsBlocked(): Flow<Int>
    fun getTotalFocusMinutes(): Flow<Int>
    suspend fun startSession(
        name: String,
        durationMinutes: Int,
        strictness: StrictnessLevel
    ): Long
    suspend fun endSession(id: Long, status: SessionStatus)
    suspend fun incrementDistractionsBlocked(id: Long)
}
