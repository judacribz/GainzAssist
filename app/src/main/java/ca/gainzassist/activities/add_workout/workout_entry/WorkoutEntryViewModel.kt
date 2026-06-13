package ca.gainzassist.activities.add_workout.workout_entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.gainzassist.core.constants.ExerciseConst
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
    val numberOfExercises: String = "3",
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
        var exercisesError: String? = null

        val numEx = currentState.numberOfExercises.toIntOrNull()
        if (numEx == null || numEx < ExerciseConst.MIN_INT) {
            exercisesError = "Must be a positive number"
            isValid = false
        }

        if (isValid && numEx != null) {
            viewModelScope.launch {
                _events.emit(
                    WorkoutEntryViewModelEvent.ContinueToExercises(
                        workoutName = currentState.workoutName.trim(),
                        numberOfExercises = numEx
                    )
                )
            }
        } else {
            _state.update {
                it.copy(
                    numberOfExercisesError = exercisesError
                )
            }
        }
    }
}
