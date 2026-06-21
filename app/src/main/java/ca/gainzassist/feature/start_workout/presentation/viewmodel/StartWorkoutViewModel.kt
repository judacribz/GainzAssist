package ca.gainzassist.feature.start_workout.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.gainzassist.domain.model.Exercise
import ca.gainzassist.domain.model.Workout
import ca.gainzassist.feature.start_workout.domain.model.StartWorkoutRestoreDecision
import ca.gainzassist.feature.start_workout.domain.usecase.SaveIncompleteWorkoutUseCase
import ca.gainzassist.feature.start_workout.domain.usecase.StartWorkoutSessionUseCase
import ca.gainzassist.feature.start_workout.presentation.screen.StartWorkoutTab
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
    val availableTabs: List<StartWorkoutTab> = listOf(
        StartWorkoutTab.WORKOUT,
        StartWorkoutTab.EXERCISES
    ),
    val exercises: List<Exercise> = emptyList(),
    val warmups: List<Exercise> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSessionReady: Boolean = false
)

sealed interface StartWorkoutViewModelEvent {
    data object OpenHowToVideos : StartWorkoutViewModelEvent
    data object FinishWorkout : StartWorkoutViewModelEvent
    data object ExitWorkout : StartWorkoutViewModelEvent
    data class Error(val message: String) : StartWorkoutViewModelEvent
}

class StartWorkoutViewModel(
    private val startWorkoutSessionUseCase: StartWorkoutSessionUseCase,
    private val saveIncompleteWorkoutUseCase: SaveIncompleteWorkoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(StartWorkoutViewModelState())
    val state: StateFlow<StartWorkoutViewModelState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<StartWorkoutViewModelEvent>()
    val events: SharedFlow<StartWorkoutViewModelEvent> = _events.asSharedFlow()

    fun initializeFromWorkout(workout: Workout) {
        _state.update {
            it.copy(
                workoutName = workout.name ?: "",
                selectedTab = StartWorkoutTab.WORKOUT,
                availableTabs = listOf(StartWorkoutTab.WORKOUT, StartWorkoutTab.EXERCISES),
                exercises = workout.exercises
            )
        }
    }

    suspend fun prepareSessionRestore(workoutName: String): StartWorkoutRestoreDecision = startWorkoutSessionUseCase(
        workoutName
    )

    fun saveLeavingSession(workoutName: String, sessionJson: String) {
        viewModelScope.launch {
            saveIncompleteWorkoutUseCase(workoutName, sessionJson)
        }
    }

    fun onWarmupsGenerated(warmups: List<Exercise>) {
        _state.update { currentState ->
            if (warmups.isEmpty()) {
                currentState.copy(
                    availableTabs = listOf(StartWorkoutTab.WORKOUT, StartWorkoutTab.EXERCISES),
                    selectedTab = StartWorkoutTab.WORKOUT,
                    warmups = warmups
                )
            } else {
                currentState.copy(
                    availableTabs = listOf(StartWorkoutTab.WARMUPS, StartWorkoutTab.WORKOUT, StartWorkoutTab.EXERCISES),
                    selectedTab = StartWorkoutTab.WORKOUT,
                    warmups = warmups
                )
            }
        }
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

    fun onSessionReady() {
        _state.update { it.copy(isSessionReady = true) }
    }
}
