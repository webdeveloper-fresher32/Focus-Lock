package com.ganesh.focuslock.domain.model

enum class StrictnessLevel(val title: String, val description: String) {
    NORMAL(
        title = "Normal",
        description = "Shows warnings and soft blocking screen. Session can be cancelled anytime."
    ),
    STRICT(
        title = "Strict",
        description = "Blocks apps during focus. Pausing and casual cancellation are disabled."
    ),
    MAXIMUM(
        title = "Maximum",
        description = "Full lockdown. Emergency exit requires a 60-second unskippable countdown and typing an accountability pledge."
    )
}

