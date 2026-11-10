package com.ganesh.focuslock.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ganesh.focuslock.data.local.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions WHERE sessionStatus = 'RUNNING' AND targetEndTimeEpochMs > :currentTimeMillis ORDER BY id DESC LIMIT 1")
    fun getActiveSession(currentTimeMillis: Long): Flow<FocusSessionEntity?>

    @Query("SELECT * FROM focus_sessions WHERE sessionStatus = 'RUNNING' AND targetEndTimeEpochMs > :currentTimeMillis ORDER BY id DESC LIMIT 1")
    suspend fun getActiveSessionSync(currentTimeMillis: Long): FocusSessionEntity?

    @Query("UPDATE focus_sessions SET sessionStatus = 'COMPLETED', actualEndTimeEpochMs = targetEndTimeEpochMs WHERE sessionStatus = 'RUNNING' AND targetEndTimeEpochMs <= :currentTimeMillis")
    suspend fun cleanUpExpiredSessions(currentTimeMillis: Long)

    @Query("SELECT * FROM focus_sessions ORDER BY id DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Update
    suspend fun updateSession(session: FocusSessionEntity)

    @Query("UPDATE focus_sessions SET distractionsBlockedCount = distractionsBlockedCount + 1 WHERE id = :sessionId")
    suspend fun incrementDistractionCount(sessionId: Long)

    @Query("UPDATE focus_sessions SET actualEndTimeEpochMs = :actualEndTime, sessionStatus = :status WHERE id = :id")
    suspend fun finishSession(id: Long, actualEndTime: Long, status: String)

    @Query("SELECT SUM(distractionsBlockedCount) FROM focus_sessions")
    fun getTotalDistractionsBlocked(): Flow<Int?>

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions WHERE sessionStatus = 'COMPLETED'")
    fun getTotalFocusMinutes(): Flow<Int?>
}
