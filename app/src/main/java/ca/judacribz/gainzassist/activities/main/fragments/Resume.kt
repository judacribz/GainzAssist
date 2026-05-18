package ca.judacribz.gainzassist.activities.main.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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

    private var _binding: FragmentResumeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResumeBinding.inflate(inflater, container, false)
        workoutViewModel = ViewModelProviders.of(this).get(WorkoutViewModel::class.java)

        binding.rvResWorkoutBtns.layoutManager = LinearLayoutManager(context)
        binding.rvResWorkoutBtns.setHasFixedSize(true)

        workoutViewModel!!.allWorkouts.observe(this, Observer { workouts ->
            allWorkouts = workouts
            updateWorkouts()
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        updateWorkouts()
    }

    private fun updateWorkouts() {
        if (allWorkouts == null) return
        val incomplete = getIncompleteWorkouts(context!!)
        filteredWorkouts = ArrayList()
        if (incomplete != null) {
            for (workout in allWorkouts!!) {
                if (incomplete.contains(workout.name)) {
                    filteredWorkouts.add(workout)
                }
            }
        }
        val workoutNames = ArrayList<String>()
        for (workout in filteredWorkouts) {
            workoutNames.add(workout.name!!)
        }
        adapter = SingleItemAdapter(
            context,
            workoutNames,
            R.layout.part_button,
            R.id.btnListItem
        )
        adapter!!.setItemClickObserver(this)
        binding.rvResWorkoutBtns.adapter = adapter
    }

    override fun onItemClick(view: View?) {
        intent = Intent(context, StartWorkout::class.java)
        extraKey = ca.judacribz.gainzassist.activities.main.Main.EXTRA_WORKOUT
        workoutViewModel!!.getWorkoutFromName(context, getTextString(view as TextView))
    }

    override fun onItemLongClick(view: View?) {}

    companion object {
        @JvmStatic
        fun getInstance(): Resume {
            return Resume()
        }
    }
}
