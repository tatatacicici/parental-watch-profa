package com.example.parental_watch.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.parental_watch.data.VideoRepository
import com.example.parental_watch.data.db.WatchHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val history: List<WatchHistory>) : DashboardState()
    object Empty : DashboardState()
}

data class DashboardSummary(
    val watchedToday: Int = 0,
    val blockedToday: Int = 0
)

enum class HistoryFilter { ALL, PLAYED, BLOCKED }

class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = VideoRepository(app)

    private val _state = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val state: StateFlow<DashboardState> = _state

    private val _filter = MutableStateFlow(HistoryFilter.ALL)
    val filter: StateFlow<HistoryFilter> = _filter

    private val _summary = MutableStateFlow(DashboardSummary())
    val summary: StateFlow<DashboardSummary> = _summary

    private var allHistory: List<WatchHistory> = emptyList()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _state.value = DashboardState.Loading
            allHistory = repository.getHistory()
            updateSummary()
            applyFilter()
        }
    }

    private fun updateSummary() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        val todayHistory = allHistory.filter { it.watchedAt >= startOfDay }
        _summary.value = DashboardSummary(
            watchedToday = todayHistory.count { it.action == "PLAYED" },
            blockedToday = todayHistory.count { it.action == "BLOCKED" }
        )
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
