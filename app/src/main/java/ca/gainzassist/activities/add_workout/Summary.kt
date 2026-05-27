package ca.gainzassist.activities.add_workout

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import ca.gainzassist.R
import ca.gainzassist.constants.ExerciseConst
import ca.gainzassist.constants.ExerciseConst.BB_MIN_WEIGHT
import ca.gainzassist.constants.ExerciseConst.DB_MIN_WEIGHT
import ca.gainzassist.constants.ExerciseConst.MIN_WEIGHT
import ca.gainzassist.models.Exercise
import ca.gainzassist.models.Exercise.SetsType.MAIN_SET
import ca.gainzassist.models.Workout
import ca.gainzassist.models.db.WorkoutViewModel
import ca.gainzassist.util.Preferences.removeIncompleteSessionPref
import ca.gainzassist.util.Preferences.removeIncompleteWorkoutPref
import ca.gainzassist.util.UI.setInitTheme
import ca.gainzassist.util.firebase.Database.addWorkoutFirebase
import org.parceler.Parcels
import java.util.Locale
import kotlin.math.max

class Summary : AppCompatActivity() {

    enum class CallingActivity {
        WORKOUTS_LIST,
        EXERCISES_ENTRY
    }

    companion object {
        const val EXTRA_WORKOUT = "ca.gainzassist.activities.add_workout.EXTRA_WORKOUT"
        const val EXTRA_CALLING_ACTIVITY =
            "ca.gainzassist.activities.add_workout.EXTRA_CALLING_ACTIVITY"

        private const val MIN_INT = 1
        private const val MIN_FLOAT = 5.0f
    }

    private var workoutId: Long = -1

    var workout: Workout? = null
    var exercises: ArrayList<Exercise>? = null
    val workoutViewModel by viewModels<WorkoutViewModel>()
    var ex: Exercise? = null

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
            ExerciseConst.BARBELL -> options.firstOrNull { it.equals("Barbell", ignoreCase = true) } ?: fallback
            ExerciseConst.DUMBBELL -> options.firstOrNull { it.equals("Dumbbell", ignoreCase = true) } ?: fallback
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
        setInitTheme(this)

        val sourceIntent = intent
        workout = Parcels.unwrap(sourceIntent.getParcelableExtra(EXTRA_WORKOUT))
        val currentWorkout = workout ?: return
        workoutId = currentWorkout.id

        var initialMainButtonText = getString(R.string.add_workout)
        when (sourceIntent.getSerializableExtra(EXTRA_CALLING_ACTIVITY) as? CallingActivity) {
            CallingActivity.WORKOUTS_LIST -> {
                initialMainButtonText = getString(R.string.update_workout)
            }

            else -> {
                // Keep default
            }
        }

        val initialWorkoutName = currentWorkout.name.orEmpty()
        exercises = currentWorkout.exercises

        setContent {
            val exercisesState =
                remember { mutableStateListOf<Exercise>().apply { exercises?.let { addAll(it) } } }
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

            val minWeight = when (selectedEquipment) {
                "Barbell" -> BB_MIN_WEIGHT
                "Dumbbell" -> DB_MIN_WEIGHT
                else -> MIN_WEIGHT
            }

            val uiState = SummaryUiState(
                workoutName = workoutName,
                exerciseName = exerciseName,
                selectedEquipment = selectedEquipment,
                equipmentOptions = resources.getStringArray(R.array.exerciseEquipment).toList(),
                weight = weight,
                reps = reps,
                sets = sets,
                exerciseNames = exercisesState.mapNotNull { it.name },
                selectedExerciseName = exercisesState.find { it.exerciseNumber == selectedExerciseNumber }?.name,
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
                canDecrementSets = (sets.toIntOrNull() ?: 0) > MIN_INT
            )

            SummaryScreenContent(
                uiState = uiState,
                onBack = { finish() },
                onWorkoutNameChanged = {
                    workoutName = it
                    workoutNameError = null
                },
                onExerciseNameChanged = {
                    exerciseName = it
                    exerciseNameError = null
                },
                onEquipmentSelected = { eq ->
                    selectedEquipment = eq
                    val newMinWeight = when (eq) {
                        "Barbell" -> BB_MIN_WEIGHT
                        "Dumbbell" -> DB_MIN_WEIGHT
                        else -> MIN_WEIGHT
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
                    weight = (current + MIN_FLOAT).toString()
                },
                onDecrementWeight = {
                    val current = weight.toFloatOrNull() ?: minWeight
                    weight = Math.max(current - MIN_FLOAT, minWeight).toString()
                },
                onIncrementReps = {
                    val current = reps.toIntOrNull() ?: MIN_INT
                    reps = (current + MIN_INT).toString()
                },
                onDecrementReps = {
                    val current = reps.toIntOrNull() ?: MIN_INT
                    reps = Math.max(current - MIN_INT, MIN_INT).toString()
                },
                onIncrementSets = {
                    val current = sets.toIntOrNull() ?: MIN_INT
                    sets = (current + MIN_INT).toString()
                },
                onDecrementSets = {
                    val current = sets.toIntOrNull() ?: MIN_INT
                    sets = Math.max(current - MIN_INT, MIN_INT).toString()
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
                        if (exerciseNameExistsForOtherExercise(exercisesState, exerciseName, null)) {
                            exerciseNameError =
                                getString(R.string.err_exercise_exists, exerciseName)
                        } else {
                            val finalReps = sanitizeReps(reps)
                            val finalSets = sanitizeSets(sets)
                            val finalWeight = sanitizeWeight(weight, minWeight)

                            reps = finalReps.toString()
                            sets = finalSets.toString()
                            weight = formatWeight(finalWeight)

                            val newExNumber = exercisesState.size
                            val exercise = Exercise(
                                newExNumber,
                                exerciseName,
                                "Strength",
                                equipmentDisplayToModel(selectedEquipment),
                                finalSets,
                                finalReps,
                                finalWeight,
                                MAIN_SET
                            ).apply { this.workoutId = this@Summary.workoutId }

                            exercises?.add(exercise)
                            exercisesState.add(exercise)

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
                        if (exerciseNameExistsForOtherExercise(exercisesState, exerciseName, selectedExerciseNumber)) {
                            exerciseNameError = getString(R.string.err_exercise_exists, exerciseName)
                        } else {
                            val finalReps = sanitizeReps(reps)
                            val finalSets = sanitizeSets(sets)
                            val finalWeight = sanitizeWeight(weight, minWeight)

                            reps = finalReps.toString()
                            sets = finalSets.toString()
                            weight = formatWeight(finalWeight)

                            val num = selectedExerciseNumber
                            if (num != null) {
                                val currentEx = exercisesState.find { it.exerciseNumber == num }
                                val exercise = Exercise(
                                    num,
                                    exerciseName,
                                    "Strength",
                                    equipmentDisplayToModel(selectedEquipment),
                                    finalSets,
                                    finalReps,
                                    finalWeight,
                                    MAIN_SET
                                ).apply {
                                    this.workoutId = this@Summary.workoutId
                                    this.id = currentEx?.id ?: -1
                                }

                                exercises?.let { exList ->
                                    val indexInOriginal = exList.indexOfFirst { it.exerciseNumber == num }
                                    if (indexInOriginal != -1) exList[indexInOriginal] = exercise
                                }
                                val indexInState = exercisesState.indexOfFirst { it.exerciseNumber == num }
                                if (indexInState != -1) exercisesState[indexInState] = exercise

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
                    val clickedEx = exercisesState.find { it.name == exName }
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
                    setResult(RESULT_OK)
                    finish()
                },
                onAddOrUpdateWorkout = {
                    if (workoutName.isBlank()) {
                        workoutNameError = getString(R.string.err_required)
                    } else {
                        val currentExercises = exercises
                        if (currentExercises.isNullOrEmpty()) {
                            Toast.makeText(
                                this@Summary,
                                "Error: No exercises added.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val currentWorkout = workout
                            if (currentWorkout != null) {
                                currentWorkout.name = workoutName
                                currentWorkout.exercises = currentExercises

                                addWorkoutFirebase(currentWorkout)

                                if ("add workout" == initialMainButtonText.lowercase()) {
                                    workoutViewModel.insertWorkout(currentWorkout)
                                } else {
                                    workoutViewModel.updateWorkout(currentWorkout)
                                    currentWorkout.name?.let {
                                        if (removeIncompleteWorkoutPref(this@Summary, it)) {
                                            removeIncompleteSessionPref(this@Summary, it)
                                        }
                                    }
                                }
                                setResult(RESULT_OK)
                                finish()
                            }
                        }
                    }
                }
            )
        }
    }
}
