package ca.gainzassist.feature.exercises_entry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.gainzassist.core.constants.ExerciseConst
import ca.gainzassist.domain.model.Exercise
import ca.gainzassist.domain.model.Workout
import ca.gainzassist.domain.usecase.workout.ExerciseExistsUseCase
import ca.gainzassist.feature.exercises_entry.domain.usecase.BuildExerciseUseCase
import ca.gainzassist.feature.exercises_entry.domain.usecase.BuildWorkoutFromExerciseEntriesUseCase
import ca.gainzassist.feature.exercises_entry.domain.usecase.CheckDuplicateExerciseUseCase
import ca.gainzassist.feature.exercises_entry.domain.usecase.DeleteExerciseUseCase
import ca.gainzassist.feature.exercises_entry.domain.usecase.ValidateExerciseInputUseCase
import ca.gainzassist.feature.exercises_entry.domain.usecase.ValidateExerciseResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class ExerciseEntryInputState(
    val exerciseName: String = "",
    val selectedEquipment: String = "Barbell",
    val weight: String = "45.0",
    val reps: String = "10",
    val sets: String = "3",
    val showEnter: Boolean = true,
    val showUpdate: Boolean = false,
    val showDelete: Boolean = true,
    val hasDuplicateError: Boolean = false,
    val canDecrementWeight: Boolean = false,
    val canDecrementReps: Boolean = true,
    val canDecrementSets: Boolean = true,
    val hasNameError: Boolean = false,
    val hasWeightError: Boolean = false,
    val hasRepsError: Boolean = false,
    val hasSetsError: Boolean = false
)

data class ExercisesEntryViewModelState(
    val workoutName: String = "",
    val numberOfExercises: Int = 1,
    val selectedIndex: Int = 0,
    val exerciseNames: List<String> = emptyList(),
    val exercises: List<Exercise> = emptyList(),
    val enteredExerciseCount: Int = 0,
    val exerciseInputs: List<ExerciseEntryInputState> = emptyList()
)

sealed interface ExercisesEntryViewModelEvent {
    data class GoToSummary(val workout: Workout) : ExercisesEntryViewModelEvent
    data class ExerciseDeleted(val index: Int) : ExercisesEntryViewModelEvent
}

class ExercisesEntryViewModel(
    private val exerciseExistsUseCase: ExerciseExistsUseCase,
    private val validateExerciseInputUseCase: ValidateExerciseInputUseCase,
    private val checkDuplicateExerciseUseCase: CheckDuplicateExerciseUseCase,
    private val buildExerciseUseCase: BuildExerciseUseCase,
    private val deleteExerciseUseCase: DeleteExerciseUseCase,
    private val buildWorkoutFromExerciseEntriesUseCase: BuildWorkoutFromExerciseEntriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ExercisesEntryViewModelState())
    val state: StateFlow<ExercisesEntryViewModelState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ExercisesEntryViewModelEvent>()
    val events: SharedFlow<ExercisesEntryViewModelEvent> = _events.asSharedFlow()

    private val exercises = mutableListOf<Exercise>()

    fun initialize(workoutName: String, numberOfExercises: Int) {
        exercises.clear()
        val inputsList = mutableListOf<ExerciseEntryInputState>()
        for (i in 0 until numberOfExercises) {
            exercises.add(Exercise().apply { exerciseNumber = i })
            inputsList.add(
                ExerciseEntryInputState(
                    showDelete = numberOfExercises > 1
                )
            )
        }

        _state.update {
            it.copy(
                workoutName = workoutName,
                numberOfExercises = numberOfExercises,
                selectedIndex = 0,
                exerciseNames = exercises.map { ex -> ex.name ?: "" },
                exercises = exercises.toList(),
                enteredExerciseCount = 0,
                exerciseInputs = inputsList
            )
        }
    }

    fun onTabSelected(index: Int) {
        _state.update {
            it.copy(selectedIndex = index)
        }
    }

    private fun getEquipmentConfig(equipment: String): Pair<Float, Float> {
        return when (equipment) {
            "Barbell" -> Pair(ExerciseConst.BB_MIN_WEIGHT, ExerciseConst.BB_WEIGHT_CHANGE)
            "Dumbbell" -> Pair(ExerciseConst.DB_MIN_WEIGHT, ExerciseConst.DB_WEIGHT_CHANGE)
            else -> Pair(ExerciseConst.MIN_WEIGHT, ExerciseConst.WEIGHT_CHANGE)
        }
    }

    private fun formatWeight(value: Float): String {
        return String.format(Locale.getDefault(), "%.1f", value)
    }

    private fun updateInputState(index: Int, block: (ExerciseEntryInputState) -> ExerciseEntryInputState) {
        _state.update { currentState ->
            val inputs = currentState.exerciseInputs.toMutableList()
            if (index in 0 until inputs.size) {
                inputs[index] = block(inputs[index])
            }
            currentState.copy(exerciseInputs = inputs)
        }
    }

    fun onExerciseNameChanged(index: Int, name: String) {
        updateInputState(index) {
            it.copy(
                exerciseName = name,
                hasNameError = false,
                hasDuplicateError = false
            )
        }
    }

    fun onEquipmentSelected(index: Int, equipment: String) {
        val (minWeight, _) = getEquipmentConfig(equipment)
        updateInputState(index) { current ->
            val currentWeight = current.weight.toFloatOrNull() ?: minWeight
            val newWeight = if (currentWeight <= minWeight) minWeight else currentWeight
            current.copy(
                selectedEquipment = equipment,
                weight = formatWeight(newWeight),
                canDecrementWeight = newWeight > minWeight
            )
        }
    }

    fun onWeightChanged(index: Int, weightStr: String) {
        val (minWeight, _) = getEquipmentConfig(_state.value.exerciseInputs.getOrNull(index)?.selectedEquipment ?: "Barbell")
        updateInputState(index) { current ->
            val parsedWeight = weightStr.toFloatOrNull()
            val canDec = parsedWeight != null && parsedWeight > minWeight
            current.copy(
                weight = weightStr,
                hasWeightError = false,
                canDecrementWeight = canDec
            )
        }
    }

    fun onRepsChanged(index: Int, repsStr: String) {
        updateInputState(index) { current ->
            val parsedReps = repsStr.toIntOrNull()
            val canDec = parsedReps != null && parsedReps > 1
            current.copy(
                reps = repsStr,
                hasRepsError = false,
                canDecrementReps = canDec
            )
        }
    }

    fun onSetsChanged(index: Int, setsStr: String) {
        updateInputState(index) { current ->
            val parsedSets = setsStr.toIntOrNull()
            val canDec = parsedSets != null && parsedSets > 1
            current.copy(
                sets = setsStr,
                hasSetsError = false,
                canDecrementSets = canDec
            )
        }
    }

    fun onIncrementWeight(index: Int) {
        val input = _state.value.exerciseInputs.getOrNull(index) ?: return
        val (minWeight, weightChange) = getEquipmentConfig(input.selectedEquipment)
        val currentWeight = input.weight.toFloatOrNull() ?: minWeight
        val newWeight = currentWeight + weightChange
        updateInputState(index) {
            it.copy(
                weight = formatWeight(newWeight),
                canDecrementWeight = newWeight > minWeight
            )
        }
    }

    fun onDecrementWeight(index: Int) {
        val input = _state.value.exerciseInputs.getOrNull(index) ?: return
        val (minWeight, weightChange) = getEquipmentConfig(input.selectedEquipment)
        val currentWeight = input.weight.toFloatOrNull() ?: minWeight
        val newWeight = maxOf(currentWeight - weightChange, minWeight)
        updateInputState(index) {
            it.copy(
                weight = formatWeight(newWeight),
                canDecrementWeight = newWeight > minWeight
            )
        }
    }

    fun onIncrementReps(index: Int) {
        val input = _state.value.exerciseInputs.getOrNull(index) ?: return
        val currentReps = input.reps.toIntOrNull() ?: 10
        val newReps = currentReps + 1
        updateInputState(index) {
            it.copy(
                reps = newReps.toString(),
                canDecrementReps = newReps > 1
            )
        }
    }

    fun onDecrementReps(index: Int) {
        val input = _state.value.exerciseInputs.getOrNull(index) ?: return
        val currentReps = input.reps.toIntOrNull() ?: 10
        val newReps = maxOf(currentReps - 1, 1)
        updateInputState(index) {
            it.copy(
                reps = newReps.toString(),
                canDecrementReps = newReps > 1
            )
        }
    }

    fun onIncrementSets(index: Int) {
        val input = _state.value.exerciseInputs.getOrNull(index) ?: return
        val currentSets = input.sets.toIntOrNull() ?: 3
        val newSets = currentSets + 1
        updateInputState(index) {
            it.copy(
                sets = newSets.toString(),
                canDecrementSets = newSets > 1
            )
        }
    }

    fun onDecrementSets(index: Int) {
        val input = _state.value.exerciseInputs.getOrNull(index) ?: return
        val currentSets = input.sets.toIntOrNull() ?: 3
        val newSets = maxOf(currentSets - 1, 1)
        updateInputState(index) {
            it.copy(
                sets = newSets.toString(),
                canDecrementSets = newSets > 1
            )
        }
    }

    fun onExerciseSubmitted(index: Int) {
        val input = _state.value.exerciseInputs.getOrNull(index) ?: return

        val (minWeight, _) = getEquipmentConfig(input.selectedEquipment)
        val validationResult = validateExerciseInputUseCase(
            name = input.exerciseName,
            weightStr = input.weight,
            repsStr = input.reps,
            setsStr = input.sets,
            minWeight = minWeight
        )

        if (validationResult is ValidateExerciseResult.Error) {
            updateInputState(index) {
                it.copy(
                    hasNameError = validationResult.nameError != null,
                    hasWeightError = validationResult.weightError != null,
                    hasRepsError = validationResult.repsError != null,
                    hasSetsError = validationResult.setsError != null
                )
            }
            return
        }

        val success = validationResult as ValidateExerciseResult.Success

        val existingNames = exercises.map { it.name ?: "" }
        val isDuplicate = checkDuplicateExerciseUseCase(
            exerciseName = success.name,
            skipIndex = index,
            existingExerciseNames = existingNames
        )

        if (isDuplicate) {
            updateInputState(index) {
                it.copy(hasDuplicateError = true)
            }
            return
        }

        val newExercise = buildExerciseUseCase(
            index = index,
            name = success.name,
            equipment = input.selectedEquipment,
            sets = success.sets,
            reps = success.reps,
            weight = success.weight
        )

        val isUpdate = exercises[index].name != null
        exercises[index] = newExercise

        updateInputState(index) {
            it.copy(
                exerciseName = success.name,
                weight = formatWeight(success.weight),
                reps = success.reps.toString(),
                sets = success.sets.toString(),
                showEnter = false,
                showUpdate = true,
                hasNameError = false,
                hasWeightError = false,
                hasRepsError = false,
                hasSetsError = false,
                hasDuplicateError = false
            )
        }

        val newEnteredCount = if (!isUpdate) {
            _state.value.enteredExerciseCount + 1
        } else {
            _state.value.enteredExerciseCount
        }

        val newSelectedIndex = if (newEnteredCount < _state.value.numberOfExercises) {
            val firstEmptyIndex = exercises.indexOfFirst { it.name.isNullOrBlank() }
            if (firstEmptyIndex != -1) firstEmptyIndex else _state.value.selectedIndex
        } else {
            _state.value.selectedIndex
        }

        _state.update {
            it.copy(
                selectedIndex = newSelectedIndex,
                exerciseNames = exercises.map { ex -> ex.name ?: "" },
                exercises = exercises.toList(),
                enteredExerciseCount = newEnteredCount
            )
        }

        if (newEnteredCount >= _state.value.numberOfExercises) {
            viewModelScope.launch {
                val workout = buildWorkoutFromExerciseEntriesUseCase(
                    workoutName = _state.value.workoutName,
                    exercises = exercises.toList()
                )
                _events.emit(ExercisesEntryViewModelEvent.GoToSummary(workout))
            }
        }
    }

    fun onExerciseDeleted(index: Int) {
        val currentState = _state.value
        if (index < 0 || index >= exercises.size) return

        viewModelScope.launch {
            _events.emit(ExercisesEntryViewModelEvent.ExerciseDeleted(index))
        }

        val updatedExercises = deleteExerciseUseCase(exercises, index)
        exercises.clear()
        exercises.addAll(updatedExercises)

        val inputsList = currentState.exerciseInputs.toMutableList()
        if (index < inputsList.size) {
            inputsList.removeAt(index)
        }

        val newNumberOfExercises = currentState.numberOfExercises - 1
        val wasEntered = currentState.exercises.getOrNull(index)?.name != null
        val newEnteredCount = if (wasEntered) currentState.enteredExerciseCount - 1 else currentState.enteredExerciseCount

        val newSelectedIndex = if (index >= newNumberOfExercises) {
            maxOf(0, newNumberOfExercises - 1)
        } else {
            index
        }

        val finalInputs = inputsList.mapIndexed { i, input ->
            input.copy(
                showDelete = newNumberOfExercises > 1
            )
        }

        _state.update {
            it.copy(
                numberOfExercises = newNumberOfExercises,
                selectedIndex = newSelectedIndex,
                exerciseNames = exercises.map { ex -> ex.name ?: "" },
                exercises = exercises.toList(),
                enteredExerciseCount = newEnteredCount,
                exerciseInputs = finalInputs
            )
        }

        if (newNumberOfExercises in 1..newEnteredCount) {
            viewModelScope.launch {
                val workout = buildWorkoutFromExerciseEntriesUseCase(
                    workoutName = currentState.workoutName,
                    exercises = exercises.toList()
                )
                _events.emit(ExercisesEntryViewModelEvent.GoToSummary(workout))
            }
        }
    }

    fun onAddExerciseClicked() {
        val currentState = _state.value
        val newIndex = currentState.numberOfExercises

        val newEx = Exercise().apply { exerciseNumber = newIndex }
        exercises.add(newEx)

        val inputsList = currentState.exerciseInputs.toMutableList()
        val updatedInputs = inputsList.map { it.copy(showDelete = true) }.toMutableList()
        updatedInputs.add(ExerciseEntryInputState(showDelete = true))

        _state.update {
            it.copy(
                numberOfExercises = newIndex + 1,
                selectedIndex = newIndex,
                exerciseNames = exercises.map { ex -> ex.name ?: "" },
                exercises = exercises.toList(),
                exerciseInputs = updatedInputs
            )
        }
    }

    fun updateExFields(index: Int, exercise: Exercise) {
        val (minWeight, _) = getEquipmentConfig(exercise.equipment.orEmpty())
        updateInputState(index) {
            it.copy(
                exerciseName = exercise.name.orEmpty(),
                selectedEquipment = exercise.equipment ?: "Barbell",
                weight = formatWeight(exercise.weight),
                reps = exercise.reps.toString(),
                sets = exercise.sets.toString(),
                showEnter = false,
                showUpdate = true,
                canDecrementWeight = exercise.weight > minWeight,
                canDecrementReps = exercise.reps > 1,
                canDecrementSets = exercise.sets > 1
            )
        }
    }
}
