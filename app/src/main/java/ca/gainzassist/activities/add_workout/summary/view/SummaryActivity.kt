package ca.gainzassist.activities.add_workout.summary.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import android.widget.Toast
import androidx.activity.compose.setContent
import ca.gainzassist.ui.components.GainzEdgeToEdgeBox
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ca.gainzassist.R
import ca.gainzassist.activities.add_workout.summary.SummaryViewModel
import ca.gainzassist.activities.add_workout.summary.SummaryViewModelEvent
import ca.gainzassist.core.constants.ExerciseConst
import ca.gainzassist.core.util.UI
import ca.gainzassist.data.local.preferences.Preferences
import ca.gainzassist.domain.model.Exercise
import ca.gainzassist.domain.model.Workout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.parceler.Parcels
import java.util.Locale
import kotlin.math.max

class SummaryActivity : AppCompatActivity() {

    enum class CallingActivity {
        WORKOUTS_LIST,
        EXERCISES_ENTRY
    }

    private var workoutId: Long = -1

    var workout: Workout? = null
    var exercises: ArrayList<Exercise>? = null
    val summaryViewModel: SummaryViewModel by viewModel()
    var ex: Exercise? = null

    companion object {
        const val EXTRA_WORKOUT = "ca.gainzassist.activities.add_workout.EXTRA_WORKOUT"
        const val EXTRA_CALLING_ACTIVITY =
            "ca.gainzassist.activities.add_workout.EXTRA_CALLING_ACTIVITY"

        private const val MIN_INT = 1
        private const val MIN_FLOAT = 5.0f
    }

    private fun sanitizeReps(value: String): Int =
        max(value.toIntOrNull() ?: MIN_INT, MIN_INT)

    private fun sanitizeSets(value: String): Int =
        max(value.toIntOrNull() ?: MIN_INT, MIN_INT)

    private fun sanitizeWeight(value: String, minWeight: Float): Float =
        max(value.toFloatOrNull() ?: minWeight, minWeight)

    private fun formatWeight(value: Float): String {
        return String.format(Locale.US, "%.1f", value)
    }

    private fun equipmentDisplayToModel(display: String): String {
        return when (display.trim().lowercase()) {
            "barbell" -> ExerciseConst.BARBELL
            "dumbbell" -> ExerciseConst.DUMBBELL
            "n/a", "na", "other" -> ExerciseConst.NA
            else -> ExerciseConst.NA
        }
    }

    private fun equipmentModelToDisplay(model: String?, options: List<String>): String {
        val fallback = options.firstOrNull() ?: "Barbell"
        return when (model?.trim()?.lowercase()) {
            ExerciseConst.BARBELL -> options.firstOrNull { it.equals("Barbell", ignoreCase = true) }
                ?: fallback

            ExerciseConst.DUMBBELL -> options.firstOrNull {
                it.equals(
                    "Dumbbell",
                    ignoreCase = true
                )
            } ?: fallback

            "n/a", "na" -> options.firstOrNull { it.equals("N/A", ignoreCase = true) } ?: fallback
            else -> fallback
        }
    }

    private fun exerciseNameExistsForOtherExercise(
        exercises: List<Exercise>,
        name: String,
        selectedExerciseNumber: Int?
    ): Boolean {
        return exercises.any { exercise ->
            exercise.exerciseNumber != selectedExerciseNumber &&
                    exercise.name.equals(name, ignoreCase = true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        UI.setInitTheme(this)

        val sourceIntent = intent
        workout = Parcels.unwrap(sourceIntent.getParcelableExtra(EXTRA_WORKOUT))
        val currentWorkout = workout ?: return
        workoutId = currentWorkout.id

        var isUpdateMode = false
        var initialMainButtonText = getString(R.string.add_workout)
        when (sourceIntent.getSerializableExtra(EXTRA_CALLING_ACTIVITY) as? CallingActivity) {
            CallingActivity.WORKOUTS_LIST -> {
                initialMainButtonText = getString(R.string.update_workout)
                isUpdateMode = true
            }

            else -> {
                // Keep default
            }
        }

        val initialWorkoutName = currentWorkout.name.orEmpty()
        exercises = currentWorkout.exercises

        summaryViewModel.initialize(currentWorkout, initialWorkoutName, exercises ?: emptyList())

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
                            Toast.makeText(this@SummaryActivity, event.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }

        setContent {
        GainzEdgeToEdgeBox {
            SummaryActivityContent(initialWorkoutName, initialMainButtonText, isUpdateMode, workout)
        
        }}
    }

    @Composable
    private fun SummaryActivityContent(
        initialWorkoutName: String,
        initialMainButtonText: String,
        isUpdateMode: Boolean,
        workout: Workout?
    ) {
        val state by summaryViewModel.state.collectAsStateWithLifecycle()

        //val exercisesState =
        remember(state.exercises) { mutableStateListOf<Exercise>().apply { addAll(state.exercises) } }
        var workoutName by rememberSaveable { mutableStateOf(initialWorkoutName) }
        var exerciseName by rememberSaveable { mutableStateOf("") }
        var weight by rememberSaveable { mutableStateOf(getString(R.string.starting_weight)) }
        var reps by rememberSaveable { mutableStateOf(getString(R.string.starting_reps)) }
        var sets by rememberSaveable { mutableStateOf(getString(R.string.starting_sets)) }
        var selectedEquipment by rememberSaveable { mutableStateOf("Barbell") }
        var selectedExerciseNumber by rememberSaveable { mutableStateOf<Int?>(null) }

        var workoutNameError by rememberSaveable { mutableStateOf<String?>(null) }
        var exerciseNameError by rememberSaveable { mutableStateOf<String?>(null) }
        var weightError by rememberSaveable { mutableStateOf<String?>(null) }
        var repsError by rememberSaveable { mutableStateOf<String?>(null) }
        var setsError by rememberSaveable { mutableStateOf<String?>(null) }

        // Sync ViewModel error message
        if (state.errorMessage != null && workoutNameError == null) {
            workoutNameError = state.errorMessage
        }

        val minWeight = when (selectedEquipment) {
            "Barbell" -> ExerciseConst.BB_MIN_WEIGHT
            "Dumbbell" -> ExerciseConst.DB_MIN_WEIGHT
            else -> ExerciseConst.MIN_WEIGHT
        }

        val uiState = SummaryUiState(
            workoutName = state.workoutName,
            exerciseName = exerciseName,
            selectedEquipment = selectedEquipment,
            equipmentOptions = resources.getStringArray(R.array.exerciseEquipment).toList(),
            weight = weight,
            reps = reps,
            sets = sets,
            exerciseNames = state.exercises.mapNotNull { it.name },
            selectedExerciseName = state.exercises.find { it.exerciseNumber == selectedExerciseNumber }?.name,
            showAddExerciseButton = selectedExerciseNumber == null,
            showUpdateExerciseButton = selectedExerciseNumber != null,
            mainWorkoutButtonText = initialMainButtonText,
            workoutNameError = workoutNameError,
            exerciseNameError = exerciseNameError,
            weightError = weightError,
            repsError = repsError,
            setsError = setsError,
            canDecrementWeight = (weight.toFloatOrNull() ?: 0f) > minWeight,
            canDecrementReps = (reps.toIntOrNull() ?: 0) > MIN_INT,
            canDecrementSets = (sets.toIntOrNull() ?: 0) > MIN_INT,
            isSaving = state.isSaving,

            )

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
                    val newMinWeight = when (eq) {
                        "Barbell" -> ExerciseConst.BB_MIN_WEIGHT
                        "Dumbbell" -> ExerciseConst.DB_MIN_WEIGHT
                        else -> ExerciseConst.MIN_WEIGHT
                    }
                    val currentWeight = weight.toFloatOrNull() ?: newMinWeight
                    if (currentWeight < newMinWeight) {
                        weight = newMinWeight.toString()
                    }
                },
                onWeightChanged = {
                    weight = it
                    weightError = null
                    val newWeight = it.toFloatOrNull() ?: minWeight
                    if (newWeight < minWeight) {
                        weight = minWeight.toString()
                    }
                },
                onRepsChanged = { reps = it; repsError = null },
                onSetsChanged = { sets = it; setsError = null },
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
                    selectedEquipment = "Barbell"
                    selectedExerciseNumber = null
                },
                onAddExercise = {
                    var isValid = true
                    if (exerciseName.isBlank()) {
                        exerciseNameError = getString(R.string.err_required); isValid = false
                    }
                    if (weight.isBlank()) {
                        weightError = getString(R.string.err_required); isValid = false
                    }
                    if (reps.isBlank()) {
                        repsError = getString(R.string.err_required); isValid = false
                    }
                    if (sets.isBlank()) {
                        setsError = getString(R.string.err_required); isValid = false
                    }

                    if (isValid) {
                        if (exerciseNameExistsForOtherExercise(
                                state.exercises,
                                exerciseName,
                                null
                            )
                        ) {
                            exerciseNameError =
                                getString(R.string.err_exercise_exists, exerciseName)
                        } else {
                            val finalReps = sanitizeReps(reps)
                            val finalSets = sanitizeSets(sets)
                            val finalWeight = sanitizeWeight(weight, minWeight)

                            reps = finalReps.toString()
                            sets = finalSets.toString()
                            weight = formatWeight(finalWeight)

                            val newExNumber = state.exercises.size
                            val exercise = Exercise(
                                newExNumber,
                                exerciseName,
                                "Strength",
                                equipmentDisplayToModel(selectedEquipment),
                                finalSets,
                                finalReps,
                                finalWeight,
                                Exercise.SetsType.MAIN_SET
                            ).apply { this.workoutId = this@SummaryActivity.workoutId }

                            val updatedExercises =
                                ArrayList(state.exercises).apply { add(exercise) }
                            exercises = updatedExercises
                            summaryViewModel.initialize(
                                workout,
                                state.workoutName,
                                updatedExercises
                            )

                            exerciseName = ""
                            exerciseNameError = null
                            reps = getString(R.string.starting_reps)
                            sets = getString(R.string.starting_sets)
                            weight = getString(R.string.starting_weight)
                            selectedEquipment = "Barbell"
                            selectedExerciseNumber = null
                        }
                    }
                },
                onUpdateExercise = {
                    var isValid = true
                    if (exerciseName.isBlank()) {
                        exerciseNameError = getString(R.string.err_required); isValid = false
                    }
                    if (weight.isBlank()) {
                        weightError = getString(R.string.err_required); isValid = false
                    }
                    if (reps.isBlank()) {
                        repsError = getString(R.string.err_required); isValid = false
                    }
                    if (sets.isBlank()) {
                        setsError = getString(R.string.err_required); isValid = false
                    }

                    if (isValid) {
                        if (exerciseNameExistsForOtherExercise(
                                state.exercises,
                                exerciseName,
                                selectedExerciseNumber
                            )
                        ) {
                            exerciseNameError =
                                getString(R.string.err_exercise_exists, exerciseName)
                        } else {
                            val finalReps = sanitizeReps(reps)
                            val finalSets = sanitizeSets(sets)
                            val finalWeight = sanitizeWeight(weight, minWeight)

                            reps = finalReps.toString()
                            sets = finalSets.toString()
                            weight = formatWeight(finalWeight)

                            val num = selectedExerciseNumber
                            if (num != null) {
                                val currentEx = state.exercises.find { it.exerciseNumber == num }
                                val exercise = Exercise(
                                    num,
                                    exerciseName,
                                    "Strength",
                                    equipmentDisplayToModel(selectedEquipment),
                                    finalSets,
                                    finalReps,
                                    finalWeight,
                                    Exercise.SetsType.MAIN_SET
                                ).apply {
                                    this.workoutId = this@SummaryActivity.workoutId
                                    this.id = currentEx?.id ?: -1
                                }

                                val updatedExercises = ArrayList(state.exercises).apply {
                                    val indexInState = indexOfFirst { it.exerciseNumber == num }
                                    if (indexInState != -1) this[indexInState] = exercise
                                }
                                exercises = updatedExercises
                                summaryViewModel.initialize(
                                    workout,
                                    state.workoutName,
                                    updatedExercises
                                )

                                exerciseName = ""
                                exerciseNameError = null
                                reps = getString(R.string.starting_reps)
                                sets = getString(R.string.starting_sets)
                                weight = getString(R.string.starting_weight)
                                selectedEquipment = "Barbell"
                                selectedExerciseNumber = null
                            }
                        }
                    }
                },
                onExerciseClicked = { exName ->
                    val clickedEx = state.exercises.find { it.name == exName }
                    if (clickedEx != null) {
                        ex = clickedEx
                        exerciseName = clickedEx.name ?: ""
                        selectedExerciseNumber = clickedEx.exerciseNumber
                        sets = clickedEx.sets.toString()
                        reps = clickedEx.reps.toString()
                        weight = formatWeight(clickedEx.weight)
                        selectedEquipment = equipmentModelToDisplay(
                            clickedEx.equipment,
                            resources.getStringArray(R.array.exerciseEquipment).toList()
                        )
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
                            "Error: No exercises added.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        summaryViewModel.onSaveClicked(isUpdateMode)
                    }
                }
            )
        )
    }
}
