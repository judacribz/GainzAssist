package ca.gainzassist.feature.summary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.gainzassist.domain.model.Exercise
import ca.gainzassist.domain.model.Workout
import ca.gainzassist.feature.summary.domain.usecase.SaveWorkoutUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SummaryViewModelState(
    val workoutName: String = "",
    val exercises: List<Exercise> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

sealed interface SummaryViewModelEvent {
    data object Saved : SummaryViewModelEvent
    data class Error(val message: String) : SummaryViewModelEvent
}

class SummaryViewModel(
    private val saveWorkoutUseCase: SaveWorkoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SummaryViewModelState())
    val state: StateFlow<SummaryViewModelState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SummaryViewModelEvent>()
    val events: SharedFlow<SummaryViewModelEvent> = _events.asSharedFlow()

    private var workout: Workout? = null

    fun initialize(initialWorkout: Workout?, workoutName: String, exercises: List<Exercise>) {
        this.workout = initialWorkout ?: Workout().apply {
            this.name = workoutName
            this.exercises = ArrayList(exercises)
        }

        _state.update {
            it.copy(
                workoutName = workoutName,
                exercises = exercises
            )
        }
    }

    fun onSaveClicked(isUpdate: Boolean) {
        val currentState = _state.value

        if (currentState.workoutName.isBlank()) {
            _state.update { it.copy(errorMessage = "Workout name is required.") }
            return
        }

        if (currentState.exercises.isEmpty()) {
            _state.update { it.copy(errorMessage = "No exercises added.") }
            return
        }

        val currentWorkout = workout ?: Workout()
        currentWorkout.name = currentState.workoutName
        currentWorkout.exercises = ArrayList(currentState.exercises)

        _state.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                saveWorkoutUseCase(currentWorkout, isUpdate)
                _events.emit(SummaryViewModelEvent.Saved)
            } catch (e: Exception) {
                _events.emit(SummaryViewModelEvent.Error(e.message ?: "Unknown error occurred"))
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }
}
