package com.example.parental_watch.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils

object AccessibilityUtils {

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedService = "${context.packageName}/" +
                "com.example.parental_watch.service.MonitoringService"

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return TextUtils.SimpleStringSplitter(':').apply {
            setString(enabledServices)
        }.any { it.equals(expectedService, ignoreCase = true) }
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}