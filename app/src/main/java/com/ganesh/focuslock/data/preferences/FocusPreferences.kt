package com.ganesh.focuslock.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "focus_lock_prefs")

class FocusPreferences(private val context: Context) {

    companion object {
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_DEFAULT_STRICTNESS = stringPreferencesKey("default_strictness")
        private val KEY_DEFAULT_DURATION = intPreferencesKey("default_duration_minutes")
        private val KEY_EMERGENCY_DELAY = intPreferencesKey("emergency_delay_seconds")
        private val KEY_BLOCKING_ENABLED = booleanPreferencesKey("blocking_enabled")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val defaultStrictness: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_STRICTNESS] ?: "STRICT"
    }

    val defaultDurationMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_DURATION] ?: 45
    }

    val emergencyDelaySeconds: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_EMERGENCY_DELAY] ?: 60
    }

    val isBlockingEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_BLOCKING_ENABLED] ?: true
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setDefaultStrictness(strictness: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_STRICTNESS] = strictness
        }
    }

    suspend fun setDefaultDurationMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_DURATION] = minutes
        }
    }

    suspend fun setBlockingEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BLOCKING_ENABLED] = enabled
        }
    }
}

