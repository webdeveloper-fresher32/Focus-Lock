package com.ganesh.focuslock.domain.model

data class FocusSession(
    val id: Long = 0L,
    val sessionName: String,
    val startTimeEpochMs: Long,
    val targetEndTimeEpochMs: Long,
    val actualEndTimeEpochMs: Long? = null,
    val durationMinutes: Int,
    val strictnessLevel: StrictnessLevel = StrictnessLevel.STRICT,
    val sessionStatus: SessionStatus = SessionStatus.RUNNING,
    val distractionsBlockedCount: Int = 0
) {
    val remainingTimeMs: Long
        get() = maxOf(0L, targetEndTimeEpochMs - System.currentTimeMillis())

    val isFinished: Boolean
        get() = remainingTimeMs <= 0L || sessionStatus != SessionStatus.RUNNING

    val progressFraction: Float
        get() {
            val total = (durationMinutes * 60 * 1000L).coerceAtLeast(1L)
            val elapsed = total - remainingTimeMs
            return (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
        }
}

