package ca.gainzassist.feature.main.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.gainzassist.domain.model.Workout
import ca.gainzassist.domain.usecase.session.GetIncompleteWorkoutNamesUseCase
import ca.gainzassist.domain.usecase.workout.DeleteAllWorkoutsUseCase
import ca.gainzassist.domain.usecase.workout.DeleteWorkoutUseCase
import ca.gainzassist.domain.usecase.workout.GetWorkoutWithExercisesByNameUseCase
import ca.gainzassist.domain.usecase.workout.ObserveWorkoutsUseCase
import ca.gainzassist.feature.main.presentation.screen.MainTab
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainViewModelState(
    val selectedTab: MainTab = MainTab.WORKOUTS,
    val allWorkoutNames: List<String> = emptyList(),
    val filteredWorkoutNames: List<String> = emptyList(),
    val resumeWorkoutNames: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedWorkoutName: String? = null,
    val isSearchExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface MainViewModelEvent {
    data class StartWorkout(val workout: Workout) : MainViewModelEvent
    data class EditWorkout(val workout: Workout) : MainViewModelEvent
    data object AddWorkout : MainViewModelEvent
    data object LoggedOut : MainViewModelEvent
    data class Error(val message: String) : MainViewModelEvent
}

class MainViewModel(
    private val observeWorkoutsUseCase: ObserveWorkoutsUseCase,
    private val getWorkoutWithExercisesByNameUseCase: GetWorkoutWithExercisesByNameUseCase,
    private val deleteWorkoutUseCase: DeleteWorkoutUseCase,
    private val deleteAllWorkoutsUseCase: DeleteAllWorkoutsUseCase,
    private val getIncompleteWorkoutNamesUseCase: GetIncompleteWorkoutNamesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MainViewModelState())
    val state: StateFlow<MainViewModelState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<MainViewModelEvent>()
    val events: SharedFlow<MainViewModelEvent> = _events.asSharedFlow()

    init {
        observeWorkouts()
    }

    private fun observeWorkouts() {
        observeWorkoutsUseCase().onEach { workouts ->
            val workoutNames = workouts.mapNotNull { it.name }
            refreshResumeWorkoutsInternal(workoutNames)
        }.launchIn(viewModelScope)
    }

    fun refreshResumeWorkouts() {
        viewModelScope.launch {
            refreshResumeWorkoutsInternal(_state.value.allWorkoutNames)
        }
    }

    private suspend fun refreshResumeWorkoutsInternal(allWorkoutNames: List<String>) {
        val incompleteNames = getIncompleteWorkoutNamesUseCase()
        val resumeNames = allWorkoutNames.filter { incompleteNames.contains(it) }

        _state.update { currentState ->
            val filtered = allWorkoutNames.filter {
                it.lowercase().contains(currentState.searchQuery.lowercase())
            }
            currentState.copy(
                allWorkoutNames = allWorkoutNames,
                resumeWorkoutNames = resumeNames,
                filteredWorkoutNames = filtered
            )
        }
    }

    fun onTabSelected(tab: MainTab) {
        _state.update { it.copy(selectedTab = tab, isSearchExpanded = false) }
        if (tab == MainTab.RESUME) {
            refreshResumeWorkouts()
        }
    }

    fun onSearchExpanded() {
        _state.update { it.copy(isSearchExpanded = true) }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { currentState ->
            val filtered = currentState.allWorkoutNames.filter {
                it.lowercase().contains(query.lowercase())
            }
            currentState.copy(searchQuery = query, filteredWorkoutNames = filtered)
        }
    }

    fun onSearchClosed() {
        _state.update {
            it.copy(
                isSearchExpanded = false,
                searchQuery = "",
                filteredWorkoutNames = it.allWorkoutNames
            )
        }
    }

    fun onWorkoutClicked(workoutName: String) {
        viewModelScope.launch {
            val workout = getWorkoutWithExercisesByNameUseCase(workoutName)
            if (workout != null) {
                _events.emit(MainViewModelEvent.StartWorkout(workout))
            } else {
                _events.emit(MainViewModelEvent.Error("Could not load workout $workoutName"))
            }
        }
    }

    fun onResumeWorkoutClicked(workoutName: String) {
        onWorkoutClicked(workoutName)
    }

    fun onWorkoutLongClicked(workoutName: String) {
        _state.update { it.copy(selectedWorkoutName = workoutName) }
    }

    fun onDismissWorkoutDialog() {
        _state.update { it.copy(selectedWorkoutName = null) }
    }

    fun onEditWorkoutClicked(workoutName: String) {
        viewModelScope.launch {
            val workout = getWorkoutWithExercisesByNameUseCase(workoutName)
            if (workout != null) {
                _events.emit(MainViewModelEvent.EditWorkout(workout))
                _state.update { it.copy(selectedWorkoutName = null) }
            } else {
                _events.emit(MainViewModelEvent.Error("Could not load workout $workoutName"))
            }
        }
    }

    fun onDeleteWorkoutClicked(workoutName: String) {
        viewModelScope.launch {
            deleteWorkoutUseCase(workoutName)
            _state.update { it.copy(selectedWorkoutName = null) }
        }
    }

    fun onAddWorkoutClicked() {
        viewModelScope.launch {
            _events.emit(MainViewModelEvent.AddWorkout)
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            deleteAllWorkoutsUseCase()
            _events.emit(MainViewModelEvent.LoggedOut)
        }
    }
}
