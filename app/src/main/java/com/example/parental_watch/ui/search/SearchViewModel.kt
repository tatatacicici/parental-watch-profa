package com.example.parental_watch.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.parental_watch.data.VideoItem
import com.example.parental_watch.data.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Success(val videos: List<VideoItem>) : SearchState()
    data class Error(val message: String) : SearchState()
    object Empty : SearchState()
}

class SearchViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = VideoRepository(app)

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState

    fun search(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _searchState.value = SearchState.Loading

            val results = repository.searchVideos(query)

            _searchState.value = when {
                results.isEmpty() -> SearchState.Empty
                else -> SearchState.Success(results)
            }
        }
    }

    fun reset() {
        _searchState.value = SearchState.Idle
    }
}
