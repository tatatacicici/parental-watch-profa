package com.example.parental_watch.data.preference

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import java.security.MessageDigest
import java.util.Calendar

class PreferencesManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val PIN_KEY = "parent_pin"
    private val PIN_DEFAULT = "1234"

    // ── PIN ──────────────────────────────────────────────────

    fun getParentPin(): String {
        return prefs.getString(PIN_KEY, PIN_DEFAULT) ?: PIN_DEFAULT
    }

    fun savePin(pin: String) {
        prefs.edit().putString(PIN_KEY, pin.hashCode().toString()).apply()
    }

    fun validatePin(input: String): Boolean {
        val stored = prefs.getString(PIN_KEY, PIN_DEFAULT.hashCode().toString())
        return input.hashCode().toString() == stored
    }

    fun isPinSet(): Boolean {
        // Check if a PIN has been explicitly set (not using default)
        return prefs.contains(PIN_KEY)
    }

    // ── Challenge / Security Question ──────────────────────────

    fun saveSecurityQuestion(question: String, answer: String) {
        prefs.edit()
            .putString(KEY_SECURITY_QUESTION, question)
            .putString(KEY_SECURITY_ANSWER_HASH, hashPin(answer.lowercase().trim()))
            .apply()
    }

    fun getSecurityQuestion(): String? {
        return prefs.getString(KEY_SECURITY_QUESTION, null)
    }

    fun validateSecurityAnswer(answer: String): Boolean {
        val savedHash = prefs.getString(KEY_SECURITY_ANSWER_HASH, null) ?: return false
        return savedHash == hashPin(answer.lowercase().trim())
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(pin.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    // ── Whitelist Aplikasi ────────────────────────────────────

    fun saveWhitelist(packageNames: Set<String>) {
        prefs.edit().putStringSet(KEY_WHITELIST, packageNames).apply()
    }

    fun getWhitelist(): Set<String> {
        return prefs.getStringSet(KEY_WHITELIST, emptySet()) ?: emptySet()
    }

    fun isAppWhitelisted(packageName: String): Boolean {
        return getWhitelist().contains(packageName)
    }

    // ── Status Service ────────────────────────────────────────

    fun setServiceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SERVICE_ENABLED, enabled).apply()
    }

    fun isServiceEnabled(): Boolean {
        return prefs.getBoolean(KEY_SERVICE_ENABLED, false)
    }

    // ── Google Login Status ───────────────────────────────────
    fun setGoogleLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_GOOGLE_LOGGED_IN, loggedIn).apply()
    }

    fun isGoogleLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_GOOGLE_LOGGED_IN, false)
    }

    // ── Onboarding ────────────────────────────────────────────
    fun isOnboardingComplete(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)
    }

    fun setOnboardingComplete(complete: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, complete).apply()
    }

    // ── Jam Belajar ─────────────────────────────────────────────

    fun setStudyModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_STUDY_MODE_ENABLED, enabled).apply()
    }

    fun isStudyModeEnabled(): Boolean {
        return prefs.getBoolean(KEY_STUDY_MODE_ENABLED, false)
    }

    fun saveStudySchedule(
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ) {
        prefs.edit()
            .putInt(KEY_STUDY_START_HOUR, startHour)
            .putInt(KEY_STUDY_START_MINUTE, startMinute)
            .putInt(KEY_STUDY_END_HOUR, endHour)
            .putInt(KEY_STUDY_END_MINUTE, endMinute)
            .apply()
    }

    fun getStudyStartHour(): Int = prefs.getInt(KEY_STUDY_START_HOUR, 19)
    fun getStudyStartMinute(): Int = prefs.getInt(KEY_STUDY_START_MINUTE, 0)
    fun getStudyEndHour(): Int = prefs.getInt(KEY_STUDY_END_HOUR, 21)
    fun getStudyEndMinute(): Int = prefs.getInt(KEY_STUDY_END_MINUTE, 0)

    fun isTimeTampered(): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.AUTO_TIME,
                0
            ) == 0
        } catch (e: Exception) {
            false // Fallback in case of permission/API issues
        }
    }

    fun isStudyTimeNow(): Boolean {
        if (!isStudyModeEnabled()) return false
        
        // Anti-cheat: Lock if automatic time is disabled to prevent spoofing
        if (isTimeTampered()) return true

        val now = Calendar.getInstance()

        val currentMinutes =
            now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val startMinutes =
            getStudyStartHour() * 60 + getStudyStartMinute()

        val endMinutes =
            getStudyEndHour() * 60 + getStudyEndMinute()

        return if (startMinutes <= endMinutes) {
            currentMinutes in startMinutes until endMinutes
        } else {
            currentMinutes >= startMinutes || currentMinutes < endMinutes
        }
    }

    fun getStudyScheduleText(): String {
        return "%02d:%02d - %02d:%02d".format(
            getStudyStartHour(),
            getStudyStartMinute(),
            getStudyEndHour(),
            getStudyEndMinute()
        )
    }

    companion object {
        private const val PREF_NAME = "parental_watch_prefs"
        private const val KEY_WHITELIST = "whitelist"
        private const val KEY_SERVICE_ENABLED = "service_enabled"
        private const val KEY_SECURITY_QUESTION = "security_question"
        private const val KEY_SECURITY_ANSWER_HASH = "security_answer_hash"

        private const val KEY_GOOGLE_LOGGED_IN = "google_logged_in"
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"

        private const val KEY_STUDY_MODE_ENABLED = "study_mode_enabled"
        private const val KEY_STUDY_START_HOUR = "study_start_hour"
        private const val KEY_STUDY_START_MINUTE = "study_start_minute"
        private const val KEY_STUDY_END_HOUR = "study_end_hour"
        private const val KEY_STUDY_END_MINUTE = "study_end_minute"
    }
}
