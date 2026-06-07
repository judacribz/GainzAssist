package ca.gainzassist.presentation.start_workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.gainzassist.activities.start_workout.StartWorkoutTab
import ca.gainzassist.models.Exercise
import ca.gainzassist.domain.usecase.session.RemoveIncompleteSessionUseCase
import ca.gainzassist.domain.usecase.session.RemoveIncompleteWorkoutUseCase
import ca.gainzassist.domain.usecase.session.RemoveSessionProgressUseCase
import ca.gainzassist.domain.usecase.session.SaveIncompleteSessionUseCase
import ca.gainzassist.domain.usecase.session.SaveSessionProgressUseCase
import ca.gainzassist.domain.usecase.workout.GetWorkoutWithExercisesByNameUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StartWorkoutViewModelState(
    val workoutName: String = "",
    val selectedTab: StartWorkoutTab = StartWorkoutTab.WORKOUT,
    val availableTabs: List<StartWorkoutTab> = emptyList(),
    val exercises: List<Exercise> = emptyList(),
    val warmups: List<Exercise> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface StartWorkoutViewModelEvent {
    data object OpenHowToVideos : StartWorkoutViewModelEvent
    data object FinishWorkout : StartWorkoutViewModelEvent
    data object ExitWorkout : StartWorkoutViewModelEvent
    data class Error(val message: String) : StartWorkoutViewModelEvent
}

class StartWorkoutViewModel(
    private val getWorkoutWithExercisesByNameUseCase: GetWorkoutWithExercisesByNameUseCase,
    private val saveIncompleteSessionUseCase: SaveIncompleteSessionUseCase,
    private val saveSessionProgressUseCase: SaveSessionProgressUseCase,
    private val removeIncompleteWorkoutUseCase: RemoveIncompleteWorkoutUseCase,
    private val removeIncompleteSessionUseCase: RemoveIncompleteSessionUseCase,
    private val removeSessionProgressUseCase: RemoveSessionProgressUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(StartWorkoutViewModelState())
    val state: StateFlow<StartWorkoutViewModelState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<StartWorkoutViewModelEvent>()
    val events: SharedFlow<StartWorkoutViewModelEvent> = _events.asSharedFlow()

    fun initialize(workoutName: String) {
        _state.update { it.copy(workoutName = workoutName) }
        // Scaffold: Logic to fetch workout and exercises using getWorkoutWithExercisesByNameUseCase
    }

    fun onTabSelected(tab: StartWorkoutTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    fun onHowToVideosClicked() {
        viewModelScope.launch {
            _events.emit(StartWorkoutViewModelEvent.OpenHowToVideos)
        }
    }

    fun onBackClicked() {
        viewModelScope.launch {
            _events.emit(StartWorkoutViewModelEvent.ExitWorkout)
        }
    }

    fun onFinishWorkoutClicked() {
        viewModelScope.launch {
            _events.emit(StartWorkoutViewModelEvent.FinishWorkout)
        }
    }
}
