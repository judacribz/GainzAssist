package ca.gainzassist.activities.add_workout

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ca.gainzassist.activities.add_workout.Summary.Companion.EXTRA_CALLING_ACTIVITY
import ca.gainzassist.activities.add_workout.Summary.Companion.EXTRA_WORKOUT
import ca.gainzassist.constants.ExerciseConst.MIN_INT
import ca.gainzassist.models.Exercise
import ca.gainzassist.models.Workout
import ca.gainzassist.ui.components.GainzTopBar
import ca.gainzassist.util.UI.setInitTheme
import ca.gainzassist.presentation.add_workout.ExercisesEntryViewModel
import ca.gainzassist.presentation.add_workout.ExercisesEntryViewModelEvent
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.parceler.Parcels

class ExercisesEntry : AppCompatActivity(), ExEntry.ExEntryDataListener {

    companion object {
        const val TAB_LABEL = "Exercise %s"
    }

    private val viewModel: ExercisesEntryViewModel by viewModel()

    // Caching fragments to preserve state during recompositions/paging
    private val fragments = mutableMapOf<Int, ExEntry>()

    private val summaryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInitTheme(this)

        val workoutEntryIntent = intent
        val workoutName = workoutEntryIntent.getStringExtra(WorkoutEntry.EXTRA_WORKOUT_NAME) ?: ""
        val numExs = workoutEntryIntent.getIntExtra(WorkoutEntry.EXTRA_NUM_EXERCISES, MIN_INT)

        viewModel.initialize(workoutName, numExs)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is ExercisesEntryViewModelEvent.GoToSummary -> {
                            val workout = Workout().apply {
                                this.id = -1
                                this.name = event.workoutName
                                this.exercises = ArrayList(event.exercises)
                            }

                            val newWorkoutSummaryIntent = Intent(this@ExercisesEntry, Summary::class.java)
                            newWorkoutSummaryIntent.putExtra(EXTRA_WORKOUT, Parcels.wrap(workout))
                            newWorkoutSummaryIntent.putExtra(
                                EXTRA_CALLING_ACTIVITY,
                                Summary.CallingActivity.EXERCISES_ENTRY
                            )
                            summaryLauncher.launch(newWorkoutSummaryIntent)
                        }
                    }
                }
            }
        }

        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()

            val tabs = state.exerciseNames.take(state.numberOfExercises).mapIndexed { i, _ ->
                ExerciseEntryTab(
                    index = i,
                    title = String.format(TAB_LABEL, i + 1),
                    id = i.toLong()
                )
            }.toMutableList()

            tabs.add(ExerciseEntryTab(index = state.numberOfExercises, title = "", id = Long.MAX_VALUE, isAddTab = true))

            val uiState = ExercisesEntryUiState(
                selectedIndex = state.selectedIndex,
                tabs = tabs,
                numExercises = state.numberOfExercises
            )

            Column(Modifier.fillMaxSize()) {
                GainzTopBar(
                    title = "Exercises Entry",
                    showBack = true,
                    onBackClick = { finish() }
                )
                ExercisesEntryScreen(
                    uiState = uiState,
                    onTabSelected = { index ->
                        if (index == state.numberOfExercises) {
                            viewModel.onAddExerciseClicked()
                            if (state.numberOfExercises > 0) {
                                fragments.values.forEach { it.showDelete() }
                            }
                        } else {
                            viewModel.onTabSelected(index)
                        }
                    },
                    pageContent = { pageIndex ->
                        ExEntryFragmentContainer(
                            pageIndex = pageIndex,
                            fragmentManager = supportFragmentManager,
                            getFragment = { idx -> getOrCreateFragment(idx, state.numberOfExercises) }
                        )
                    }
                )
            }
        }
    }

    private fun getOrCreateFragment(index: Int, numExs: Int): ExEntry {
        var fragment = fragments[index]
        if (fragment == null) {
            fragment = ExEntry().apply {
                setInd(index)
                if (numExs <= 1) {
                    hideDelete()
                }
                val ex = viewModel.state.value.exercises.getOrNull(index)
                if (ex != null && ex.name != null) {
                    updateExFields(ex)
                }
            }
            fragments[index] = fragment
        }
        return fragment
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun exerciseDoesNotExist(
        fmt: ExEntry,
        exerciseName: String?,
        skipIndex: Int
    ): Boolean {
        val targetName = exerciseName?.trim().takeUnless(String?::isNullOrEmpty) ?: return false
        val names = viewModel.state.value.exerciseNames

        for ((i, name) in names.withIndex()) {
            if (i == skipIndex) continue
            if (name.equals(targetName, ignoreCase = true)) {
                fmt.setExerciseExists()
                return false
            }
        }

        return true
    }

    override fun exerciseDataReceived(exercise: Exercise?, update: Boolean) {
        exercise ?: return
        exercise.workoutId = -1
        viewModel.onExerciseSubmitted(exercise)
    }

    override fun deleteExercise(exercise: Exercise?, index: Int) {
        // Shift fragments down to match new indices
        val newFragments = mutableMapOf<Int, ExEntry>()
        fragments.filterKeys { it != index }.forEach { (oldIdx, frag) ->
            val newIdx = if (oldIdx > index) oldIdx - 1 else oldIdx
            frag.setInd(newIdx)
            newFragments[newIdx] = frag
        }

        val deletedFrag = fragments[index]
        if (deletedFrag != null) {
            supportFragmentManager.commit {
                remove(deletedFrag)
            }
        }

        fragments.clear()
        fragments.putAll(newFragments)

        if (viewModel.state.value.numberOfExercises <= 2) {
            fragments[0]?.hideDelete()
        }

        viewModel.onExerciseDeleted(index)
    }
}

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
                id = View.generateViewId()
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