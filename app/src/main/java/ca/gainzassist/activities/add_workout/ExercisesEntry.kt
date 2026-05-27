package ca.gainzassist.activities.add_workout

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import ca.gainzassist.R
import ca.gainzassist.activities.add_workout.Summary.Companion.EXTRA_CALLING_ACTIVITY
import ca.gainzassist.activities.add_workout.Summary.Companion.EXTRA_WORKOUT
import ca.gainzassist.adapters.WorkoutPagerAdapter
import ca.gainzassist.constants.ExerciseConst.MIN_INT
import ca.gainzassist.databinding.ActivityExercisesEntryBinding
import ca.gainzassist.models.Exercise
import ca.gainzassist.models.Workout
import ca.gainzassist.util.Misc.shrinkTo
import ca.gainzassist.util.UI.setInitTheme
import ca.gainzassist.util.UI.setToolbar
import com.google.android.material.tabs.TabLayout
import org.parceler.Parcels

class ExercisesEntry : AppCompatActivity(), ExEntry.ExEntryDataListener {

    companion object {
        const val TAB_LABEL = "Exercise %s"
    }

    private var workoutPagerAdapter: WorkoutPagerAdapter? = null
    private var tabLayoutOnPageChangeListener: TabLayout.TabLayoutOnPageChangeListener? = null
    private var viewPagerOnTabSelectedListener: TabLayout.ViewPagerOnTabSelectedListener? = null
    private val tabs = ArrayList<TabLayout.Tab>()

    var layInflater: LayoutInflater? = null

    private var workout: Workout? = null
    private var workoutId: Long = -1
    private var exercises = ArrayList<Exercise>()
    private var pos = 0
    private var numExs = 0
    private var addedExs = 0

    private lateinit var binding: ActivityExercisesEntryBinding

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
        binding = ActivityExercisesEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setToolbar(this, "Exercises Entry", true)

        val workoutEntryIntent = intent

        workout = Workout()
        workout?.id = -1
        workoutId = workout?.id ?: -1

        workout?.name = workoutEntryIntent.getStringExtra(WorkoutEntry.EXTRA_WORKOUT_NAME)
        numExs = workoutEntryIntent.getIntExtra(WorkoutEntry.EXTRA_NUM_EXERCISES, MIN_INT)

        exercises = ArrayList(List(numExs) { Exercise() })

        tabLayoutOnPageChangeListener = TabLayout.TabLayoutOnPageChangeListener(binding.tlayNavbar)
        viewPagerOnTabSelectedListener = object : TabLayout.ViewPagerOnTabSelectedListener(
            binding.vpFmtContainer
        ) {
            override fun onTabSelected(tab: TabLayout.Tab) {
                super.onTabSelected(tab)

                if (tab.text == null) {
                    numExs++
                    exercises.add(Exercise())

                    workoutPagerAdapter?.addTab()
                    workoutPagerAdapter?.notifyDataSetChanged()

                    pos = tab.position
                    tab.text = String.format(TAB_LABEL, pos + 1)
                    tab.icon = null
                    tabs.add(tab)

                    binding.tlayNavbar.addTab(
                        binding.tlayNavbar.newTab().setIcon(R.drawable.ic_plus)
                    )
                    binding.vpFmtContainer.setCurrentItem(numExs - 1, true)
                    tab.select()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                super.onTabUnselected(tab)
            }
        }

        setupPager()
    }

    private fun setupPager() {
        layInflater = layoutInflater

        tabLayoutOnPageChangeListener?.let(binding.vpFmtContainer::addOnPageChangeListener)
        binding.tlayNavbar.addOnTabSelectedListener(viewPagerOnTabSelectedListener)

        val newTabs = List(numExs) { i ->
            binding.tlayNavbar.newTab().setText(String.format(TAB_LABEL, i + 1))
        }.also { tabs.addAll(it) }
        newTabs.forEach { binding.tlayNavbar.addTab(it) }

        binding.tlayNavbar.addTab(binding.tlayNavbar.newTab().setIcon(R.drawable.ic_plus))

        workoutPagerAdapter = WorkoutPagerAdapter(supportFragmentManager, numExs)
        binding.vpFmtContainer.adapter = workoutPagerAdapter
        binding.vpFmtContainer.currentItem = 0
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
                Summary.CALLING_ACTIVITY.EXERCISES_ENTRY
            )
            summaryLauncher.launch(newWorkoutSummaryIntent)
        } else {
            setFirstEmptyTab()
        }
    }

    fun setFirstEmptyTab() {
        val firstEmptyIndex = exercises.indexOfFirst { it.name == null }
        if (firstEmptyIndex != -1) {
            binding.vpFmtContainer.setCurrentItem(firstEmptyIndex, true)
        }
    }

    override fun exerciseDoesNotExist(
        fmt: ExEntry,
        exerciseName: String?,
        skipIndex: Int
    ): Boolean {
        if (exerciseName.isNullOrEmpty()) {
            return false
        }

        for (ex in exercises) {
            if (skipIndex == exercises.indexOf(ex)) {
                continue
            }

            val name = ex.name
            if (name != null) {
                if (name == exerciseName) {
                    fmt.setExerciseExists()
                    return false
                }
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
        if (exercise != null) {
            addedExs--
        }

        numExs--

        exercises.removeAt(index)
        shrinkTo(exercises, numExs)

        binding.tlayNavbar.removeTabAt(index)
        tabs.removeAt(index)

        var selectedIndex = index
        if (selectedIndex != 0) {
            selectedIndex--
        }

        tabs[selectedIndex].select()

        for (i in selectedIndex until tabs.size) {
            tabs[i].text = String.format(TAB_LABEL, i + 1)
        }

        workoutPagerAdapter?.removeFragment(selectedIndex, exercises)
        workoutPagerAdapter?.notifyDataSetChanged()

        checkAndGoToSummary()

        if (numExs <= 1) {
            workoutPagerAdapter?.hideDelete()
        }
    }
}
