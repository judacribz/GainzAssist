package ca.judacribz.gainzassist.activities.main.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.main.Main
import ca.judacribz.gainzassist.activities.start_workout.StartWorkoutActivity
import ca.judacribz.gainzassist.adapters.SingleItemAdapter
import ca.judacribz.gainzassist.adapters.SingleItemAdapter.ItemClickObserver
import ca.judacribz.gainzassist.models.db.WorkoutViewModel
import ca.judacribz.gainzassist.util.Preferences
import ca.judacribz.gainzassist.util.UI

class Resume  // --------------------------------------------------------------------------------------------
// ######################################################################################### //
// WarmupsList Constructor/Instance                                                        //
// ######################################################################################### //
    : Fragment(), ItemClickObserver {
    // --------------------------------------------------------------------------------------------
    // Global Vars
    // --------------------------------------------------------------------------------------------
    var newView: View? = null
    var act: Main? = null
    var workoutAdapter: SingleItemAdapter? = null
    var layoutManager: LinearLayoutManager? = null
    val workoutViewModel: WorkoutViewModel by viewModels()
    var workoutNames: ArrayList<String>? = null
    var filteredWorkouts: ArrayList<String>? = null
    var intent: Intent? = null
    var extraKey: String? = null

    // UI Elements
    @BindView(R.id.rv_res_workout_btns)
    lateinit var workoutsList: RecyclerView

    // ######################################################################################### //
    // Fragment Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun onAttach(context: Context) {
        super.onAttach(context)
        act = context as Main
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (newView != null) {
            return newView
        }
        ButterKnife.bind(
            this,
            inflater.inflate(R.layout.fragment_resume, container, false).also {
                newView = it
            }
        )
        retainInstance = true

        // ExerciseSet the layout manager for the localWorkouts
        layoutManager = LinearLayoutManager(act)
        workoutsList!!.layoutManager = layoutManager
        workoutsList!!.setHasFixedSize(true)
        return newView
    }

    override fun onResume() {
        super.onResume()
        filteredWorkouts = ArrayList()
        val names = Preferences.getIncompleteWorkouts(act)
        if (names != null) {
            filteredWorkouts!!.addAll(names)
            displayWorkoutList(null)
        }
    }

    /* Helper function to display button list of workouts */
    fun displayWorkoutList(workouts: ArrayList<String>?) {
        if (workouts != null) {
            filteredWorkouts = workouts
        }
        workoutAdapter = SingleItemAdapter(
            act,
            filteredWorkouts,
            R.layout.part_button,
            R.id.btnListItem
        )
        workoutAdapter!!.setItemClickObserver(this)
        workoutsList!!.adapter = workoutAdapter
    }

    //Fragment//Override///////////////////////////////////////////////////////////////////////////
    // SingleItemAdapter.ItemClickObserver Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun onItemClick(view: View) {
        intent = Intent(act, StartWorkoutActivity::class.java)
        extraKey = Main.EXTRA_WORKOUT
        workoutViewModel!!.getWorkoutFromName(act, UI.getTextString(view as TextView))
    }

    override fun onItemLongClick(view: View) {} //SingleItemAdapter.ItemClickObserver//Override////////////////////////////////////////////////

    // Click Handling
    // ============================================================================================
    //=Click=Handling==============================================================================
    companion object {
        val instance = Resume()
    }
}
