package ca.gainzassist.activities.start_workout.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import ca.gainzassist.activities.start_workout.fragments.exercises.ExerciseSetsListScreen
import ca.gainzassist.activities.start_workout.fragments.exercises.ExerciseSetsListUiState
import ca.gainzassist.adapters.WorkoutPagerAdapter.Companion.EXTRA_MAIN_EXERCISES
import ca.gainzassist.models.Exercise
import ca.gainzassist.ui.theme.GainzAssistTheme
import org.parceler.Parcels
import java.util.*

class ExercisesList : Fragment() {

    private var exercises: ArrayList<Exercise>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.let {
            exercises = Parcels.unwrap(it.getParcelable(EXTRA_MAIN_EXERCISES))
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                GainzAssistTheme {
                    ExerciseSetsListScreen(
                        uiState = ExerciseSetsListUiState(
                            exercises = exercises ?: emptyList()
                        )
                    )
                }
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
