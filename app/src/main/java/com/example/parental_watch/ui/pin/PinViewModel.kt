package com.example.parental_watch.ui.pin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.parental_watch.data.preference.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class PinState {
    object Idle : PinState()
    object Success : PinState()
    data class Error(val attemptsLeft: Int) : PinState()
    object Locked : PinState()
}

class PinViewModel(app: Application) : AndroidViewModel(app) {

    private val prefManager = PreferencesManager(app)

    private val _state = MutableStateFlow<PinState>(PinState.Idle)
    val state: StateFlow<PinState> = _state

    private var attempts = 0
    private val MAX_ATTEMPTS = 3

    fun validatePin(input: String) {
        if (attempts >= MAX_ATTEMPTS) {
            _state.value = PinState.Locked
            return
        }

        if (prefManager.validatePin(input)) {
            attempts = 0
            _state.value = PinState.Success
        } else {
            attempts++
            val left = MAX_ATTEMPTS - attempts
            _state.value = if (left <= 0) PinState.Locked
                           else PinState.Error(left)
        }
    }

    fun resetLock() {
        attempts = 0
        _state.value = PinState.Idle
    }
}
