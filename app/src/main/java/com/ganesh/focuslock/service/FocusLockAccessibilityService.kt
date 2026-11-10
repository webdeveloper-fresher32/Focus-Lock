package com.ganesh.focuslock.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.ganesh.focuslock.FocusLockApp
import com.ganesh.focuslock.presentation.blocker.BlockOverlayActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FocusLockAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var lastBlockedPackage: String? = null
    private var lastBlockedTimestamp: Long = 0L

    companion object {
        private const val TAG = "FocusAccessibility"
        private const val THROTTLE_MS = 1200L // prevent duplicate bursts
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return

        // Ignore our own app and system UI
        if (packageName == this.packageName || isSystemWhitelisted(packageName)) {
            return
        }

        serviceScope.launch {
            checkAndEnforceRestriction(packageName)
        }
    }

    private suspend fun checkAndEnforceRestriction(targetPackage: String) {
        val app = application as? FocusLockApp ?: return
        val sessionRepo = app.sessionRepository
        val appRepo = app.appRepository
        val usageRepo = app.usageRepository

        // Clean up any expired sessions first to ensure fresh state
        sessionRepo.cleanupExpiredSessions()

        val activeSession = sessionRepo.getActiveSessionSync()
        var shouldBlock = false
        var reason = ""

        if (activeSession != null && !activeSession.isFinished) {
            val restrictedPackages = appRepo.getRestrictedPackageNamesSync()
            if (restrictedPackages.contains(targetPackage)) {
                shouldBlock = true
                reason = "Focus Session '${activeSession.sessionName}' is active"
            }
        }

        if (!shouldBlock) {
            // Check daily limit exceeded only if an explicit limit was set
            if (usageRepo.isAppExceedingDailyLimit(targetPackage)) {
                shouldBlock = true
                reason = "Daily app limit reached"
            }
        }

        if (shouldBlock) {
            val now = System.currentTimeMillis()
            if (targetPackage == lastBlockedPackage && (now - lastBlockedTimestamp) < THROTTLE_MS) {
                return
            }
            lastBlockedPackage = targetPackage
            lastBlockedTimestamp = now

            Log.i(TAG, "Blocking restricted application: $targetPackage. Reason: $reason")

            // Increment distraction stat only if active session exists
            if (activeSession != null && !activeSession.isFinished) {
                sessionRepo.incrementDistractionsBlocked(activeSession.id)
            }

            // Launch the Block Screen Overlay
            val blockIntent = Intent(this, BlockOverlayActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION
                putExtra(BlockOverlayActivity.EXTRA_PACKAGE_NAME, targetPackage)
                putExtra(BlockOverlayActivity.EXTRA_REASON, reason)
                if (activeSession != null && !activeSession.isFinished) {
                    putExtra(BlockOverlayActivity.EXTRA_SESSION_ID, activeSession.id)
                    putExtra(BlockOverlayActivity.EXTRA_SESSION_NAME, activeSession.sessionName)
                    putExtra(BlockOverlayActivity.EXTRA_TARGET_END_MS, activeSession.targetEndTimeEpochMs)
                    putExtra(BlockOverlayActivity.EXTRA_STRICTNESS, activeSession.strictnessLevel.name)
                }
            }
            startActivity(blockIntent)
        }
    }

    private fun isSystemWhitelisted(pkg: String): Boolean {
        return pkg.contains("dialer") ||
                pkg.contains("telecom") ||
                pkg.contains("emergency") ||
                pkg.contains("settings") ||
                pkg.contains("launcher") ||
                pkg == "android" ||
                pkg == "com.android.systemui"
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility Service Interrupted")
    }
}
