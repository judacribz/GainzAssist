package ca.judacribz.gainzassist.activities.main.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.add_workout.Summary
import ca.judacribz.gainzassist.activities.add_workout.Summary.CALLING_ACTIVITY.WORKOUTS_LIST
import ca.judacribz.gainzassist.activities.add_workout.Summary.Companion.EXTRA_CALLING_ACTIVITY
import ca.judacribz.gainzassist.activities.main.Main
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout
import ca.judacribz.gainzassist.adapters.SingleItemAdapter
import ca.judacribz.gainzassist.models.Workout
import ca.judacribz.gainzassist.models.db.WorkoutViewModel
import ca.judacribz.gainzassist.util.UI.getTextString
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import java.util.*

class Workouts : Fragment(), SingleItemAdapter.ItemClickObserver, MaterialSearchView.OnQueryTextListener {

    var intent: Intent? = null
    var extraKey: String? = null
    var workoutViewModel: WorkoutViewModel? = null
    var adapter: SingleItemAdapter? = null
    var allWorkouts: List<Workout>? = null
    var filteredWorkouts: List<Workout>? = null
    var workoutNames: ArrayList<String>? = null

    private lateinit var act: Main
    private var dialog: DialogPlus? = null
    private var workoutName: String? = null

    @BindView(R.id.rv_workout_btns)
    lateinit var workoutsList: RecyclerView

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        act = context as Main

        dialog = DialogPlus.newDialog(act)
            .setOnItemClickListener { _, _, _, _ -> }
            .setContentHolder(ViewHolder(R.layout.dialog_workout))
            .setContentBackgroundResource(R.drawable.edit_text_box_blue)
            .setExpanded(true)
            .setGravity(Gravity.CENTER)
            .setCancelable(true)
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_workouts, container, false)
        ButterKnife.bind(this, view)
        workoutViewModel = ViewModelProviders.of(act).get(WorkoutViewModel::class.java)

        workoutsList.layoutManager = LinearLayoutManager(act)
        workoutsList.setHasFixedSize(true)

        workoutViewModel!!.allWorkouts.observe(this, Observer { workouts ->
            allWorkouts = workouts
            filteredWorkouts = workouts
            workoutNames = ArrayList()
            if (workouts != null) {
                for (workout in workouts) {
                    workoutNames!!.add(workout.name!!)
                }
            }
            updateWorkouts()
        })

        return view
    }

    private fun updateWorkouts() {
        if (workoutNames == null) return
        adapter = SingleItemAdapter(
            act,
            workoutNames,
            R.layout.part_button,
            R.id.btnListItem
        )
        adapter!!.setItemClickObserver(this)
        workoutsList.adapter = adapter
    }

    override fun onItemClick(view: View?) {
        intent = Intent(act, StartWorkout::class.java)
        extraKey = ca.judacribz.gainzassist.activities.main.Main.EXTRA_WORKOUT
        workoutViewModel!!.getWorkoutFromName(act, getTextString(view as TextView))
    }

    override fun onItemLongClick(view: View?) {
        workoutName = getTextString(view as TextView)

        (dialog!!.findViewById(R.id.tv_workout_name) as TextView).text = workoutName

        dialog!!.findViewById(R.id.btn_edit_workout).setOnClickListener {
            editWorkout(workoutName!!)
        }

        dialog!!.findViewById(R.id.btn_delete_workout).setOnClickListener {
            deleteWorkout(workoutName!!)
        }

        dialog!!.show()
    }

    private fun editWorkout(workoutName: String) {
        val newWorkoutSummaryIntent = Intent(
            act,
            Summary::class.java
        )

        newWorkoutSummaryIntent.putExtra(EXTRA_CALLING_ACTIVITY, WORKOUTS_LIST)

        extraKey = Summary.EXTRA_WORKOUT
        intent = newWorkoutSummaryIntent

        workoutViewModel!!.getWorkoutFromName(act, workoutName)

        dialog!!.dismiss()
    }

    private fun deleteWorkout(workoutName: String) {
        workoutViewModel!!.deleteWorkout(workoutName)
        dialog!!.dismiss()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (allWorkouts == null) return false
        val query = newText?.lowercase() ?: ""
        filteredWorkouts = allWorkouts!!.filter { it.name!!.lowercase().contains(query) }
        workoutNames = ArrayList()
        for (workout in filteredWorkouts!!) {
            workoutNames!!.add(workout.name!!)
        }
        updateWorkouts()
        return true
    }

    companion object {
        @JvmStatic
        fun getInstance(): Workouts {
            return Workouts()
        }
    }
}
