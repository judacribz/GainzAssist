package ca.judacribz.gainzassist.activities.start_workout.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout
import ca.judacribz.gainzassist.adapters.WorkoutPagerAdapter.Companion.EXTRA_MAIN_EXERCISES
import ca.judacribz.gainzassist.databinding.FragmentExercisesListBinding
import ca.judacribz.gainzassist.models.Exercise
import ca.judacribz.gainzassist.models.Exercise.SetsType.MAIN_SET
import org.parceler.Parcels
import java.util.*

class ExercisesList : Fragment() {

    private var exercises: ArrayList<Exercise>? = null

    private var _binding: FragmentExercisesListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExercisesListBinding.inflate(inflater, container, false)
        exercises = Parcels.unwrap(arguments!!.getParcelable(EXTRA_MAIN_EXERCISES))
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        if (binding.llExerciseSetsInsert.childCount > 0) {
            binding.llExerciseSetsInsert.removeAllViews()
        }
        for (exercise in exercises!!) {
            (activity as StartWorkout?)?.displaySets(MAIN_SET, exercise, binding.llExerciseSetsInsert)
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(): ExercisesList {
            return ExercisesList()
        }
    }
}
