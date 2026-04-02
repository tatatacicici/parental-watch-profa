package com.example.parental_watch.utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * DebounceUtils — mencegah model dijalankan terlalu sering.
 *
 * Accessibility Service bisa trigger ratusan event per menit.
 * Debounce memastikan kita hanya proses setelah event "tenang"
 * selama X millisecond — mirip cara kerja search bar autocomplete.
 */
class DebounceUtils {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var debounceJob: Job? = null

    fun debounce(delayMs: Long = 300L, action: () -> Unit){
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(delayMs)
            action()
        }
    }

    fun cancel(){
        debounceJob?.cancel()
    }
}