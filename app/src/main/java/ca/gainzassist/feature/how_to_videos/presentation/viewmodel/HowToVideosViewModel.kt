package ca.gainzassist.feature.how_to_videos.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.gainzassist.R
import ca.gainzassist.core.util.UiText
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
    val message: UiText? = null,
    val isLoading: Boolean = false
)

sealed interface HowToVideosViewModelEvent {
    data class ShowMessage(val message: UiText) : HowToVideosViewModelEvent
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
                _events.emit(
                    HowToVideosViewModelEvent.ShowMessage(
                        UiText.StringResource(R.string.msg_search_exercise_video)
                    )
                )
            }
        }
    }

    fun executeSearch(query: String) {
        val queryKey = query.trim()
        if ((queryKey.isBlank()) || (queryKey == activeQuery)) {
            return
        }

        if (checkCache(queryKey)) {
            return
        }

        performSearch(queryKey)
    }

    private fun checkCache(queryKey: String): Boolean {
        if (queryCache.containsKey(queryKey)) {
            val cached = queryCache[queryKey] ?: emptyList()
            _state.update {
                it.copy(
                    videos = cached,
                    message = if (cached.isEmpty()) {
                        UiText.StringResource(R.string.msg_no_video_results)
                    } else null
                )
            }
            return true
        }
        return false
    }

    private fun performSearch(queryKey: String) {
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
                        message = if (uiItems.isEmpty()) {
                            UiText.StringResource(R.string.msg_no_video_results)
                        } else null
                    )
                }
            } catch (e: Exception) {
                handleSearchError(e)
            } finally {
                if (activeQuery == queryKey) {
                    activeQuery = null
                }
            }
        }
    }

    private suspend fun handleSearchError(e: Exception) {
        val errorMsg = e.message.orEmpty()
        val uiText = if (e.message != null) UiText.DynamicString(errorMsg)
        else UiText.StringResource(R.string.err_youtube_load_failed)
        _state.update {
            it.copy(
                isLoading = false,
                message = uiText
            )
        }
        _events.emit(HowToVideosViewModelEvent.ShowMessage(uiText))
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
