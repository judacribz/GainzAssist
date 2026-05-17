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
import butterknife.BindView
import butterknife.ButterKnife
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout
import ca.judacribz.gainzassist.adapters.SingleItemAdapter
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

    @BindView(R.id.rv_res_workout_btns)
    lateinit var workoutsList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_resume, container, false)
        ButterKnife.bind(this, view)
        workoutViewModel = ViewModelProviders.of(this).get(WorkoutViewModel::class.java)

        workoutsList.layoutManager = LinearLayoutManager(context)
        workoutsList.setHasFixedSize(true)

        workoutViewModel!!.allWorkouts.observe(this, Observer { workouts ->
            allWorkouts = workouts
            updateWorkouts()
        })

        return view
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
        workoutsList.adapter = adapter
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
