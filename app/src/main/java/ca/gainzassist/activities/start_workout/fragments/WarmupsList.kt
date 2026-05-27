package ca.gainzassist.activities.start_workout.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import ca.gainzassist.adapters.WorkoutPagerAdapter.Companion.EXTRA_WARMUPS
import ca.gainzassist.models.Exercise
import ca.gainzassist.ui.theme.GainzAssistTheme
import org.parceler.Parcels
import java.util.*

class WarmupsList : Fragment() {

    private var warmups: ArrayList<Exercise>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val args = arguments
        if (args != null) {
            warmups = Parcels.unwrap(args.getParcelable(EXTRA_WARMUPS))
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                GainzAssistTheme {
                    WarmupsListScreen(warmups = warmups)
                }
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
