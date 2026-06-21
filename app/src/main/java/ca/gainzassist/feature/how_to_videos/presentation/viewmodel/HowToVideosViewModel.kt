package ca.gainzassist.feature.how_to_videos.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.gainzassist.feature.how_to_videos.domain.usecase.SearchHowToVideosUseCase
import ca.gainzassist.feature.how_to_videos.presentation.screen.HowToVideoUiItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HowToVideosViewModelState(
    val exerciseName: String = "",
    val videos: List<HowToVideoUiItem> = emptyList(),
    val isPlayerVisible: Boolean = false,
    val isSearchExpanded: Boolean = false,
    val searchQuery: String = "",
    val selectedVideoId: String? = null,
    val message: String? = null,
    val isLoading: Boolean = false
)

sealed interface HowToVideosViewModelEvent {
    data class ShowMessage(val message: String) : HowToVideosViewModelEvent
}

class HowToVideosViewModel(
    private val searchHowToVideosUseCase: SearchHowToVideosUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HowToVideosViewModelState())
    val state: StateFlow<HowToVideosViewModelState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<HowToVideosViewModelEvent>()
    val events: SharedFlow<HowToVideosViewModelEvent> = _events.asSharedFlow()

    private val queryCache = mutableMapOf<String, List<HowToVideoUiItem>>()
    private var activeQuery: String? = null

    fun initialize(exerciseName: String) {
        _state.update {
            it.copy(
                exerciseName = exerciseName,
                searchQuery = ""
            )
        }
        if (exerciseName.isNotBlank()) {
            executeSearch("how to $exerciseName")
        } else {
            viewModelScope.launch {
                _events.emit(HowToVideosViewModelEvent.ShowMessage("Search for an exercise video."))
            }
        }
    }

    fun executeSearch(query: String) {
        val queryKey = query.trim()
        if (queryKey.isBlank() || queryKey == activeQuery) {
            return
        }

        if (queryCache.containsKey(queryKey)) {
            val cached = queryCache[queryKey] ?: emptyList()
            _state.update {
                it.copy(
                    videos = cached,
                    message = if (cached.isEmpty()) "No video results" else null
                )
            }
            return
        }

        activeQuery = queryKey
        _state.update { it.copy(isLoading = true, message = null) }

        viewModelScope.launch {
            try {
                val results = searchHowToVideosUseCase(queryKey)
                val uiItems = results.map { HowToVideoUiItem(it.videoId, it.title) }
                queryCache[queryKey] = uiItems
                
                _state.update {
                    it.copy(
                        videos = uiItems,
                        isLoading = false,
                        message = if (uiItems.isEmpty()) "No video results" else null
                    )
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unable to load YouTube videos. Check API key or network."
                _state.update {
                    it.copy(
                        isLoading = false,
                        message = errorMsg
                    )
                }
                _events.emit(HowToVideosViewModelEvent.ShowMessage(errorMsg))
            } finally {
                if (activeQuery == queryKey) {
                    activeQuery = null
                }
            }
        }
    }

    fun onVideoClick(videoId: String) {
        _state.update {
            it.copy(
                selectedVideoId = videoId,
                isPlayerVisible = true
            )
        }
    }

    fun closePlayer() {
        _state.update {
            it.copy(
                isPlayerVisible = false
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun onSearchExpandedChanged(expanded: Boolean) {
        _state.update { it.copy(isSearchExpanded = expanded) }
    }
}
