package ca.gainzassist.activities.main.fragments

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import ca.gainzassist.activities.start_workout.StartWorkout
import ca.gainzassist.activities.main.fragments.resume.ResumeScreen
import ca.gainzassist.activities.main.fragments.resume.ResumeUiState
import ca.gainzassist.models.Workout
import ca.gainzassist.models.db.WorkoutViewModel
import ca.gainzassist.util.Preferences.getIncompleteWorkouts
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

class Resume : Fragment() {

    var intent: Intent? = null
    var extraKey: String? = null
    var workoutViewModel: WorkoutViewModel? = null
    var allWorkouts: List<Workout>? = null
    var filteredWorkouts = ArrayList<Workout>()

    private val uiStateFlow = MutableStateFlow(ResumeUiState(workoutNames = emptyList()))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        workoutViewModel = ViewModelProvider(this).get(WorkoutViewModel::class.java)

        workoutViewModel?.allWorkouts?.observe(viewLifecycleOwner, Observer { workouts ->
            allWorkouts = workouts
            updateWorkouts()
        })

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val uiState by uiStateFlow.collectAsState()
                ResumeScreen(
                    uiState = uiState,
                    onWorkoutClick = { workoutName ->
                        onWorkoutClicked(workoutName)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateWorkouts()
    }

    private fun updateWorkouts() {
        val workouts = allWorkouts ?: return
        val ctx = context ?: return
        val incomplete = getIncompleteWorkouts(ctx)
        filteredWorkouts = ArrayList()
        if (incomplete != null) {
            for (workout in workouts) {
                if (incomplete.contains(workout.name)) {
                    filteredWorkouts.add(workout)
                }
            }
        }
        val workoutNames = ArrayList<String>()
        for (workout in filteredWorkouts) {
            workout.name?.let { workoutNames.add(it) }
        }

        uiStateFlow.value = ResumeUiState(workoutNames = workoutNames)
    }

    private fun onWorkoutClicked(workoutName: String) {
        intent = Intent(context, StartWorkout::class.java)
        extraKey = ca.gainzassist.activities.main.Main.EXTRA_WORKOUT
        workoutViewModel?.getWorkoutFromName(context, workoutName)
    }

    companion object {
        @JvmStatic
        fun getInstance(): Resume {
            return Resume()
        }
    }
}
