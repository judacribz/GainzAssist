package ca.judacribz.gainzassist.activities.start_workout.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import butterknife.BindView
import butterknife.ButterKnife
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout
import ca.judacribz.gainzassist.activities.start_workout.fragments.ExercisesList
import ca.judacribz.gainzassist.adapters.WorkoutPagerAdapter.Companion.EXTRA_MAIN_EXERCISES
import ca.judacribz.gainzassist.models.Exercise
import ca.judacribz.gainzassist.models.Exercise.SetsType.MAIN_SET
import org.parceler.Parcels
import java.util.*

class ExercisesList : Fragment() {

    private var exercises: ArrayList<Exercise>? = null

    @BindView(R.id.ll_exercise_sets_insert)
    lateinit var llExSetsInsert: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exercises_list, container, false)
        ButterKnife.bind(this, view)
        exercises = Parcels.unwrap(arguments!!.getParcelable(EXTRA_MAIN_EXERCISES))
        return view
    }

    override fun onResume() {
        super.onResume()
        if (llExSetsInsert.childCount > 0) {
            llExSetsInsert.removeAllViews()
        }
        for (exercise in exercises!!) {
            (activity as StartWorkout?)?.displaySets(MAIN_SET, exercise, llExSetsInsert)
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(): ExercisesList {
            return ExercisesList()
        }
    }
}
