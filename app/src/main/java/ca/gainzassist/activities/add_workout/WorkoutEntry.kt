package ca.gainzassist.activities.add_workout

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import ca.gainzassist.R
import ca.gainzassist.constants.ExerciseConst.MIN_INT
import ca.gainzassist.util.UI.setInitTheme
import kotlin.math.max

class WorkoutEntry : AppCompatActivity() {

    companion object {
        const val EXTRA_WORKOUT_NAME = "ca.gainzassist.activities.add_workout.EXTRA_WORKOUT"
        const val EXTRA_NUM_EXERCISES = "ca.gainzassist.activities.add_workout.EXTRA_NUM_EXERCISES"
    }

    private val exercisesEntryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInitTheme(this)

        setContent {
            var workoutName by rememberSaveable { mutableStateOf("") }
            var numExercises by rememberSaveable {
                mutableIntStateOf(getString(R.string.initial_num_exercises).toInt())
            }

            WorkoutEntryScreen(
                uiState = WorkoutEntryUiState(
                    workoutName = workoutName,
                    numExercises = numExercises
                ),
                onWorkoutNameChanged = { workoutName = it },
                onNumExercisesChanged = { numExercises = it },
                onIncrementExercises = { numExercises++ },
                onDecrementExercises = {
                    if (numExercises > MIN_INT) numExercises--
                },
                onCancel = { finish() },
                onEnter = { enterWorkoutName(workoutName, numExercises) },
                onBack = { finish() }
            )
        }
    }

    private fun enterWorkoutName(workoutName: String, numExercises: Int) {
        if (numExercises >= MIN_INT) {
            val exercisesEntry = Intent(this, ExercisesEntry::class.java)

            if (workoutName.trim().isNotEmpty()) {
                Toast.makeText(this, workoutName, Toast.LENGTH_SHORT).show()
                exercisesEntry.putExtra(EXTRA_WORKOUT_NAME, workoutName)
            }

            exercisesEntry.putExtra(EXTRA_NUM_EXERCISES, max(MIN_INT, numExercises))
            exercisesEntryLauncher.launch(exercisesEntry)
        }
    }
}
