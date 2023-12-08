package ca.judacribz.gainzassist.activities.start_workout.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.start_workout.StartWorkoutActivity
import ca.judacribz.gainzassist.adapters.WorkoutPagerAdapter
import ca.judacribz.gainzassist.models.Exercise
import ca.judacribz.gainzassist.models.Exercise.SetsType

// --------------------------------------------------------------------------------------------
// ######################################################################################### //
// ExercisesList Constructor/Instance                                                        //
// ######################################################################################### //
class ExercisesList : Fragment() {
    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------
    // Global Vars
    // --------------------------------------------------------------------------------------------

    var bundle: Bundle? = null

    //    @BindView(R.id.ll_exercise_subtitle_insert) LinearLayout llExSubInsert;
    @BindView(R.id.ll_exercise_sets_insert)
    lateinit var llExSetsInsert: LinearLayout

    // ######################################################################################### //
    // Fragment Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun onAttach(context: Context) {
        super.onAttach(context)
         arguments?.let { bundle }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       val view = inflater.inflate(R.layout.fragment_exercises_list, container, false)
        ButterKnife.bind(this, view)
        bundle?.let {
            val exercises = it.getParcelableArrayList<Exercise>(WorkoutPagerAdapter.EXTRA_MAIN_EXERCISES)
            if (exercises != null) {
                for (i in exercises.indices.reversed()) {
                    (activity as? StartWorkoutActivity)?.displaySets(
                        SetsType.MAIN_SET,
                        exercises[i],  //                            llExSubInsert,
                        llExSetsInsert
                    )
                }
            }
        }
        return view
    } //Fragment//Override///////////////////////////////////////////////////////////////////////////

    companion object {
        val instance: ExercisesList
            get() = ExercisesList()
    }
}
