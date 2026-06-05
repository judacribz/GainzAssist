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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import ca.gainzassist.activities.add_workout.Summary.Companion.EXTRA_CALLING_ACTIVITY
import ca.gainzassist.activities.add_workout.Summary.Companion.EXTRA_WORKOUT
import ca.gainzassist.constants.ExerciseConst.MIN_INT
import ca.gainzassist.models.Exercise
import ca.gainzassist.models.Workout
import ca.gainzassist.ui.components.GainzTopBar
import ca.gainzassist.util.Misc.shrinkTo
import ca.gainzassist.util.UI.setInitTheme
import org.parceler.Parcels

class ExercisesEntry : AppCompatActivity(), ExEntry.ExEntryDataListener {

    companion object {
        const val TAB_LABEL = "Exercise %s"
    }

    private var workout: Workout? = null
    private var workoutId: Long = -1
    private var exercises = ArrayList<Exercise>()
    private var numExs = 0
    private var addedExs = 0

    // Caching fragments to preserve state during recompositions/paging
    private val fragments = mutableMapOf<Int, ExEntry>()

    private var uiState by mutableStateOf(
        ExercisesEntryUiState(
            selectedIndex = 0,
            tabs = emptyList(),
            numExercises = 0
        )
    )

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

        workout = Workout()
        workout?.id = -1
        workoutId = workout?.id ?: -1

        workout?.name = workoutEntryIntent.getStringExtra(WorkoutEntry.EXTRA_WORKOUT_NAME)
        numExs = workoutEntryIntent.getIntExtra(WorkoutEntry.EXTRA_NUM_EXERCISES, MIN_INT)

        exercises = ArrayList(List(numExs) { Exercise() })

        updateUiState(0)

        setContent {
            Column(Modifier.fillMaxSize()) {
                GainzTopBar(
                    title = "Exercises Entry",
                    showBack = true,
                    onBackClick = { finish() }
                )
                ExercisesEntryScreen(
                    uiState = uiState,
                    onTabSelected = { index ->
                        handleTabSelected(index)
                    },
                    pageContent = { pageIndex ->
                        ExEntryFragmentContainer(
                            pageIndex = pageIndex,
                            fragmentManager = supportFragmentManager,
                            getFragment = { idx -> getOrCreateFragment(idx) }
                        )
                    }
                )
            }
        }
    }

    private fun handleTabSelected(index: Int) {
        val tab = uiState.tabs.getOrNull(index)
        if (tab?.isAddTab == true) {
            addNewExercise()
        } else {
            updateUiState(index)
        }
    }

    private fun addNewExercise() {
        numExs++
        exercises.add(Exercise())
        updateUiState(numExs - 1)
        
        // Show delete on all fragments since we have more than 1 exercise
        if (numExs > 1) {
            fragments.values.forEach { it.showDelete() }
        }
    }

    private fun updateUiState(selectedIndex: Int) {
        val tabs = exercises.take(numExs).mapIndexed { i, ex ->
            ExerciseEntryTab(
                index = i,
                title = String.format(TAB_LABEL, i + 1),
                id = System.identityHashCode(ex).toLong()
            )
        }.toMutableList()

        // Plus Tab
        tabs.add(ExerciseEntryTab(index = numExs, title = "", id = Long.MAX_VALUE, isAddTab = true))

        uiState = uiState.copy(
            selectedIndex = selectedIndex,
            tabs = tabs,
            numExercises = numExs
        )
    }



    private fun getOrCreateFragment(index: Int): ExEntry {
        var fragment = fragments[index]
        if (fragment == null) {
            fragment = ExEntry().apply {
                setInd(index)
                if (numExs <= 1) {
                    hideDelete()
                }
                val ex = exercises.getOrNull(index)
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

    private fun checkAndGoToSummary() {
        if (addedExs >= numExs) {
            workout?.exercises = exercises

            val newWorkoutSummaryIntent = Intent(this, Summary::class.java)
            newWorkoutSummaryIntent.putExtra(EXTRA_WORKOUT, Parcels.wrap(workout))
            newWorkoutSummaryIntent.putExtra(
                EXTRA_CALLING_ACTIVITY,
                Summary.CallingActivity.EXERCISES_ENTRY
            )
            summaryLauncher.launch(newWorkoutSummaryIntent)
        } else {
            setFirstEmptyTab()
        }
    }

    fun setFirstEmptyTab() {
        val firstEmptyIndex = exercises.indexOfFirst { it.name == null }
        if (firstEmptyIndex != -1) {
            updateUiState(firstEmptyIndex)
        }
    }

    override fun exerciseDoesNotExist(
        fmt: ExEntry,
        exerciseName: String?,
        skipIndex: Int
    ): Boolean {
        val targetName = exerciseName?.trim().takeUnless(String?::isNullOrEmpty) ?: return false

        for (ex in exercises) {
            if (skipIndex == exercises.indexOf(ex)) continue

            val name = ex.name
            if (name == targetName) {
                fmt.setExerciseExists()
                return false
            }
        }

        return true
    }

    override fun exerciseDataReceived(exercise: Exercise?, update: Boolean) {
        exercise ?: return
        exercise.workoutId = workoutId
        exercises[exercise.exerciseNumber] = exercise

        if (!update) {
            addedExs++
        }

        checkAndGoToSummary()
    }

    override fun deleteExercise(exercise: Exercise?, index: Int) {
        if (exercise != null && exercise.name != null) {
            addedExs--
        }

        numExs--

        exercises.removeAt(index)
        shrinkTo(exercises, numExs)

        // Re-index remaining exercises
        exercises.forEachIndexed { i, ex ->
            ex.exerciseNumber = i
        }

        // Shift fragments down to match new indices
        val newFragments = mutableMapOf<Int, ExEntry>()
        fragments.filterKeys { it != index }.forEach { (oldIdx, frag) ->
            val newIdx = if (oldIdx > index) oldIdx - 1 else oldIdx
            frag.setInd(newIdx)
            newFragments[newIdx] = frag
        }

        // Optional: Remove the deleted fragment from FragmentManager to fully clean up
        val deletedFrag = fragments[index]
        if (deletedFrag != null) {
            supportFragmentManager.commit {
                remove(deletedFrag)
            }
        }

        fragments.clear()
        fragments.putAll(newFragments)

        var selectedIndex = index
        if (selectedIndex >= numExs) {
            selectedIndex = numExs - 1
        }
        if (selectedIndex < 0) {
            selectedIndex = 0
        }

        if (numExs <= 1) {
            fragments[0]?.hideDelete()
        }

        updateUiState(selectedIndex)
        checkAndGoToSummary()
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
