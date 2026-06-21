package ca.gainzassist.feature.exercises_entry.presentation.screen

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ca.gainzassist.R
import ca.gainzassist.activities.base.GainzBaseActivity
import ca.gainzassist.core.constants.ExerciseConst.MIN_INT
import ca.gainzassist.feature.exercises_entry.presentation.viewmodel.ExercisesEntryViewModel
import ca.gainzassist.feature.exercises_entry.presentation.viewmodel.ExercisesEntryViewModelEvent
import ca.gainzassist.feature.summary.presentation.screen.CallingActivity
import ca.gainzassist.feature.summary.presentation.screen.SummaryActivity
import ca.gainzassist.feature.summary.presentation.screen.SummaryActivity.Companion.EXTRA_CALLING_ACTIVITY
import ca.gainzassist.feature.summary.presentation.screen.SummaryActivity.Companion.EXTRA_WORKOUT
import ca.gainzassist.feature.workout_entry.presentation.screen.WorkoutEntryActivity
import ca.gainzassist.ui.components.GainzTopBar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ExercisesEntryActivity : GainzBaseActivity() {

    private val viewModel: ExercisesEntryViewModel by viewModel()
    private val fragments = mutableMapOf<Int, ExerciseEntryFragment>()
    private val summaryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onBeforeSetContent() {
        val workoutEntryIntent = intent
        val workoutName = workoutEntryIntent.getStringExtra(
            /* name = */ WorkoutEntryActivity.EXTRA_WORKOUT_NAME
        ).orEmpty()
        val numExs = workoutEntryIntent.getIntExtra(
            /* name = */ WorkoutEntryActivity.EXTRA_NUM_EXERCISES,
            /* defaultValue = */ MIN_INT
        )

        viewModel.initialize(
            workoutName = workoutName,
            numberOfExercises = numExs,
            defaultReps = getString(R.string.starting_reps),
            defaultSets = getString(R.string.starting_sets),
            defaultWeight = getString(R.string.starting_weight),
            defaultEquipment = resources.getStringArray(R.array.exerciseEquipment).first()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is ExercisesEntryViewModelEvent.GoToSummary -> {
                            val newWorkoutSummaryIntent = Intent(
                                /* packageContext = */ this@ExercisesEntryActivity,
                                /* cls = */ SummaryActivity::class.java
                            )
                            newWorkoutSummaryIntent.putExtra(EXTRA_WORKOUT, event.workout)
                            newWorkoutSummaryIntent.putExtra(
                                EXTRA_CALLING_ACTIVITY,
                                CallingActivity.EXERCISES_ENTRY
                            )
                            summaryLauncher.launch(newWorkoutSummaryIntent)
                        }

                        is ExercisesEntryViewModelEvent.ExerciseDeleted -> {
                            // Clear cached fragments so they are recreated with proper arguments
                            // next time they are requested
                            fragments.clear()
                        }
                    }
                }
            }
        }
    }

    @Composable
    override fun InnerContent() {
        val state by viewModel.state.collectAsStateWithLifecycle()
        val tabs = List(state.exerciseNames.take(state.numberOfExercises).size) { index ->
            ExerciseEntryTab(
                index = index,
                title = String.format(getString(R.string.tab_exercise_label), index.inc()),
                id = index.toLong()
            )
        }.toMutableList()
        val uiState = ExercisesEntryUiState(
            selectedIndex = state.selectedIndex,
            tabs = tabs,
            numExercises = state.numberOfExercises
        )
        tabs.add(
            ExerciseEntryTab(
                index = state.numberOfExercises,
                title = "",
                id = Long.MAX_VALUE,
                isAddTab = true
            )
        )
        Column(Modifier.fillMaxSize()) {
            GainzTopBar(
                title = getString(R.string.title_exercises_entry),
                showBack = true,
                onBackClick = { finish() }
            )
            ExercisesEntryScreen(
                uiState = uiState,
                onTabSelected = { index ->
                    if (index == state.numberOfExercises) {
                        viewModel.onAddExerciseClicked()
                    } else {
                        viewModel.onTabSelected(index)
                    }
                },
                pageContent = { pageIndex ->
                    ExEntryFragmentContainer(
                        pageIndex = pageIndex,
                        fragmentManager = supportFragmentManager,
                        getFragment = { idx ->
                            getOrCreateFragment(idx)
                        }
                    )
                }
            )
        }
    }

    private fun getOrCreateFragment(index: Int): ExerciseEntryFragment {
        var fragment = fragments[index]
        if (fragment == null) {
            fragment = ExerciseEntryFragment.newInstance(index)
            fragments[index] = fragment
        }
        return fragment
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}

private const val FRAGMENT_CONTAINER_ID_OFFSET = 100_000

@Composable
fun ExEntryFragmentContainer(
    pageIndex: Int,
    fragmentManager: FragmentManager,
    getFragment: (Int) -> Fragment
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            FragmentContainerView(ctx).apply {
                // Use a stable ID derived from the pageIndex to prevent crashes on recomposition
                id = FRAGMENT_CONTAINER_ID_OFFSET + pageIndex
            }
        },
        update = { view ->
            val fragment = getFragment(pageIndex)
            val existing = fragmentManager.findFragmentById(view.id)
            if (existing != fragment) {
                if (fragment.isAdded) {
                    fragmentManager.commitNow { remove(fragment) }
                }
                fragmentManager.commitNow {
                    replace(view.id, fragment)
                }
            } else {
                if (fragment.view != null && fragment.view?.parent != view) {
                    fragmentManager.commitNow { remove(fragment) }
                    fragmentManager.commitNow { replace(view.id, fragment) }
                }
            }
        }
    )
}
