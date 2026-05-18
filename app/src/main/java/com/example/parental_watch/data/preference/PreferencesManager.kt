package com.example.parental_watch.data.preference

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // ── PIN ──────────────────────────────────────────────────

    fun isPinSet(): Boolean {
        return prefs.getString(KEY_PIN_HASH, null) != null
    }

    fun savePin(pin: String) {
        prefs.edit().putString(KEY_PIN_HASH, hashPin(pin)).apply()
    }

    fun validatePin(pin: String): Boolean {
        val savedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return savedHash == hashPin(pin)
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(pin.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
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

    companion object {
        private const val PREF_NAME = "parental_watch_prefs"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_WHITELIST = "whitelist"
        private const val KEY_SERVICE_ENABLED = "service_enabled"
        private const val KEY_SECURITY_QUESTION = "security_question"
        private const val KEY_SECURITY_ANSWER_HASH = "security_answer_hash"
    }
}