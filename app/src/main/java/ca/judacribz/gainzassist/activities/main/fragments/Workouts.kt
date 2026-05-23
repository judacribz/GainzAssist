package ca.judacribz.gainzassist.activities.main.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ca.judacribz.gainzassist.activities.add_workout.Summary
import ca.judacribz.gainzassist.activities.add_workout.Summary.CALLING_ACTIVITY.WORKOUTS_LIST
import ca.judacribz.gainzassist.activities.add_workout.Summary.Companion.EXTRA_CALLING_ACTIVITY
import ca.judacribz.gainzassist.activities.main.Main
import ca.judacribz.gainzassist.activities.main.fragments.workouts.WorkoutsScreen
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout
import ca.judacribz.gainzassist.models.Workout
import ca.judacribz.gainzassist.models.db.WorkoutViewModel
import java.util.*

class Workouts : Fragment(), SearchView.OnQueryTextListener {

    var intent: Intent? = null
    var extraKey: String? = null
    var workoutViewModel: WorkoutViewModel? = null

    private var allWorkouts: List<Workout>? = null
    private var filteredWorkouts: List<Workout>? = null
    private var currentQuery: String = ""

    // Compose states
    private var workoutNames by mutableStateOf<List<String>>(emptyList())
    private var selectedWorkoutName by mutableStateOf<String?>(null)

    private lateinit var act: Main

    override fun onAttach(context: Context) {
        super.onAttach(context)
        act = context as Main
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        workoutViewModel = ViewModelProvider(act).get(WorkoutViewModel::class.java)

        workoutViewModel?.allWorkouts?.observe(viewLifecycleOwner, Observer { workouts ->
            allWorkouts = workouts
            applyFilter()
        })

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                WorkoutsScreen(
                    workoutNames = workoutNames,
                    selectedWorkoutName = selectedWorkoutName,
                    onWorkoutClick = { workoutName ->
                        onItemClick(workoutName)
                    },
                    onWorkoutLongClick = { workoutName ->
                        selectedWorkoutName = workoutName
                    },
                    onDismissDialog = {
                        selectedWorkoutName = null
                    },
                    onEditWorkout = { workoutName ->
                        editWorkout(workoutName)
                    },
                    onDeleteWorkout = { workoutName ->
                        deleteWorkout(workoutName)
                    }
                )
            }
        }
    }

    private fun onItemClick(workoutName: String) {
        intent = Intent(act, StartWorkout::class.java)
        extraKey = ca.judacribz.gainzassist.activities.main.Main.EXTRA_WORKOUT
        workoutViewModel?.getWorkoutFromName(act, workoutName)
    }

    private fun editWorkout(workoutName: String) {
        val newWorkoutSummaryIntent = Intent(
            act,
            Summary::class.java
        )

        newWorkoutSummaryIntent.putExtra(EXTRA_CALLING_ACTIVITY, WORKOUTS_LIST)

        extraKey = Summary.EXTRA_WORKOUT
        intent = newWorkoutSummaryIntent

        workoutViewModel?.getWorkoutFromName(act, workoutName)

        selectedWorkoutName = null
    }

    private fun deleteWorkout(workoutName: String) {
        workoutViewModel?.deleteWorkout(workoutName)
        selectedWorkoutName = null
    }

    private fun applyFilter() {
        val workouts = allWorkouts ?: return
        val query = currentQuery.lowercase()
        filteredWorkouts = workouts.filter { it.name.orEmpty().lowercase().contains(query) }

        val names = ArrayList<String>()
        filteredWorkouts?.let { filtered ->
            for (workout in filtered) {
                workout.name?.let { names.add(it) }
            }
        }
        workoutNames = names
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        currentQuery = newText ?: ""
        applyFilter()
        return true
    }

    companion object {
        @JvmStatic
        fun getInstance(): Workouts {
            return Workouts()
        }
    }
}
