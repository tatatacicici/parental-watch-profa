package com.example.parental_watch.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.parental_watch.data.LogRepository
import com.example.parental_watch.data.db.LogEntity
import com.example.parental_watch.data.preference.PreferencesManager
import com.example.parental_watch.network.ApiClient
import com.example.parental_watch.network.ClassifyRequest
import com.example.parental_watch.overlay.OverlayManager
import com.example.parental_watch.utils.DummyClassifier
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.LinkedHashMap

class MonitoringService : AccessibilityService() {

    private lateinit var prefManager: PreferencesManager
    private lateinit var overlayManager: OverlayManager
    private lateinit var logRepository: LogRepository
    private val dummyClassifier = DummyClassifier()
    
    // Pisah scope — debounce di main, klasifikasi di IO
    private val debounceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val classifyScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var debounceJob: Job? = null
    private var lastTextHash: Int = 0
    private val currentGeneration = AtomicInteger(0)
    
    // Perubahan 2: Tambah cache di class level
    private val classificationCache = LinkedHashMap<String, DummyClassifier.ClassificationResult>(100, 0.75f, true)
    private val CACHE_MAX_SIZE = 100
    private val TAG = "ParentalWatch_Service"

    override fun onServiceConnected() {
        prefManager = PreferencesManager(this)
        overlayManager = OverlayManager(this)
        logRepository = LogRepository(this)

        serviceInfo = AccessibilityServiceInfo().apply {
            // Mode Layar Saja: Fokus pada perubahan jendela agar tidak berat saat mengetik
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOWS_CHANGED
            
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            
            notificationTimeout = 1000 
        }
        Log.d(TAG, "Service Connected - Mode Debounce Terpisah")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d(TAG, "Event masuk: ${event?.eventType} pkg=${event?.packageName}")
        val packageName = event?.packageName?.toString() ?: return
        
        // Abaikan keyboard
        if (packageName.contains("inputmethod") || packageName.contains("keyboard")) {
            return
        }

        if (!prefManager.isServiceEnabled() || !prefManager.isAppWhitelisted(packageName)) return

        // Perubahan 1: Remove overlay SEGERA saat event masuk — sebelum debounce
        overlayManager.removeAllOverlays()

        // Debounce HANYA untuk pengambilan teks di Main Thread
        debounceJob?.cancel()
        debounceJob = debounceScope.launch {
            delay(600L) // Tunggu layar stabil
            
            val textDataList = extractTextFromScreen() ?: return@launch

            // Cek hash untuk menghindari pemrosesan ulang konten yang sama
            val combinedText = textDataList.joinToString("|") { it.text }
            val hash = combinedText.hashCode()
            if (hash == lastTextHash) return@launch
            lastTextHash = hash

            if (textDataList.isEmpty()) {
                overlayManager.removeAllOverlays()
                return@launch
            }

            // Fix B: Implementasi Generational ID dan Pembersihan Overlay Segera
            val gen = currentGeneration.incrementAndGet()
            
            // Bersihkan overlay lama SEGERA — tidak perlu tunggu job selesai
            overlayManager.removeAllOverlays()

            // Lempar ke classifyScope (IO) dengan Generational ID
            classifyScope.launch {
                runClassification(textDataList, packageName, gen)
            }
        }
    }

    private fun extractTextFromScreen(): List<TextData>? {
        val rootNode = rootInActiveWindow ?: return null
        
        // Deteksi screen state dulu sebelum ekstraksi penuh
        val screenState = detectWhatsAppScreen(rootNode)
        Log.d(TAG, "Screen state: $screenState")
        
        if (screenState == WAScreen.CHAT_LIST || screenState == WAScreen.UNKNOWN) {
            Log.d(TAG, "Bukan conversation screen, skip scan")
            return emptyList()
        }
        
        val result = mutableListOf<TextData>()
        collectTextNodes(rootNode, result)
        
        // Dedup: teks sama hanya diproses sekali, ambil bounds pertama
        return result.distinctBy { it.text.trim().lowercase() }
    }

    enum class WAScreen { CONVERSATION, CHAT_LIST, UNKNOWN }

    private fun detectWhatsAppScreen(root: AccessibilityNodeInfo): WAScreen {
        // Indikator chat list: tab navbar WA
        val chatListIndicators = listOf(
            "Komunitas", "Pembaruan", "Panggilan",
            "Community", "Updates", "Calls"
        )
        
        // Cek flat — tidak perlu traversal penuh, cukup cek beberapa node
        val flatNodes = mutableListOf<String>()
        collectFlatText(root, flatNodes, maxNodes = 30)
        
        val isChatList = chatListIndicators.any { indicator ->
            flatNodes.any { it.equals(indicator, ignoreCase = true) }
        }
        
        if (isChatList) return WAScreen.CHAT_LIST
        
        // Indikator conversation: ada input bar di bawah
        val screenHeight = resources.displayMetrics.heightPixels
        val hasInputBar = hasNodeInZone(root, topThreshold = screenHeight * 0.85f)
        
        return if (hasInputBar) WAScreen.CONVERSATION else WAScreen.UNKNOWN
    }

    private fun collectFlatText(
        node: AccessibilityNodeInfo?,
        result: MutableList<String>,
        maxNodes: Int
    ) {
        if (node == null || result.size >= maxNodes) return
        val text = node.text?.toString() ?: node.contentDescription?.toString()
        if (!text.isNullOrBlank()) result.add(text)
        for (i in 0 until node.childCount) {
            collectFlatText(node.getChild(i), result, maxNodes)
        }
    }

    private fun hasNodeInZone(
        node: AccessibilityNodeInfo?,
        topThreshold: Float
    ): Boolean {
        node ?: return false
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        if (bounds.top > topThreshold && bounds.width() > 100) return true
        for (i in 0 until node.childCount) {
            if (hasNodeInZone(node.getChild(i), topThreshold)) return true
        }
        return false
    }

    private fun collectTextNodes(node: AccessibilityNodeInfo?, result: MutableList<TextData>) {
        node ?: return

        val className = node.className?.toString() ?: ""
        val text = node.text?.toString() ?: node.contentDescription?.toString()

        // === LAYER 1: Filter berdasarkan className ===
        val isEditText = className.contains("EditText", ignoreCase = true)
        val isButton = className.contains("Button", ignoreCase = true)
        val isImageView = className.contains("ImageView", ignoreCase = true)

        if (!isEditText && !isButton && !isImageView) {
            if (!text.isNullOrBlank()) {

                // === LAYER 2: Filter berdasarkan konten teks ===
                val isTimestamp = text.matches(Regex("""\d{1,2}[.:]\d{2}"""))
                val isFileSize = text.matches(Regex("""^\d+[,.]?\d*\s?(MB|KB|GB)$""", RegexOption.IGNORE_CASE))
                val isFileExtension = text.matches(Regex("""^(MP4|PDF|JPG|PNG|AAC|OGG|OPUS|DOC|DOCX|XLS|XLSX)$""", RegexOption.IGNORE_CASE))
                val isDateSeparator = text.matches(Regex("""^\d{1,2}\s\w+\s\d{4}$""")) // "9 April 2026"
                val isDayLabel = text.matches(Regex("""^(Hari ini|Kemarin|Yesterday|Today)$""", RegexOption.IGNORE_CASE))
                val isFilename = text.matches(Regex(""".*\.(mp4|pdf|jpg|png|doc|docx|xls)$""", RegexOption.IGNORE_CASE))
                val isDeliveryStatus = text.matches(Regex("""^(Tersampaikan|Dibaca|Sent|Read|Delivered)$""", RegexOption.IGNORE_CASE))
                val isTooShort = text.trim().length < 3

                // === Fix A: LAYER 2 TAMBAHAN: Filter UI chrome WA spesifik ===
                val isURL = text.startsWith("http://") || text.startsWith("https://")
                val isPreviewSender = text.endsWith(": ") || text.matches(Regex("""^\w[\w\s]+:\s*$"""))
                val isBadge = text.contains("pesan belum dibaca") || text.contains("unread message")
                val isAccessibilityHint = text.contains("Usap ke", ignoreCase = true) ||
                        text.contains("untuk menampilkan", ignoreCase = true) ||
                        text.contains("Ketuk dua kali", ignoreCase = true) ||
                        text.contains("Radio Group", ignoreCase = true)
                val isSearchBar = text.contains("Tanya Meta AI", ignoreCase = true) ||
                        (text.contains("Cari", ignoreCase = true) && text.length < 10)
                val isDateFormat = text.matches(Regex("""\d{1,2}/\d{2}/\d{2,4}""")) // "16/05/26"
                val isDiteruskan = text.trim().equals("Diteruskan", ignoreCase = true)
                val isCallLog = text.contains("Telepon suara") || text.contains("Telepon video") ||
                        text.matches(Regex("""\d+\s?dtk""")) // "20 dtk"

                val shouldSkip = isTimestamp || isFileSize || isFileExtension ||
                        isDateSeparator || iisDayLabel || isFilename ||
                        isDeliveryStatus || isTooShort || isURL ||
                        isPreviewSender || isBadge || isAccessibilityHint ||
                        isSearchBar || isDateFormat || isDiteruskan || isCallLog

                if (!shouldSkip) {
                    val bounds = Rect()
                    node.getBoundsInScreen(bounds)
                    
                    // Normalisasi koordinat X — handle split-screen / floating window
                    val screenWidth = resources.displayMetrics.widthPixels
                    if (bounds.left >= screenWidth) {
                        val offset = (bounds.left / screenWidth) * screenWidth
                        bounds.left -= offset
                        bounds.right -= offset
                    }

                    // Zone filter: hanya ambil node di zona bubble
                    val screenHeight = resources.displayMetrics.heightPixels
                    val headerZoneBottom = screenHeight * 0.10f   // ~240px
                    val inputBarZoneTop = screenHeight * 0.88f    // ~2112px
                    val isInBubbleZone = bounds.top > headerZoneBottom &&
                                         bounds.top < inputBarZoneTop
                    if (!isInBubbleZone) {
                        Log.d(TAG, "OUT OF ZONE: '$text' bounds=$bounds")
                    } else if (bounds.width() > 0 && bounds.height() > 0) {
                        Log.d(TAG, "LOLOS: '$text' bounds=$bounds")
                        result.add(TextData(text, Rect(bounds)))
                    }
                } else {
                    Log.d(TAG, "FILTERED: '$text'")
                }
            }
        }

        for (i in 0 until node.childCount) {
            collectTextNodes(node.getChild(i), result)
        }
    }

    // Perubahan 3: Ganti seluruh runClassification
    private suspend fun runClassification(
        textDataList: List<TextData>,
        packageName: String,
        gen: Int
    ) {
        try {
            // Step 1: Pisah cache hit vs cache miss
            val cacheHits = mutableListOf<ClassificationMatch>()
            val cacheMisses = mutableListOf<TextData>()

            textDataList.forEach { data ->
                val key = data.text.trim().lowercase()
                val cached = synchronized(classificationCache) { classificationCache[key] }
                if (cached != null) {
                    Log.d(TAG, "Cache hit: '${data.text}' → ${cached.label}")
                    if (cached.isOffensive) cacheHits.add(ClassificationMatch(data, cached))
                } else {
                    cacheMisses.add(data)
                }
            }

            // Step 2: Batas 5 node untuk cache miss
            val toClassify = cacheMisses.take(5)
            if (cacheMisses.size > 5) {
                Log.d(TAG, "Node limit: ${cacheMisses.size} miss, ambil 5 saja")
            }

            // Step 3: Classify hanya yang miss, paralel
            val newResults = toClassify.map { data ->
                classifyScope.async {
                    val result = classifyWithFallback(data.text)

                    // Simpan ke cache — thread safe karena akses dari classifyScope
                    val key = data.text.trim().lowercase()
                    synchronized(classificationCache) {
                        classificationCache[key] = result
                        if (classificationCache.size > CACHE_MAX_SIZE) {
                            classificationCache.remove(classificationCache.keys.first())
                        }
                    }

                    if (result.isOffensive) ClassificationMatch(data, result) else null
                }
            }.awaitAll().filterNotNull()

            // Step 4: Gabung cache hit + hasil baru
            val offensiveResults = cacheHits + newResults

            withContext(Dispatchers.Main) {
                // GUARD: Jika generasi tidak cocok, abaikan hasilnya (stale)
                if (gen != currentGeneration.get()) {
                    Log.d(TAG, "Stale result gen=$gen, current=${currentGeneration.get()}, discarded")
                    return@withContext
                }

                // Bersihkan yang lama sebelum menampilkan yang baru
                overlayManager.removeAllOverlays()

                if (offensiveResults.isNotEmpty()) {
                    offensiveResults.forEach { match ->
                        Log.w(TAG, "Offensive found: ${match.data.text}")
                        overlayManager.showOverlay(match.data.bounds)
                    }
                }
            }

            // Logging jika ada temuan (tetap di-log meskipun UI mungkin sudah berubah)
            if (offensiveResults.isNotEmpty()) {
                val appName = try {
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) { packageName }

                offensiveResults.forEach { match ->
                    logRepository.insert(
                        LogEntity(
                            text = match.data.text,
                            appPackage = packageName,
                            appName = appName,
                            label = match.result.label,
                            confidence = match.result.confidence
                        )
                    )
                }
            }

        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Classification error: ${e.message}")
        }
    }

    private suspend fun classifyWithFallback(text: String): DummyClassifier.ClassificationResult {
        return try {
            withTimeout(5000) {
                val response = ApiClient.apiService.classify(ClassifyRequest(text))
                Log.d(TAG, "Raw response: label=${response.label} confidence=${response.confidence} isOffensive=${response.isOffensive}")
                DummyClassifier.ClassificationResult(text, response.isOffensive, response.confidence, response.label)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Network classification failed, falling back to dummy: ${e.message}")
            dummyClassifier.classify(text)
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        overlayManager.removeAllOverlays()
        debounceScope.cancel()
        classifyScope.cancel()
    }

    data class TextData(val text: String, val bounds: Rect)
    data class ClassificationMatch(val data: TextData, val result: DummyClassifier.ClassificationResult)
}
