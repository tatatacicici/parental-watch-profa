package com.example.parental_watch.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.parental_watch.data.LogRepository
import com.example.parental_watch.data.db.LogEntity
import com.example.parental_watch.data.preference.PreferencesManager
import com.example.parental_watch.network.ApiClient
import com.example.parental_watch.network.ClassifyRequest
import com.example.parental_watch.overlay.OverlayManager
import com.example.parental_watch.utils.DebounceUtils
import com.example.parental_watch.utils.DummyClassifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MonitoringService : AccessibilityService() {

    private lateinit var prefManager: PreferencesManager
    private lateinit var overlayManager: OverlayManager
    private lateinit var logRepository: LogRepository
    private val dummyClassifier = DummyClassifier()
    private val debounce = DebounceUtils()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onServiceConnected() {
        prefManager = PreferencesManager(this)
        overlayManager = OverlayManager(this)
        logRepository = LogRepository(this)

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        if (!prefManager.isServiceEnabled()) return

        val packageName = event.packageName?.toString() ?: return
        if (!prefManager.isAppWhitelisted(packageName)) return

        debounce.debounce(300L) {
            processEvent(event, packageName)
        }
    }

    private fun processEvent(event: AccessibilityEvent, packageName: String) {
        val rootNode = rootInActiveWindow ?: return
        val textNodes = mutableListOf<AccessibilityNodeInfo>()
        collectTextNodes(rootNode, textNodes)

        // Ambil nama app untuk log
        val appName = try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(packageName, 0)
            ).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }

        serviceScope.launch {
            // Hapus overlay lama sebelum proses baru
            launch(Dispatchers.Main) {
                overlayManager.removeAllOverlays()
            }

            for (node in textNodes) {
                val text = node.text?.toString() ?: continue
                if (text.isBlank() || text.length < 2) continue

                // Coba kirim ke server dulu, fallback ke dummy
                val result = classifyWithFallback(text)

                if (result.isOffensive) {
                    launch(Dispatchers.Main) {
                        overlayManager.showOverlay(node)
                    }

                    // Simpan ke log database
                    logRepository.insert(
                        LogEntity(
                            text = text,
                            appPackage = packageName,
                            appName = appName,
                            label = result.label,
                            confidence = result.confidence
                        )
                    )
                }
            }
        }
    }

    private suspend fun classifyWithFallback(text: String): DummyClassifier.ClassificationResult {
        return try {
            // Coba server dulu
            val response = ApiClient.apiService.classify(ClassifyRequest(text))
            DummyClassifier.ClassificationResult(
                text = text,
                isOffensive = response.is_offensive,
                confidence = response.confidence,
                label = response.label
            )
        } catch (e: Exception) {
            // Server tidak bisa diakses → fallback ke dummy
            dummyClassifier.classify(text)
        }
    }

    private fun collectTextNodes(
        node: AccessibilityNodeInfo?,
        result: MutableList<AccessibilityNodeInfo>
    ) {
        node ?: return

        if (!node.text.isNullOrBlank()) {
            val isLeafTextNode = node.childCount == 0 ||
                    (0 until node.childCount).none { i ->
                        node.getChild(i)?.text.isNullOrBlank().not()
                    }
            if (isLeafTextNode) result.add(node)
        }

        for (i in 0 until node.childCount) {
            collectTextNodes(node.getChild(i), result)
        }
    }

    override fun onInterrupt() {
        overlayManager.removeAllOverlays()
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayManager.removeAllOverlays()
        serviceScope.cancel()
    }
}