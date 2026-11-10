package com.ganesh.focuslock.data.repository

import com.ganesh.focuslock.data.local.dao.FocusSessionDao
import com.ganesh.focuslock.data.local.entity.FocusSessionEntity
import com.ganesh.focuslock.domain.model.FocusSession
import com.ganesh.focuslock.domain.model.SessionStatus
import com.ganesh.focuslock.domain.model.StrictnessLevel
import com.ganesh.focuslock.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SessionRepositoryImpl(
    private val sessionDao: FocusSessionDao
) : SessionRepository {

    override fun getActiveSession(): Flow<FocusSession?> {
        return sessionDao.getActiveSession().map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun getActiveSessionSync(): FocusSession? {
        return withContext(Dispatchers.IO) {
            sessionDao.getActiveSessionSync()?.toDomain()
        }
    }

    override fun getAllSessions(): Flow<List<FocusSession>> {
        return sessionDao.getAllSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTotalDistractionsBlocked(): Flow<Int> {
        return sessionDao.getTotalDistractionsBlocked().map { it ?: 0 }
    }

    override fun getTotalFocusMinutes(): Flow<Int> {
        return sessionDao.getTotalFocusMinutes().map { it ?: 0 }
    }

    override suspend fun startSession(
        name: String,
        durationMinutes: Int,
        strictness: StrictnessLevel
    ): Long {
        return withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val targetEnd = now + (durationMinutes * 60 * 1000L)
            val entity = FocusSessionEntity(
                sessionName = name,
                startTimeEpochMs = now,
                targetEndTimeEpochMs = targetEnd,
                durationMinutes = durationMinutes,
                strictnessLevel = strictness.name,
                sessionStatus = SessionStatus.RUNNING.name
            )
            sessionDao.insertSession(entity)
        }
    }

    override suspend fun endSession(id: Long, status: SessionStatus) {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            sessionDao.finishSession(id, now, status.name)
        }
    }

    override suspend fun incrementDistractionsBlocked(id: Long) {
        withContext(Dispatchers.IO) {
            sessionDao.incrementDistractionCount(id)
        }
    }

    private fun FocusSessionEntity.toDomain() = FocusSession(
        id = id,
        sessionName = sessionName,
        startTimeEpochMs = startTimeEpochMs,
        targetEndTimeEpochMs = targetEndTimeEpochMs,
        actualEndTimeEpochMs = actualEndTimeEpochMs,
        durationMinutes = durationMinutes,
        strictnessLevel = try {
            StrictnessLevel.valueOf(strictnessLevel)
        } catch (_: Exception) {
            StrictnessLevel.STRICT
        },
        sessionStatus = try {
            SessionStatus.valueOf(sessionStatus)
        } catch (_: Exception) {
            SessionStatus.COMPLETED
        },
        distractionsBlockedCount = distractionsBlockedCount
    )
}

