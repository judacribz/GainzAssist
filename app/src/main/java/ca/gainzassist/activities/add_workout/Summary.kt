package ca.gainzassist.activities.add_workout

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import ca.gainzassist.R
import ca.gainzassist.constants.ExerciseConst.BB_MIN_WEIGHT
import ca.gainzassist.constants.ExerciseConst.BB_WEIGHT_CHANGE
import ca.gainzassist.constants.ExerciseConst.DB_MIN_WEIGHT
import ca.gainzassist.constants.ExerciseConst.DB_WEIGHT_CHANGE
import ca.gainzassist.constants.ExerciseConst.MIN_WEIGHT
import ca.gainzassist.constants.ExerciseConst.WEIGHT_CHANGE
import ca.gainzassist.models.Exercise
import ca.gainzassist.models.Exercise.Companion.EQUIPMENT_TYPES
import ca.gainzassist.models.Exercise.SetsType.MAIN_SET
import ca.gainzassist.models.ExerciseSet
import ca.gainzassist.models.Workout
import ca.gainzassist.models.db.WorkoutViewModel
import ca.gainzassist.util.Preferences.removeIncompleteSessionPref
import ca.gainzassist.util.Preferences.removeIncompleteWorkoutPref
import ca.gainzassist.util.UI.setInitTheme
import ca.gainzassist.util.firebase.Database.addWorkoutFirebase
import com.orhanobut.logger.Logger
import org.parceler.Parcels
import java.util.ArrayList

class Summary : AppCompatActivity() {

    enum class CALLING_ACTIVITY {
        WORKOUTS_LIST,
        EXERCISES_ENTRY
    }

    companion object {
        const val EXTRA_WORKOUT = "ca.gainzassist.activities.add_workout.EXTRA_WORKOUT"
        const val EXTRA_CALLING_ACTIVITY =
            "ca.gainzassist.activities.add_workout.EXTRA_CALLING_ACTIVITY"

        private const val MIN_INT = 1
        private const val MIN_FLOAT = 5.0f

        private const val POS_STREN = 0
        private const val POS_CARDIO = 1
        private const val POS_NA = 2
    }

    private var workoutId: Long = -1

    var workout: Workout? = null
    var exercises: ArrayList<Exercise>? = null
    val workoutViewModel by viewModels<WorkoutViewModel>()
    var ex: Exercise? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInitTheme(this)

        val sourceIntent = intent
        workout = Parcels.unwrap(sourceIntent.getParcelableExtra(EXTRA_WORKOUT))
        val currentWorkout = workout ?: return
        workoutId = currentWorkout.id

        var initialMainButtonText = getString(R.string.add_workout)
        when (sourceIntent.getSerializableExtra(EXTRA_CALLING_ACTIVITY) as? CALLING_ACTIVITY) {
            CALLING_ACTIVITY.WORKOUTS_LIST -> {
                initialMainButtonText = getString(R.string.update_workout)
            }
            else -> {
                // Keep default
            }
        }

        val initialWorkoutName = currentWorkout.name ?: ""
        exercises = currentWorkout.exercises

        setContent {
            val exercisesState = remember { mutableStateListOf<Exercise>().apply { exercises?.let { addAll(it) } } }
            var workoutName by rememberSaveable { mutableStateOf(initialWorkoutName) }
            var exerciseName by rememberSaveable { mutableStateOf("") }
            var weight by rememberSaveable { mutableStateOf(getString(R.string.starting_weight)) }
            var reps by rememberSaveable { mutableStateOf(getString(R.string.starting_reps)) }
            var sets by rememberSaveable { mutableStateOf(getString(R.string.starting_sets)) }
            var selectedEquipment by rememberSaveable { mutableStateOf("Barbell") }
            var selectedExerciseName by rememberSaveable { mutableStateOf<String?>(null) }

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
                selectedExerciseName = selectedExerciseName,
                showAddExerciseButton = selectedExerciseName == null,
                showUpdateExerciseButton = selectedExerciseName != null,
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
                    if (it.isNotEmpty() && workout?.containsExercise(it) == true) {
                        selectedExerciseName = it
                    } else {
                        selectedExerciseName = null
                    }
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
                    selectedExerciseName = null
                },
                onAddExercise = {
                    var isValid = true
                    if (exerciseName.isBlank()) { exerciseNameError = getString(R.string.err_required); isValid = false }
                    if (weight.isBlank()) { weightError = getString(R.string.err_required); isValid = false }
                    if (reps.isBlank()) { repsError = getString(R.string.err_required); isValid = false }
                    if (sets.isBlank()) { setsError = getString(R.string.err_required); isValid = false }

                    if (isValid) {
                        if (workout?.containsExercise(exerciseName) == true) {
                            exerciseNameError = getString(R.string.err_exercise_exists, exerciseName)
                        } else {
                            val newExNumber = workout?.numExercises ?: 0
                            val exercise = Exercise(
                                newExNumber, exerciseName, "Strength", selectedEquipment.lowercase(),
                                sets.toIntOrNull() ?: MIN_INT, reps.toIntOrNull() ?: MIN_INT, weight.toFloatOrNull() ?: minWeight, MAIN_SET
                            ).apply { this.workoutId = this@Summary.workoutId }

                            exercises?.add(exercise)
                            exercisesState.add(exercise)

                            exerciseName = ""
                            exerciseNameError = null
                            reps = getString(R.string.starting_reps)
                            sets = getString(R.string.starting_sets)
                            weight = getString(R.string.starting_weight)
                            selectedEquipment = "Barbell"
                            selectedExerciseName = null
                        }
                    }
                },
                onUpdateExercise = {
                    var isValid = true
                    if (exerciseName.isBlank()) { exerciseNameError = getString(R.string.err_required); isValid = false }
                    if (weight.isBlank()) { weightError = getString(R.string.err_required); isValid = false }
                    if (reps.isBlank()) { repsError = getString(R.string.err_required); isValid = false }
                    if (sets.isBlank()) { setsError = getString(R.string.err_required); isValid = false }

                    if (isValid) {
                        val currentEx = ex
                        if (currentEx != null) {
                            val num = currentEx.exerciseNumber
                            exercises?.let { exList ->
                                if (num >= 0 && num < exList.size) {
                                    val exercise = Exercise(
                                        num, exerciseName, "Strength", selectedEquipment.lowercase(),
                                        sets.toIntOrNull() ?: MIN_INT, reps.toIntOrNull() ?: MIN_INT, weight.toFloatOrNull() ?: minWeight, MAIN_SET
                                    ).apply {
                                        this.workoutId = this@Summary.workoutId
                                        this.id = currentEx.id
                                    }
                                    exList[num] = exercise
                                    exercisesState[num] = exercise
                                }
                            }
                        }

                        exerciseName = ""
                        exerciseNameError = null
                        reps = getString(R.string.starting_reps)
                        sets = getString(R.string.starting_sets)
                        weight = getString(R.string.starting_weight)
                        selectedEquipment = "Barbell"
                        selectedExerciseName = null
                    }
                },
                onExerciseClicked = { exName ->
                    ex = workout?.getExerciseFromName(exName)
                    exerciseName = exName
                    selectedExerciseName = exName
                    ex?.let {
                        sets = it.sets.toString()
                        reps = it.reps.toString()
                        weight = it.weight.toString()
                        val equipmentIndex = EQUIPMENT_TYPES.indexOf(it.equipment)
                        val equipmentOptions = resources.getStringArray(R.array.exerciseEquipment)
                        if (equipmentIndex >= 0 && equipmentIndex < equipmentOptions.size) {
                            selectedEquipment = equipmentOptions[equipmentIndex]
                        }
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
                            Toast.makeText(this@Summary, "Error: No exercises added.", Toast.LENGTH_SHORT).show()
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
