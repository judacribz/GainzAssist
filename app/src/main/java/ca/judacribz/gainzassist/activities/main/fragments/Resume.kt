package ca.judacribz.gainzassist.activities.main.fragments

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout
import ca.judacribz.gainzassist.adapters.SingleItemAdapter
import ca.judacribz.gainzassist.databinding.FragmentResumeBinding
import ca.judacribz.gainzassist.models.Workout
import ca.judacribz.gainzassist.models.db.WorkoutViewModel
import ca.judacribz.gainzassist.util.Preferences.getIncompleteWorkouts
import ca.judacribz.gainzassist.util.UI.getTextString
import java.util.*

class Resume : Fragment(), SingleItemAdapter.ItemClickObserver {

    var intent: Intent? = null
    var extraKey: String? = null
    var workoutViewModel: WorkoutViewModel? = null
    var adapter: SingleItemAdapter? = null
    var allWorkouts: List<Workout>? = null
    var filteredWorkouts = ArrayList<Workout>()

    private lateinit var binding: FragmentResumeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentResumeBinding.inflate(inflater, container, false)
        workoutViewModel = ViewModelProvider(this).get(WorkoutViewModel::class.java)

        binding.rvResWorkoutBtns.layoutManager = LinearLayoutManager(context)
        binding.rvResWorkoutBtns.setHasFixedSize(true)

        workoutViewModel?.allWorkouts?.observe(viewLifecycleOwner, Observer { workouts ->
            allWorkouts = workouts
            updateWorkouts()
        })

        return binding.root
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
        adapter = SingleItemAdapter(
            ctx,
            workoutNames,
            R.layout.part_button,
            R.id.btnListItem
        )
        adapter?.setItemClickObserver(this)
        binding.rvResWorkoutBtns.adapter = adapter
    }

    override fun onItemClick(view: View?) {
        intent = Intent(context, StartWorkout::class.java)
        extraKey = ca.judacribz.gainzassist.activities.main.Main.EXTRA_WORKOUT
        view?.let {
            workoutViewModel?.getWorkoutFromName(context, getTextString(it as TextView))
        }
    }

    override fun onItemLongClick(view: View?) {}

    companion object {
        @JvmStatic
        fun getInstance(): Resume {
            return Resume()
        }
    }
}
