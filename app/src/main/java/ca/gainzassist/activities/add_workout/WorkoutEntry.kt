package ca.gainzassist.activities.add_workout

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.gainzassist.constants.ExerciseConst.MIN_INT
import ca.gainzassist.presentation.add_workout.WorkoutEntryViewModel
import ca.gainzassist.presentation.add_workout.WorkoutEntryViewModelEvent
import ca.gainzassist.util.UI.setInitTheme
import org.koin.androidx.compose.koinViewModel
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
            val viewModel: WorkoutEntryViewModel = koinViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(viewModel.events) {
                viewModel.events.collect { event ->
                    when (event) {
                        is WorkoutEntryViewModelEvent.ContinueToExercises -> {
                            enterWorkoutName(event.workoutName, event.numberOfExercises)
                        }
                    }
                }
            }

            WorkoutEntryScreen(
                workoutName = state.workoutName,
                numberOfExercises = state.numberOfExercises,
                workoutNameError = state.workoutNameError,
                numberOfExercisesError = state.numberOfExercisesError,
                onWorkoutNameChanged = viewModel::onWorkoutNameChanged,
                onNumberOfExercisesChanged = viewModel::onNumberOfExercisesChanged,
                onIncrementExercises = {
                    val current = state.numberOfExercises.toIntOrNull() ?: MIN_INT
                    viewModel.onNumberOfExercisesChanged((current + 1).toString())
                },
                onDecrementExercises = {
                    val current = state.numberOfExercises.toIntOrNull() ?: MIN_INT
                    if (current > MIN_INT) {
                        viewModel.onNumberOfExercisesChanged((current - 1).toString())
                    }
                },
                onCancel = { finish() },
                onContinueClicked = viewModel::onContinueClicked,
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
