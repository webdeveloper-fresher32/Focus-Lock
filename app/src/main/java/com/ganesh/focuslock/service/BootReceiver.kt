package com.ganesh.focuslock.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ganesh.focuslock.FocusLockApp
import com.ganesh.focuslock.domain.model.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d("BootReceiver", "Device reboot detected. Checking for active FocusLock sessions...")
            val app = context.applicationContext as? FocusLockApp ?: return

            CoroutineScope(Dispatchers.IO).launch {
                val activeSession = app.sessionRepository.getActiveSessionSync()
                if (activeSession != null && activeSession.sessionStatus == SessionStatus.RUNNING) {
                    if (activeSession.remainingTimeMs > 0L) {
                        Log.i("BootReceiver", "Resuming active focus session after reboot: ${activeSession.sessionName}")
                        FocusSessionService.startService(context)
                    } else {
                        app.sessionRepository.endSession(activeSession.id, SessionStatus.COMPLETED)
                    }
                }
            }
        }
    }
}

