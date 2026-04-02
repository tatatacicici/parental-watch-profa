package com.example.parental_watch.service
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import com.example.parental_watch.overlay.OverlayManager
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import com.example.parental_watch.data.preference.PreferencesManager
import com.example.parental_watch.utils.DummyClassifier
import com.example.parental_watch.utils.DebounceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MonitoringService: AccessibilityService() {
    private lateinit var prefManager: PreferencesManager
    private lateinit var  overlayManager: OverlayManager
    private lateinit var classifier: DummyClassifier
    private val debounce = DebounceUtils()

    //coroutine scope for background preprocessing
    private val  serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onServiceConnected() {
        prefManager = PreferencesManager(this)
        overlayManager = OverlayManager(this)
        classifier = DummyClassifier()

        //Service Config
        val info  = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                         AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                         AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED

        //get window content
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS

            notificationTimeout = 100
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
       event ?: return

        // check whether service is activated by parents
        if (!prefManager.isServiceEnabled()) return

        val packageName = event.packageName?.toString() ?: return

        // Filter: process app at whitelist only
        if (!prefManager.isAppWhitelisted(packageName)) return

//        Debounce: wait 300ms after last event to process
        debounce.debounce(300L){
            processEvent(event)
        }
    }

    private fun processEvent(event: AccessibilityEvent){
        val rootNode = rootInActiveWindow ?: return

        val textNodes = mutableListOf<AccessibilityNodeInfo>()
        collectTextNodes(rootNode, textNodes)

        serviceScope.launch {
            for (node in textNodes){
                val text = node.text?.toString() ?: continue
                if(text.isBlank() || text.length< 2) continue

                val result = classifier.classify(text)

                if(result.isOffensive){
                    launch(Dispatchers.Main){
                        overlayManager.showOverlay(node)
                    }
                }
            }
        }
    }

//    recursive for collect all node had text
    private fun collectTextNodes(
        node: AccessibilityNodeInfo?,
        result: MutableList<AccessibilityNodeInfo>
        ){

        node ?: return

        if(!node.text.isNullOrBlank()) {
            result.add(node)
        }

        for (i in 0 until node.childCount){
            collectTextNodes(node.getChild(i), result)
        }

    }

    override fun onInterrupt() {
        overlayManager.removeAllOverlays()
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayManager.removeAllOverlays()
        debounce.cancel()
        serviceScope.cancel()
    }
}
