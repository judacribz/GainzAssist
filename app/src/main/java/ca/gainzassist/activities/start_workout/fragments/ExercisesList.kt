package ca.gainzassist.activities.start_workout.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.gainzassist.activities.start_workout.StartWorkout
import ca.gainzassist.adapters.WorkoutPagerAdapter.Companion.EXTRA_MAIN_EXERCISES
import ca.gainzassist.databinding.FragmentExercisesListBinding
import ca.gainzassist.models.Exercise
import ca.gainzassist.models.Exercise.SetsType.MAIN_SET
import org.parceler.Parcels
import java.util.*

class ExercisesList : Fragment() {

    private var exercises: ArrayList<Exercise>? = null

    private lateinit var binding: FragmentExercisesListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExercisesListBinding.inflate(inflater, container, false)
        arguments?.let {
            exercises = Parcels.unwrap(it.getParcelable(EXTRA_MAIN_EXERCISES))
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (binding.llExerciseSetsInsert.childCount > 0) {
            binding.llExerciseSetsInsert.removeAllViews()
        }
        exercises?.let {
            for (exercise in it) {
                (activity as StartWorkout?)?.displaySets(MAIN_SET, exercise, binding.llExerciseSetsInsert)
            }
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(): ExercisesList {
            return ExercisesList()
        }
    }
}
