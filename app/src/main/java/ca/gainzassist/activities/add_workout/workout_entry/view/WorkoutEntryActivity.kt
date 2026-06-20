package ca.gainzassist.activities.add_workout.workout_entry.view

import android.content.Intent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.gainzassist.activities.add_workout.exercises_entry.view.ExercisesEntryActivity
import ca.gainzassist.activities.add_workout.workout_entry.WorkoutEntryViewModel
import ca.gainzassist.activities.add_workout.workout_entry.WorkoutEntryViewModelEvent
import ca.gainzassist.activities.base.GainzBaseActivity
import ca.gainzassist.core.constants.ExerciseConst
import org.koin.androidx.compose.koinViewModel
import kotlin.math.max

class WorkoutEntryActivity : GainzBaseActivity() {

    private val exercisesEntryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            finish()
        }
    }

    @Composable
    override fun InnerContent() {
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
            actions = WorkoutEntryScreenActions(
                onWorkoutNameChanged = viewModel::onWorkoutNameChanged,
                onNumberOfExercisesChanged = viewModel::onNumberOfExercisesChanged,
                onIncrementExercises = {
                    val current = state.numberOfExercises.toIntOrNull() ?: ExerciseConst.MIN_INT
                    viewModel.onNumberOfExercisesChanged((current + 1).toString())
                },
                onDecrementExercises = {
                    val current = state.numberOfExercises.toIntOrNull() ?: ExerciseConst.MIN_INT
                    if (current > ExerciseConst.MIN_INT) {
                        viewModel.onNumberOfExercisesChanged((current - 1).toString())
                    }
                },
                onCancel = { finish() },
                onContinueClicked = viewModel::onContinueClicked,
                onBack = { finish() }
            )
        )
    }

    private fun enterWorkoutName(workoutName: String, numExercises: Int) {
        if (numExercises >= ExerciseConst.MIN_INT) {
            val exercisesEntry = Intent(this, ExercisesEntryActivity::class.java)
            if (workoutName.trim().isNotEmpty()) {
                Toast.makeText(this, workoutName, Toast.LENGTH_SHORT).show()
                exercisesEntry.putExtra(EXTRA_WORKOUT_NAME, workoutName)
            }
            exercisesEntry.putExtra(EXTRA_NUM_EXERCISES, max(ExerciseConst.MIN_INT, numExercises))
            exercisesEntryLauncher.launch(exercisesEntry)
        }
    }

    companion object {
        const val EXTRA_WORKOUT_NAME = "ca.gainzassist.activities.add_workout.EXTRA_WORKOUT"
        const val EXTRA_NUM_EXERCISES = "ca.gainzassist.activities.add_workout.EXTRA_NUM_EXERCISES"
    }
}