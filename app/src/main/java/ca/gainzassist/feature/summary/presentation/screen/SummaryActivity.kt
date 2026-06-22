package ca.gainzassist.feature.summary.presentation.screen

import android.os.Bundle
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ca.gainzassist.R
import ca.gainzassist.activities.base.GainzBaseActivity
import ca.gainzassist.core.constants.ExerciseConst
import ca.gainzassist.data.local.preferences.Preferences
import ca.gainzassist.domain.model.Exercise
import ca.gainzassist.domain.model.Workout
import ca.gainzassist.feature.summary.presentation.viewmodel.SummaryViewModel
import ca.gainzassist.feature.summary.presentation.viewmodel.SummaryViewModelEvent
import ca.gainzassist.feature.summary.presentation.viewmodel.SummaryViewModelState
import java.util.Locale
import kotlin.math.max
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

enum class CallingActivity {
    WORKOUTS_LIST,
    EXERCISES_ENTRY
}

class SummaryActivity : GainzBaseActivity() {

    private var workoutId: Long = -1
    var workout: Workout? = null
    var exercises: ArrayList<Exercise>? = null
    val summaryViewModel: SummaryViewModel by viewModel()
    var ex: Exercise? = null
    private var isUpdateMode = false
    private var initialMainButtonText = ""
    private var initialWorkoutName = ""

    override fun onBeforeSetContent() {
        val sourceIntent = intent
        val w = IntentCompat.getParcelableExtra(
            /* in = */ sourceIntent,
            /* name = */ EXTRA_WORKOUT,
            /* clazz = */ Workout::class.java
        )
        workout = w
        val currentWorkout = w ?: return
        workoutId = currentWorkout.id
        initialMainButtonText = getString(R.string.add_workout)
        when (IntentCompat.getSerializableExtra(
            /* in = */ sourceIntent,
            /* key = */ EXTRA_CALLING_ACTIVITY,
            /* clazz = */ CallingActivity::class.java
        )) {
            CallingActivity.WORKOUTS_LIST -> {
                initialMainButtonText = getString(R.string.update_workout)
                isUpdateMode = true
            }

            else -> {
                // Keep default
            }
        }
        initialWorkoutName = currentWorkout.name.orEmpty()
        exercises = currentWorkout.exercises
        summaryViewModel.initialize(currentWorkout, initialWorkoutName, exercises ?: emptyList())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                summaryViewModel.events.collectLatest { event ->
                    when (event) {
                        is SummaryViewModelEvent.Saved -> {
                            if (isUpdateMode) {
                                val stateValue = summaryViewModel.state.value
                                val wName = stateValue.workoutName
                                if (Preferences.removeIncompleteWorkoutPref(
                                        this@SummaryActivity,
                                        wName
                                    )
                                ) {
                                    Preferences.removeIncompleteSessionPref(
                                        this@SummaryActivity,
                                        wName
                                    )
                                }
                            }
                            setResult(RESULT_OK)
                            finish()
                        }

                        is SummaryViewModelEvent.Error -> {
                            Toast.makeText(
                                /* context = */ this@SummaryActivity,
                                /* text = */ event.message.asString(this@SummaryActivity),
                                /* duration = */ Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    @Composable
    override fun InnerContent() = SummaryActivityContent(
        initialWorkoutName = initialWorkoutName,
        initialMainButtonText = initialMainButtonText,
        isUpdateMode = isUpdateMode,
        workout = workout
    )

    private fun sanitizeReps(value: String): Int = max(value.toIntOrNull() ?: MIN_INT, MIN_INT)

    private fun sanitizeSets(value: String): Int = max(value.toIntOrNull() ?: MIN_INT, MIN_INT)

    private fun sanitizeWeight(
        value: String,
        minWeight: Float
    ): Float = max(value.toFloatOrNull() ?: minWeight, minWeight)

    private fun formatWeight(value: Float): String = String.format(Locale.US, "%.1f", value)

    private fun equipmentDisplayToModel(
        display: String
    ): String = when (display.trim().lowercase()) {
        "barbell" -> ExerciseConst.BARBELL
        "dumbbell" -> ExerciseConst.DUMBBELL
        else -> ExerciseConst.NA
    }

    private fun equipmentModelToDisplay(model: String?, options: List<String>): String {
        val fallback = options.firstOrNull() ?: ExerciseConst.BARBELL.replaceFirstChar {
            it.uppercase()
        }
        return when (model?.trim()?.lowercase()) {
            ExerciseConst.BARBELL -> options.find {
                it.equals(
                    ExerciseConst.BARBELL,
                    ignoreCase = true
                )
            }
                ?: fallback

            ExerciseConst.DUMBBELL -> options.find {
                it.equals(
                    ExerciseConst.DUMBBELL,
                    ignoreCase = true
                )
            } ?: fallback

            ExerciseConst.NA.lowercase() -> options.find {
                it.equals(
                    ExerciseConst.NA,
                    ignoreCase = true
                )
            } ?: fallback

            else -> fallback
        }
    }

    private fun exerciseNameExistsForOtherExercise(
        exercises: List<Exercise>,
        name: String,
        selectedExerciseNumber: Int?
    ): Boolean = exercises.any { exercise ->
        (exercise.exerciseNumber != selectedExerciseNumber) &&
            exercise.name.equals(name, ignoreCase = true)
    }

    @Composable
    private fun SummaryActivityContent(
        initialWorkoutName: String,
        initialMainButtonText: String,
        isUpdateMode: Boolean,
        workout: Workout?
    ) {
        val state by summaryViewModel.state.collectAsStateWithLifecycle()
        remember(state.exercises) { mutableStateListOf<Exercise>().apply { addAll(state.exercises) } }

        var workoutName by rememberSaveable { mutableStateOf(initialWorkoutName) }
        var exerciseName by rememberSaveable { mutableStateOf("") }
        var weight by rememberSaveable { mutableStateOf(getString(R.string.starting_weight)) }
        var reps by rememberSaveable { mutableStateOf(getString(R.string.starting_reps)) }
        var sets by rememberSaveable { mutableStateOf(getString(R.string.starting_sets)) }
        val equipmentOptions = resources.getStringArray(R.array.exerciseEquipment).toList()
        var selectedEquipment by rememberSaveable {
            mutableStateOf(equipmentOptions.firstOrNull().orEmpty())
        }
        var selectedExerciseNumber by rememberSaveable { mutableStateOf<Int?>(null) }
        var workoutNameError by rememberSaveable { mutableStateOf<String?>(null) }
        var exerciseNameError by rememberSaveable { mutableStateOf<String?>(null) }
        var weightError by rememberSaveable { mutableStateOf<String?>(null) }
        var repsError by rememberSaveable { mutableStateOf<String?>(null) }
        var setsError by rememberSaveable { mutableStateOf<String?>(null) }
        val minWeight = getMinWeight(selectedEquipment)
        val uiState = createSummaryUiState(
            state = state,
            input = ExerciseInput(exerciseName, weight, reps, sets, selectedEquipment, minWeight),
            equipmentOptions = equipmentOptions,
            selectedExerciseNumber = selectedExerciseNumber,
            initialMainButtonText = initialMainButtonText,
            errors = ExerciseErrors(
                workoutNameError,
                exerciseNameError,
                weightError,
                repsError,
                setsError
            )
        )

        if ((state.errorMessage != null) && (workoutNameError == null)) {
            workoutNameError = state.errorMessage?.asString()
        }

        SummaryScreenContent(
            uiState = uiState,
            actions = SummaryScreenActions(
                onBack = { finish() },
                onWorkoutNameChanged = {
                    workoutName = it
                    workoutNameError = null
                    summaryViewModel.initialize(workout, it, state.exercises)
                },
                onExerciseNameChanged = {
                    exerciseName = it
                    exerciseNameError = null
                },
                onEquipmentSelected = { eq ->
                    selectedEquipment = eq
                    val newMinWeight = getMinWeight(eq)
                    val currentWeight = weight.toFloatOrNull() ?: newMinWeight
                    if (currentWeight < newMinWeight) weight = newMinWeight.toString()
                },
                onWeightChanged = {
                    weight = it
                    weightError = null
                    val newWeight = it.toFloatOrNull() ?: minWeight
                    if (newWeight < minWeight) weight = minWeight.toString()
                },
                onRepsChanged = {
                    reps = it
                    repsError = null
                },
                onSetsChanged = {
                    sets = it
                    setsError = null
                },
                onIncrementWeight = {
                    val current = weight.toFloatOrNull() ?: minWeight
                    weight = formatWeight(current + MIN_FLOAT)
                },
                onDecrementWeight = {
                    val current = weight.toFloatOrNull() ?: minWeight
                    weight = formatWeight(max(current - MIN_FLOAT, minWeight))
                },
                onIncrementReps = {
                    val current = reps.toIntOrNull() ?: MIN_INT
                    reps = (current + MIN_INT).toString()
                },
                onDecrementReps = {
                    val current = reps.toIntOrNull() ?: MIN_INT
                    reps = max(current - MIN_INT, MIN_INT).toString()
                },
                onIncrementSets = {
                    val current = sets.toIntOrNull() ?: MIN_INT
                    sets = (current + MIN_INT).toString()
                },
                onDecrementSets = {
                    val current = sets.toIntOrNull() ?: MIN_INT
                    sets = max(current - MIN_INT, MIN_INT).toString()
                },
                onClearExercise = {
                    exerciseName = ""
                    exerciseNameError = null
                    reps = getString(R.string.starting_reps)
                    sets = getString(R.string.starting_sets)
                    weight = getString(R.string.starting_weight)
                    selectedEquipment = equipmentOptions.firstOrNull().orEmpty()
                    selectedExerciseNumber = null
                },
                onAddExercise = {
                    val result = validateExercise(
                        exerciseName,
                        weight,
                        reps,
                        sets,
                        state.exercises,
                        null,
                        minWeight
                    )
                    exerciseNameError = result.nameError
                    weightError = result.weightError
                    repsError = result.repsError
                    setsError = result.setsError

                    if (result.isValid) {
                        reps = result.sanitizedReps.toString()
                        sets = result.sanitizedSets.toString()
                        weight = formatWeight(result.sanitizedWeight)
                        onAddExerciseAction(
                            state,
                            workout,
                            exerciseName,
                            selectedEquipment,
                            result.sanitizedSets,
                            result.sanitizedReps,
                            result.sanitizedWeight
                        )
                        exerciseName = ""
                        exerciseNameError = null
                        reps = getString(R.string.starting_reps)
                        sets = getString(R.string.starting_sets)
                        weight = getString(R.string.starting_weight)
                        selectedEquipment = equipmentOptions.firstOrNull().orEmpty()
                        selectedExerciseNumber = null
                    }
                },
                onUpdateExercise = {
                    val result = validateExercise(
                        exerciseName,
                        weight,
                        reps,
                        sets,
                        state.exercises,
                        selectedExerciseNumber,
                        minWeight
                    )
                    exerciseNameError = result.nameError
                    weightError = result.weightError
                    repsError = result.repsError
                    setsError = result.setsError

                    if (result.isValid && selectedExerciseNumber != null) {
                        reps = result.sanitizedReps.toString()
                        sets = result.sanitizedSets.toString()
                        weight = formatWeight(result.sanitizedWeight)
                        onUpdateExerciseAction(
                            state,
                            workout,
                            ExerciseUpdateParams(
                                exerciseName,
                                selectedEquipment,
                                result.sanitizedSets,
                                result.sanitizedReps,
                                result.sanitizedWeight
                            ),
                            selectedExerciseNumber!!
                        )
                        exerciseName = ""
                        exerciseNameError = null
                        reps = getString(R.string.starting_reps)
                        sets = getString(R.string.starting_sets)
                        weight = getString(R.string.starting_weight)
                        selectedEquipment = equipmentOptions.firstOrNull().orEmpty()
                        selectedExerciseNumber = null
                    }
                },
                onExerciseClicked = { exName ->
                    state.exercises.find { it.name == exName }?.let { clickedEx ->
                        ex = clickedEx
                        exerciseName = clickedEx.name ?: ""
                        selectedExerciseNumber = clickedEx.exerciseNumber
                        sets = clickedEx.sets.toString()
                        reps = clickedEx.reps.toString()
                        weight = formatWeight(clickedEx.weight)
                        selectedEquipment = equipmentModelToDisplay(clickedEx.equipment, equipmentOptions)
                    }
                },
                onDiscardWorkout = {
                    setResult(RESULT_CANCELED)
                    finish()
                },
                onAddOrUpdateWorkout = {
                    if (state.workoutName.isBlank()) {
                        workoutNameError = getString(R.string.err_required)
                    } else if (state.exercises.isEmpty()) {
                        Toast.makeText(
                            this@SummaryActivity,
                            getString(R.string.err_no_exercises),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        summaryViewModel.onSaveClicked(isUpdateMode)
                    }
                }
            )
        )
    }

    private fun getMinWeight(equipment: String): Float = when (equipment.lowercase()) {
        ExerciseConst.BARBELL -> ExerciseConst.BB_MIN_WEIGHT
        ExerciseConst.DUMBBELL -> ExerciseConst.DB_MIN_WEIGHT
        else -> ExerciseConst.MIN_WEIGHT
    }

    private data class ExerciseInput(
        val name: String,
        val weight: String,
        val reps: String,
        val sets: String,
        val equipment: String,
        val minWeight: Float
    )

    private data class ExerciseErrors(
        val workoutNameError: String?,
        val exerciseNameError: String?,
        val weightError: String?,
        val repsError: String?,
        val setsError: String?
    )

    private fun createSummaryUiState(
        state: SummaryViewModelState,
        input: ExerciseInput,
        equipmentOptions: List<String>,
        selectedExerciseNumber: Int?,
        initialMainButtonText: String,
        errors: ExerciseErrors
    ) = SummaryUiState(
        workoutName = state.workoutName,
        exerciseName = input.name,
        selectedEquipment = input.equipment,
        equipmentOptions = equipmentOptions,
        weight = input.weight,
        reps = input.reps,
        sets = input.sets,
        exerciseNames = state.exercises.mapNotNull { it.name },
        selectedExerciseName = state.exercises.find { it.exerciseNumber == selectedExerciseNumber }?.name,
        showAddExerciseButton = selectedExerciseNumber == null,
        showUpdateExerciseButton = selectedExerciseNumber != null,
        mainWorkoutButtonText = initialMainButtonText,
        workoutNameError = errors.workoutNameError,
        exerciseNameError = errors.exerciseNameError,
        weightError = errors.weightError,
        repsError = errors.repsError,
        setsError = errors.setsError,
        canDecrementWeight = (input.weight.toFloatOrNull() ?: 0f) > input.minWeight,
        canDecrementReps = (input.reps.toIntOrNull() ?: 0) > MIN_INT,
        canDecrementSets = (input.sets.toIntOrNull() ?: 0) > MIN_INT,
        isSaving = state.isSaving,
    )

    private data class ExerciseValidationResult(
        val isValid: Boolean,
        val nameError: String? = null,
        val weightError: String? = null,
        val repsError: String? = null,
        val setsError: String? = null,
        val sanitizedReps: Int = 0,
        val sanitizedSets: Int = 0,
        val sanitizedWeight: Float = 0f
    )

    private fun validateExercise(
        name: String,
        weight: String,
        reps: String,
        sets: String,
        currentExercises: List<Exercise>,
        selectedExerciseNumber: Int?,
        minWeight: Float
    ): ExerciseValidationResult {

        var nameErr: String? = null
        var weightErr: String? = null
        var repsErr: String? = null
        var setsErr: String? = null

        if (name.isBlank()) nameErr = getString(R.string.err_required)
        if (weight.isBlank()) weightErr = getString(R.string.err_required)
        if (reps.isBlank()) repsErr = getString(R.string.err_required)
        if (sets.isBlank()) setsErr = getString(R.string.err_required)

        if (nameErr != null || weightErr != null || repsErr != null || setsErr != null) {
            return ExerciseValidationResult(isValid = false, nameErr, weightErr, repsErr, setsErr)
        }

        if (exerciseNameExistsForOtherExercise(currentExercises, name, selectedExerciseNumber)) {
            return ExerciseValidationResult(false, nameError = getString(R.string.err_exercise_exists, name))
        }

        return ExerciseValidationResult(
            isValid = true,
            sanitizedReps = sanitizeReps(reps),
            sanitizedSets = sanitizeSets(sets),
            sanitizedWeight = sanitizeWeight(weight, minWeight)
        )
    }

    private fun onAddExerciseAction(
        state: SummaryViewModelState,
        workout: Workout?,
        name: String,
        equipment: String,
        sets: Int,
        reps: Int,
        weight: Float
    ) {
        val exercise = Exercise(
            state.exercises.size,
            name,
            ExerciseConst.STRENGTH,
            equipmentDisplayToModel(equipment),
            sets,
            reps,
            weight,
            Exercise.SetsType.MAIN_SET
        ).apply { this.workoutId = this@SummaryActivity.workoutId }

        val updatedExercises = ArrayList<Exercise>(state.exercises).apply { add(exercise) }
        exercises = updatedExercises
        summaryViewModel.initialize(workout, state.workoutName, updatedExercises)
    }

    private data class ExerciseUpdateParams(
        val name: String,
        val equipment: String,
        val sets: Int,
        val reps: Int,
        val weight: Float
    )

    private fun onUpdateExerciseAction(
        state: SummaryViewModelState,
        workout: Workout?,
        params: ExerciseUpdateParams,
        selectedNumber: Int
    ) {

        val currentEx = state.exercises.find { it.exerciseNumber == selectedNumber }
        val exercise = Exercise(
            selectedNumber,
            params.name,
            ExerciseConst.STRENGTH,
            equipmentDisplayToModel(params.equipment),
            params.sets,
            params.reps,
            params.weight,
            Exercise.SetsType.MAIN_SET
        ).apply {
            this.workoutId = this@SummaryActivity.workoutId
            this.id = currentEx?.id ?: -1
        }

        val updatedExercises = ArrayList<Exercise>(state.exercises).apply {
            val index = indexOfFirst { it.exerciseNumber == selectedNumber }
            if (index != -1) this[index] = exercise
        }
        exercises = updatedExercises
        summaryViewModel.initialize(workout, state.workoutName, updatedExercises)
    }

    companion object {
        const val EXTRA_WORKOUT = "ca.gainzassist.activities.add_workout.EXTRA_WORKOUT"
        const val EXTRA_CALLING_ACTIVITY =
            "ca.gainzassist.activities.add_workout.EXTRA_CALLING_ACTIVITY"
        private const val MIN_INT = 1
        private const val MIN_FLOAT = 5.0f
    }
}
