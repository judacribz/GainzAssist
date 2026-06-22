package ca.gainzassist.feature.workout_entry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.gainzassist.R
import ca.gainzassist.feature.workout_entry.domain.usecase.ValidateWorkoutEntryUseCase
import ca.gainzassist.feature.workout_entry.domain.usecase.WorkoutEntryValidationResult
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
    val workoutNameErrorResId: Int? = null,
    val numberOfExercisesErrorResId: Int? = null
)

sealed interface WorkoutEntryViewModelEvent {

    data class ContinueToExercises(val workoutName: String, val numberOfExercises: Int) : WorkoutEntryViewModelEvent
}

class WorkoutEntryViewModel(private val validateWorkoutEntryUseCase: ValidateWorkoutEntryUseCase) : ViewModel() {

    private val _state = MutableStateFlow(WorkoutEntryViewModelState())
    val state: StateFlow<WorkoutEntryViewModelState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<WorkoutEntryViewModelEvent>()
    val events: SharedFlow<WorkoutEntryViewModelEvent> = _events.asSharedFlow()

    fun onWorkoutNameChanged(value: String) {
        _state.update {
            it.copy(
                workoutName = value,
                workoutNameErrorResId = null
            )
        }
    }

    fun onNumberOfExercisesChanged(value: String) {
        _state.update {
            it.copy(
                numberOfExercises = value,
                numberOfExercisesErrorResId = null
            )
        }
    }

    fun onContinueClicked() {
        val currentState = _state.value
        val numEx = currentState.numberOfExercises.toIntOrNull()

        when (validateWorkoutEntryUseCase(currentState.numberOfExercises)) {
            WorkoutEntryValidationResult.Success -> {
                if (numEx != null) {
                    viewModelScope.launch {
                        _events.emit(
                            WorkoutEntryViewModelEvent.ContinueToExercises(
                                workoutName = currentState.workoutName.trim(),
                                numberOfExercises = numEx
                            )
                        )
                    }
                }
            }
            WorkoutEntryValidationResult.InvalidNumber -> {
                _state.update {
                    it.copy(
                        numberOfExercisesErrorResId = R.string.err_must_be_positive_number
                    )
                }
            }
        }
    }
}
