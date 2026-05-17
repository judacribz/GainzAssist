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
import ca.judacribz.gainzassist.adapters.WorkoutPagerAdapter.Companion.EXTRA_WARMUPS
import ca.judacribz.gainzassist.models.Exercise
import ca.judacribz.gainzassist.models.Exercise.SetsType.WARMUP_SET
import org.parceler.Parcels
import java.util.*

class WarmupsList : Fragment() {

    private var warmups: ArrayList<Exercise>? = null

    @BindView(R.id.ll_exercise_sets_insert)
    lateinit var llExSetsInsert: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_warmups_list, container, false)
        ButterKnife.bind(this, view)
        val args = arguments
        if (args != null) {
            warmups = Parcels.unwrap(args.getParcelable(EXTRA_WARMUPS))
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        if (llExSetsInsert.childCount > 0) {
            llExSetsInsert.removeAllViews()
        }
        if (warmups != null) {
            for (warmup in warmups!!) {
                (activity as StartWorkout?)?.displaySets(WARMUP_SET, warmup, llExSetsInsert)
            }
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(): WarmupsList {
            return WarmupsList()
        }
    }
}
