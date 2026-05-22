package com.example.parental_watch.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.parental_watch.data.VideoRepository
import com.example.parental_watch.data.db.WatchHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val history: List<WatchHistory>) : DashboardState()
    object Empty : DashboardState()
}

enum class HistoryFilter { ALL, PLAYED, BLOCKED }

class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = VideoRepository(app)

    private val _state = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val state: StateFlow<DashboardState> = _state

    private val _filter = MutableStateFlow(HistoryFilter.ALL)
    val filter: StateFlow<HistoryFilter> = _filter

    private var allHistory: List<WatchHistory> = emptyList()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _state.value = DashboardState.Loading
            allHistory = repository.getHistory()
            applyFilter()
        }
    }

    fun setFilter(filter: HistoryFilter) {
        _filter.value = filter
        applyFilter()
    }

    private fun applyFilter() {
        val filtered = when (_filter.value) {
            HistoryFilter.ALL -> allHistory
            HistoryFilter.PLAYED -> allHistory.filter { it.action == "PLAYED" }
            HistoryFilter.BLOCKED -> allHistory.filter { it.action == "BLOCKED" }
        }
        _state.value = if (filtered.isEmpty()) DashboardState.Empty
                       else DashboardState.Success(filtered)
    }
}
