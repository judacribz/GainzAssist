package ca.gainzassist.presentation.add_workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkoutEntryViewModelState(
    val workoutName: String = "",
    val numberOfExercises: String = "",
    val workoutNameError: String? = null,
    val numberOfExercisesError: String? = null
)

sealed interface WorkoutEntryViewModelEvent {
    data class ContinueToExercises(
        val workoutName: String,
        val numberOfExercises: Int
    ) : WorkoutEntryViewModelEvent
}

class WorkoutEntryViewModel : ViewModel() {

    private val _state = MutableStateFlow(WorkoutEntryViewModelState())
    val state: StateFlow<WorkoutEntryViewModelState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<WorkoutEntryViewModelEvent>()
    val events: SharedFlow<WorkoutEntryViewModelEvent> = _events.asSharedFlow()

    fun onWorkoutNameChanged(value: String) {
        _state.update {
            it.copy(
                workoutName = value,
                workoutNameError = null
            )
        }
    }

    fun onNumberOfExercisesChanged(value: String) {
        _state.update {
            it.copy(
                numberOfExercises = value,
                numberOfExercisesError = null
            )
        }
    }

    fun onContinueClicked() {
        val currentState = _state.value
        var isValid = true
        var nameError: String? = null
        var exercisesError: String? = null

        val name = currentState.workoutName.trim()
        if (name.isEmpty()) {
            nameError = "Required"
            isValid = false
        }

        val numEx = currentState.numberOfExercises.toIntOrNull()
        if (numEx == null || numEx <= 0) {
            exercisesError = "Must be a positive number"
            isValid = false
        }

        if (isValid && numEx != null) {
            viewModelScope.launch {
                _events.emit(
                    WorkoutEntryViewModelEvent.ContinueToExercises(
                        workoutName = name,
                        numberOfExercises = numEx
                    )
                )
            }
        } else {
            _state.update {
                it.copy(
                    workoutNameError = nameError,
                    numberOfExercisesError = exercisesError
                )
            }
        }
    }
}
