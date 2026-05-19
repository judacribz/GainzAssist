package ca.judacribz.gainzassist.activities.start_workout.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout
import ca.judacribz.gainzassist.adapters.WorkoutPagerAdapter.Companion.EXTRA_WARMUPS
import ca.judacribz.gainzassist.databinding.FragmentWarmupsListBinding
import ca.judacribz.gainzassist.models.Exercise
import ca.judacribz.gainzassist.models.Exercise.SetsType.WARMUP_SET
import org.parceler.Parcels
import java.util.*

class WarmupsList : Fragment() {

    private var warmups: ArrayList<Exercise>? = null

    private lateinit var binding: FragmentWarmupsListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWarmupsListBinding.inflate(inflater, container, false)
        val args = arguments
        if (args != null) {
            warmups = Parcels.unwrap(args.getParcelable(EXTRA_WARMUPS))
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (binding.llExerciseSetsInsert.childCount > 0) {
            binding.llExerciseSetsInsert.removeAllViews()
        }
        if (warmups != null) {
            for (warmup in warmups!!) {
                (activity as StartWorkout?)?.displaySets(WARMUP_SET, warmup, binding.llExerciseSetsInsert)
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
