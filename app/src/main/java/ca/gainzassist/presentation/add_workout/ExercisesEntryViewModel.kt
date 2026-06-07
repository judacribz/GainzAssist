package ca.gainzassist.presentation.add_workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.gainzassist.domain.usecase.workout.ExerciseExistsUseCase
import ca.gainzassist.models.Exercise
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExercisesEntryViewModelState(
    val workoutName: String = "",
    val numberOfExercises: Int = 1,
    val selectedIndex: Int = 0,
    val exerciseNames: List<String> = emptyList(),
    val exercises: List<Exercise> = emptyList(),
    val enteredExerciseCount: Int = 0,
    val selectedExerciseName: String? = null,
    val duplicateExerciseError: String? = null
)

sealed interface ExercisesEntryViewModelEvent {
    data class GoToSummary(
        val workoutName: String,
        val exercises: List<Exercise>
    ) : ExercisesEntryViewModelEvent
}

class ExercisesEntryViewModel(
    private val exerciseExistsUseCase: ExerciseExistsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ExercisesEntryViewModelState())
    val state: StateFlow<ExercisesEntryViewModelState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ExercisesEntryViewModelEvent>()
    val events: SharedFlow<ExercisesEntryViewModelEvent> = _events.asSharedFlow()

    private val exercises = mutableListOf<Exercise>()

    fun initialize(workoutName: String, numberOfExercises: Int) {
        exercises.clear()
        for (i in 0 until numberOfExercises) {
            exercises.add(Exercise().apply { exerciseNumber = i })
        }

        _state.update {
            it.copy(
                workoutName = workoutName,
                numberOfExercises = numberOfExercises,
                selectedIndex = 0,
                exerciseNames = exercises.map { ex -> ex.name ?: "" },
                exercises = exercises.toList(),
                enteredExerciseCount = 0
            )
        }
    }

    fun onTabSelected(index: Int) {
        _state.update {
            it.copy(selectedIndex = index)
        }
    }

    fun onExerciseSubmitted(exercise: Exercise) {
        val currentState = _state.value
        val isUpdate = exercises[exercise.exerciseNumber].name != null

        exercises[exercise.exerciseNumber] = exercise

        val newEnteredCount = if (!isUpdate) {
            currentState.enteredExerciseCount + 1
        } else {
            currentState.enteredExerciseCount
        }

        val newSelectedIndex = if (newEnteredCount < currentState.numberOfExercises) {
            val firstEmptyIndex = exercises.indexOfFirst { it.name.isNullOrBlank() }
            if (firstEmptyIndex != -1) firstEmptyIndex else currentState.selectedIndex
        } else {
            currentState.selectedIndex
        }

        _state.update {
            it.copy(
                selectedIndex = newSelectedIndex,
                exerciseNames = exercises.map { ex -> ex.name ?: "" },
                exercises = exercises.toList(),
                enteredExerciseCount = newEnteredCount
            )
        }

        if (newEnteredCount >= currentState.numberOfExercises) {
            viewModelScope.launch {
                _events.emit(
                    ExercisesEntryViewModelEvent.GoToSummary(
                        workoutName = currentState.workoutName,
                        exercises = exercises.toList()
                    )
                )
            }
        }
    }

    fun onExerciseDeleted(index: Int) {
        val currentState = _state.value
        if (index < 0 || index >= exercises.size) return

        val deletedExercise = exercises.removeAt(index)
        val wasEntered = deletedExercise.name != null

        val newNumberOfExercises = currentState.numberOfExercises - 1
        val newEnteredCount = if (wasEntered) currentState.enteredExerciseCount - 1 else currentState.enteredExerciseCount

        // Re-index
        exercises.forEachIndexed { i, ex ->
            ex.exerciseNumber = i
        }

        val newSelectedIndex = if (index >= newNumberOfExercises) {
            maxOf(0, newNumberOfExercises - 1)
        } else {
            index
        }

        _state.update {
            it.copy(
                numberOfExercises = newNumberOfExercises,
                selectedIndex = newSelectedIndex,
                exerciseNames = exercises.map { ex -> ex.name ?: "" },
                exercises = exercises.toList(),
                enteredExerciseCount = newEnteredCount
            )
        }

        if (newNumberOfExercises in 1..newEnteredCount) {
            viewModelScope.launch {
                _events.emit(
                    ExercisesEntryViewModelEvent.GoToSummary(
                        workoutName = currentState.workoutName,
                        exercises = exercises.toList()
                    )
                )
            }
        }
    }

    fun onAddExerciseClicked() {
        val currentState = _state.value
        val newIndex = currentState.numberOfExercises

        exercises.add(Exercise().apply { exerciseNumber = newIndex })

        _state.update {
            it.copy(
                numberOfExercises = newIndex + 1,
                selectedIndex = newIndex,
                exerciseNames = exercises.map { ex -> ex.name ?: "" },
                exercises = exercises.toList()
            )
        }
    }
}
