package ca.judacribz.gainzassist.activities.main.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.add_workout.Summary
import ca.judacribz.gainzassist.activities.main.Main
import ca.judacribz.gainzassist.activities.start_workout.StartWorkoutActivity
import ca.judacribz.gainzassist.adapters.SingleItemAdapter
import ca.judacribz.gainzassist.models.Workout
import ca.judacribz.gainzassist.models.db.WorkoutViewModel
import ca.judacribz.gainzassist.util.UI
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.OnItemClickListener
import com.orhanobut.dialogplus.ViewHolder
import java.util.Locale

// --------------------------------------------------------------------------------------------
// ######################################################################################### //
// WarmupsList Constructor/Instance                                                          //
// ######################################################################################### //
class Workouts : Fragment(), SingleItemAdapter.ItemClickObserver {
    // --------------------------------------------------------------------------------------------
    // Global Vars
    // --------------------------------------------------------------------------------------------
    var act: Main? = null
    var newView: View? = null
    var workoutAdapter: SingleItemAdapter? = null
    var layoutManager: LinearLayoutManager? = null
    var dialog: DialogPlus? = null
    val workoutViewModel: WorkoutViewModel by viewModels()
    lateinit var workObs: Observer<List<Workout>>
    var workLiv: LiveData<List<Workout>>? = null
    var workoutNames: ArrayList<String>? = null
    var filteredWorkouts: ArrayList<String>? = null
    var intent: Intent? = null
    var extraKey: String? = null

    // UI Elements
    @BindView(R.id.rv_workout_btns)
    lateinit var workoutsList: RecyclerView

    // ######################################################################################### //
    // Fragment Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun onAttach(context: Context) {
        super.onAttach(context)
        act = context as Main
        dialog = DialogPlus.newDialog(act)
            .setOnItemClickListener(OnItemClickListener { dialog, item, view, position -> })
            .setContentHolder(ViewHolder(R.layout.dialog_workout))
            .setContentBackgroundResource(R.drawable.edit_text_box_blue)
            .setExpanded(true)
            .setGravity(Gravity.CENTER)
            .setCancelable(true)
            .create()
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
            inflater.inflate(R.layout.fragment_workouts, container, false).also {
                newView = it
            }
        )
        retainInstance = true
        workoutNames = ArrayList()
        layoutManager = LinearLayoutManager(act)
        workoutsList.setLayoutManager(layoutManager)
        workoutsList.setHasFixedSize(true)
        workObs = Observer { workouts ->
                if (workouts != null) {
                    workoutNames = ArrayList()
                    for (workout in workouts) {
                        workout.name?.let { workoutNames!!.add(it) }
                    }
                    if (workoutAdapter == null) {
                        displayWorkoutList(workoutNames)
                    } else {
                        //                        workoutAdapter.setItems(workoutNames);
                        //                        workoutAdapter.notifyDataSetChanged();
                        displayWorkoutList(workoutNames)
                    }
                }
            }
        workLiv = workoutViewModel.allWorkouts
        return newView
    }

    override fun onResume() {
        super.onResume()
        workLiv?.observe(requireActivity(), workObs)
    }

    override fun onStop() {
        super.onStop()
        workLiv?.removeObserver(workObs)
    }

    /* Helper function to display button list of workouts */
    fun displayWorkoutList( workouts: ArrayList<String>?) {
        if (workouts != null) {
            filteredWorkouts = workouts
        }
        workoutAdapter = SingleItemAdapter(
            act,
            filteredWorkouts,
            R.layout.part_button,
            R.id.btnListItem
        )
        workoutAdapter?.setItemClickObserver(this)
        workoutsList.setAdapter(workoutAdapter)
    }

    //Fragment//Override///////////////////////////////////////////////////////////////////////////
    // SingleItemAdapter.ItemClickObserver Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun onItemClick(view: View) {
        intent = Intent(act, StartWorkoutActivity::class.java)
        extraKey = Main.EXTRA_WORKOUT
        workoutViewModel.getWorkoutFromName(act, UI.getTextString(view as TextView))
    }

    var workoutName: String? = null
    override fun onItemLongClick(view: View) {
        workoutName = UI.getTextString(view as TextView)
        (dialog?.findViewById(R.id.tv_workout_name) as TextView).setText(workoutName)
        dialog?.findViewById(R.id.btn_edit_workout)?.setOnClickListener { editWorkout(workoutName) }
        dialog?.findViewById(R.id.btn_delete_workout)?.setOnClickListener {
            deleteWorkout(
                workoutName
            )
        }
        dialog?.show()
    }

    private fun editWorkout(workoutName: String?) {
        val newWorkoutSummaryIntent = Intent(
            act,
            Summary::class.java
        )
        newWorkoutSummaryIntent.putExtra(
            Summary.EXTRA_CALLING_ACTIVITY,
            Summary.CALLING_ACTIVITY.WORKOUTS_LIST
        )
        extraKey = Summary.EXTRA_WORKOUT
        intent = newWorkoutSummaryIntent
        workoutViewModel.getWorkoutFromName(act, workoutName)
        dialog?.dismiss()
    }

    private fun deleteWorkout(workoutName: String?) {
        workoutViewModel.deleteWorkout(workoutName)
        dialog?.dismiss()
    }

    //SingleItemAdapter.ItemClickObserver//Override////////////////////////////////////////////////
    fun onQueryTextChange(newText: String) {
        val filterWord = newText.lowercase(Locale.getDefault())
        filteredWorkouts = ArrayList()
        for (workoutName in workoutNames!!) {
            if (workoutName.lowercase(Locale.getDefault()).contains(filterWord)) {
                filteredWorkouts!!.add(workoutName)
            }
        }
        workoutAdapter?.setItems(filteredWorkouts)
        workoutAdapter?.notifyDataSetChanged()
    } // Click Handling

    // ============================================================================================
    //=Click=Handling==============================================================================
    companion object {
        val instance = Workouts()
    }
}
