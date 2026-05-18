package ca.judacribz.gainzassist.activities.add_workout

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.add_workout.WorkoutEntry.Companion.EXTRA_NUM_EXERCISES
import ca.judacribz.gainzassist.activities.add_workout.WorkoutEntry.Companion.EXTRA_WORKOUT_NAME
import ca.judacribz.gainzassist.activities.add_workout.Summary.Companion.EXTRA_WORKOUT
import ca.judacribz.gainzassist.activities.add_workout.Summary.Companion.EXTRA_CALLING_ACTIVITY
import ca.judacribz.gainzassist.adapters.WorkoutPagerAdapter
import ca.judacribz.gainzassist.constants.ExerciseConst.MIN_INT
import ca.judacribz.gainzassist.databinding.ActivityExercisesEntryBinding
import ca.judacribz.gainzassist.models.Exercise
import ca.judacribz.gainzassist.models.Workout
import ca.judacribz.gainzassist.util.Misc.shrinkTo
import ca.judacribz.gainzassist.util.UI.setInitView
import com.orhanobut.logger.Logger
import org.parceler.Parcels
import java.util.*

class ExercisesEntry : AppCompatActivity(), ExEntry.ExEntryDataListener {

    companion object {
        const val REQ_NEW_WORKOUT_SUMMARY = 1002
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setInitView(
            this,
            R.layout.activity_exercises_entry,
            "Exercises Entry",
            true
        )
        binding = ActivityExercisesEntryBinding.bind(findViewById(R.id.cl_parent))

        val workoutEntryIntent = intent

        workout = Workout()
        workout!!.id = -1
        workoutId = workout!!.id

        workout!!.name = workoutEntryIntent.getStringExtra(WorkoutEntry.EXTRA_WORKOUT_NAME)
        numExs = workoutEntryIntent.getIntExtra(WorkoutEntry.EXTRA_NUM_EXERCISES, MIN_INT)

        exercises = ArrayList()
        for (i in 0 until numExs) {
            exercises.add(Exercise())
        }

        tabLayoutOnPageChangeListener = TabLayout.TabLayoutOnPageChangeListener(binding.tlayNavbar)
        viewPagerOnTabSelectedListener = object : TabLayout.ViewPagerOnTabSelectedListener(binding.vpFmtContainer) {
            override fun onTabSelected(tab: TabLayout.Tab) {
                super.onTabSelected(tab)

                if (tab.text == null) {
                    numExs++
                    exercises.add(Exercise())

                    workoutPagerAdapter!!.addTab()
                    workoutPagerAdapter!!.notifyDataSetChanged()

                    pos = tab.position
                    tab.text = String.format(TAB_LABEL, pos + 1)
                    tab.icon = null
                    tabs.add(tab)

                    binding.tlayNavbar.addTab(binding.tlayNavbar.newTab().setIcon(R.drawable.ic_plus))
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

        binding.vpFmtContainer.addOnPageChangeListener(tabLayoutOnPageChangeListener!!)
        binding.tlayNavbar.addOnTabSelectedListener(viewPagerOnTabSelectedListener!!)

        for (i in 0 until numExs) {
            val tab = binding.tlayNavbar.newTab().setText(String.format(TAB_LABEL, i + 1))
            tabs.add(tab)
            binding.tlayNavbar.addTab(tab)
        }

        binding.tlayNavbar.addTab(binding.tlayNavbar.newTab().setIcon(R.drawable.ic_plus))

        workoutPagerAdapter = WorkoutPagerAdapter(supportFragmentManager, numExs)
        binding.vpFmtContainer.adapter = workoutPagerAdapter
        binding.vpFmtContainer.currentItem = 0
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        when (res) {
            RESULT_OK -> {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    private fun checkAndGoToSummary() {
        if (addedExs >= numExs) {
            workout!!.exercises = exercises

            val newWorkoutSummaryIntent = Intent(this, Summary::class.java)
            newWorkoutSummaryIntent.putExtra(EXTRA_WORKOUT, Parcels.wrap(workout))
            newWorkoutSummaryIntent.putExtra(
                EXTRA_CALLING_ACTIVITY,
                Summary.CALLING_ACTIVITY.EXERCISES_ENTRY
            )
            startActivityForResult(newWorkoutSummaryIntent, REQ_NEW_WORKOUT_SUMMARY)
        } else {
            setFirstEmptyTab()
        }
    }

    fun setFirstEmptyTab() {
        for (i in 0 until numExs) {
            if (exercises[i].name == null) {
                binding.vpFmtContainer.setCurrentItem(i, true)
                break
            }
        }
    }

    override fun exerciseDoesNotExist(fmt: ExEntry, exerciseName: String, skipIndex: Int): Boolean {
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

    override fun exerciseDataReceived(exercise: Exercise, update: Boolean) {
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

        workoutPagerAdapter!!.removeFragment(selectedIndex, exercises)
        workoutPagerAdapter!!.notifyDataSetChanged()

        checkAndGoToSummary()

        if (numExs <= 1) {
            workoutPagerAdapter!!.hideDelete()
        }
    }
}
