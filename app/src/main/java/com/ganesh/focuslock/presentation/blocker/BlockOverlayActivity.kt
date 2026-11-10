package com.ganesh.focuslock.presentation.blocker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import com.ganesh.focuslock.FocusLockApp
import com.ganesh.focuslock.MainActivity
import com.ganesh.focuslock.core.theme.FocusLockTheme
import com.ganesh.focuslock.domain.model.SessionStatus
import com.ganesh.focuslock.domain.model.StrictnessLevel
import com.ganesh.focuslock.service.FocusSessionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BlockOverlayActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "EXTRA_PACKAGE_NAME"
        const val EXTRA_REASON = "EXTRA_REASON"
        const val EXTRA_SESSION_ID = "EXTRA_SESSION_ID"
        const val EXTRA_SESSION_NAME = "EXTRA_SESSION_NAME"
        const val EXTRA_TARGET_END_MS = "EXTRA_TARGET_END_MS"
        const val EXTRA_STRICTNESS = "EXTRA_STRICTNESS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Disable standard back button from dismissing overlay into restricted app
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                returnToHome()
            }
        })

        val restrictedPackage = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: "Distracting App"
        val reason = intent.getStringExtra(EXTRA_REASON) ?: "Focus Lock Active"
        val sessionId = intent.getLongExtra(EXTRA_SESSION_ID, -1L)
        val sessionName = intent.getStringExtra(EXTRA_SESSION_NAME) ?: "Focus Session"
        val targetEndMs = intent.getLongExtra(EXTRA_TARGET_END_MS, 0L)
        val strictnessString = intent.getStringExtra(EXTRA_STRICTNESS) ?: "STRICT"
        val strictness = try {
            StrictnessLevel.valueOf(strictnessString)
        } catch (_: Exception) {
            StrictnessLevel.STRICT
        }

        setContent {
            FocusLockTheme {
                BlockScreen(
                    restrictedPackage = restrictedPackage,
                    reason = reason,
                    sessionName = sessionName,
                    targetEndMs = targetEndMs,
                    strictness = strictness,
                    onReturnHome = { returnToHome() },
                    onOpenFocusLock = {
                        val mainIntent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(mainIntent)
                        finish()
                    },
                    onEmergencyCancel = {
                        if (sessionId > 0) {
                            val app = application as FocusLockApp
                            CoroutineScope(Dispatchers.IO).launch {
                                app.sessionRepository.endSession(sessionId, SessionStatus.CANCELLED_EMERGENCY)
                                FocusSessionService.stopService(this@BlockOverlayActivity)
                            }
                        }
                        returnToHome()
                    }
                )
            }
        }
    }

    private fun returnToHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
}

