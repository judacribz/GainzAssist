package ca.gainzassist.activities.start_workout.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.gainzassist.activities.start_workout.StartWorkout
import ca.gainzassist.adapters.WorkoutPagerAdapter.Companion.EXTRA_WARMUPS
import ca.gainzassist.databinding.FragmentWarmupsListBinding
import ca.gainzassist.models.Exercise
import ca.gainzassist.models.Exercise.SetsType.WARMUP_SET
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
        warmups?.let {
            for (warmup in it) {
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
