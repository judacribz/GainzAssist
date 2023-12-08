package ca.judacribz.gainzassist.activities.add_workout

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.add_workout.ExEntryFragment.ExEntryDataListener
import ca.judacribz.gainzassist.activities.add_workout.Summary.Companion.EXTRA_CALLING_ACTIVITY
import ca.judacribz.gainzassist.activities.add_workout.Summary.Companion.EXTRA_WORKOUT
import ca.judacribz.gainzassist.activities.add_workout.WorkoutEntry.Companion.EXTRA_NUM_EXERCISES
import ca.judacribz.gainzassist.activities.add_workout.WorkoutEntry.Companion.EXTRA_WORKOUT_NAME
import ca.judacribz.gainzassist.adapters.WorkoutPagerAdapter
import ca.judacribz.gainzassist.constants.ExerciseConst.MIN_INT
import ca.judacribz.gainzassist.models.Exercise
import ca.judacribz.gainzassist.models.Workout
import ca.judacribz.gainzassist.util.Misc.shrinkTo
import ca.judacribz.gainzassist.util.UI
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import com.google.android.material.tabs.TabLayout.ViewPagerOnTabSelectedListener
import com.orhanobut.logger.Logger

class ExercisesEntryActivity : AppCompatActivity(), ExEntryDataListener {
    // --------------------------------------------------------------------------------------------
    // Global Vars
    // --------------------------------------------------------------------------------------------
    lateinit var workoutPagerAdapter: WorkoutPagerAdapter
    private lateinit var tabLayoutOnPageChangeListener: TabLayoutOnPageChangeListener
    private lateinit var viewPagerOnTabSelectedListener: ViewPagerOnTabSelectedListener
    var tabs = ArrayList<TabLayout.Tab>()
    private lateinit var layInflater: LayoutInflater
    lateinit var workout: Workout
    var workoutId: Long = 0
    lateinit var exercises: ArrayList<Exercise>
    var pos = 0
    var numExs = 0
    private var addedExs = 0

    @BindView(R.id.tlay_navbar)
    lateinit var tabLayout: TabLayout

    @BindView(R.id.vp_fmt_container)
    lateinit var viewPager: ViewPager

    // --------------------------------------------------------------------------------------------
    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UI.setInitView(
            this,
            R.layout.activity_exercises_entry,
            "Exercises Entry",
            true
        )
        val workoutEntryIntent = intent
        workout = Workout()
        workout.id = -1
        workoutId = workout.id
        workoutEntryIntent.getStringExtra(EXTRA_WORKOUT_NAME)?.let { workout.name = it }
        numExs = workoutEntryIntent.getIntExtra(EXTRA_NUM_EXERCISES, MIN_INT)
        exercises = ArrayList()
        for (i in 0 until numExs) {
            exercises.add(Exercise())
        }
        tabLayoutOnPageChangeListener = TabLayoutOnPageChangeListener(tabLayout)
        viewPagerOnTabSelectedListener = object : ViewPagerOnTabSelectedListener(viewPager) {
            override fun onTabSelected(tab: TabLayout.Tab) {
                super.onTabSelected(tab)
                if (tab.text == null) {
                    numExs++
                    exercises.add(Exercise())
                    workoutPagerAdapter.addTab()
                    workoutPagerAdapter.notifyDataSetChanged()
                    pos = tab.position
                    tab.setText(String.format(TAB_LABEL, pos + 1))
                    tab.setIcon(null)
                    tabs.add(tab)
                    tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_plus))
                    viewPager.setCurrentItem(numExs - 1, true)
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
        var tab: TabLayout.Tab
        layInflater = layoutInflater
        viewPager.addOnPageChangeListener(tabLayoutOnPageChangeListener)
        tabLayout.addOnTabSelectedListener(viewPagerOnTabSelectedListener)
        for (i in 0 until numExs) {
            tab = tabLayout.newTab().setText(String.format(TAB_LABEL, i + 1))
            tabs.add(tab)
            tabLayout.addTab(tab)
        }
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_plus))
        viewPager.adapter =
            WorkoutPagerAdapter(supportFragmentManager, numExs).also { workoutPagerAdapter = it }
        viewPager.currentItem = 0
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)
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

    //AppCompatActivity//Override//////////////////////////////////////////////////////////////////
    // ExEntry.ExEntryDataListener Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun exerciseDoesNotExist(
        fmt: ExEntryFragment,
        exerciseName: String,
        skipIndex: Int
    ): Boolean {
        for (ex in exercises) {
            if (skipIndex == exercises.indexOf(ex)) {
                continue
            }
            if (ex.name == exerciseName) {
                fmt.setExerciseExists()
                return false
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
        lognow()
        checkAndGoToSummary()
    }

    //TODO remove debug
    private fun lognow() {
        var e: Exercise?
        for (i in exercises.indices) {
            e = exercises[i]
            var g: String
            g = if (e == null) {
                "null"
            } else ({
                e.name
            }).toString()
            Logger.d("INDEX = " + (i + 1) + " value=  " + g)
        }
        Logger.d("INDEX = -----------------------------------")
    }

    override fun deleteExercise(exercise: Exercise, index: Int) {
        var currIndex = index
        addedExs--
        numExs--
        exercises.removeAt(currIndex)
        shrinkTo(exercises, numExs)
        lognow()
        tabLayout.removeTabAt(currIndex)
        tabs.removeAt(currIndex)
        if (currIndex != 0) {
            currIndex--
        }
        tabs[currIndex].select()
        var tab: TabLayout.Tab
        for (i in currIndex until tabs.size) {
            tab = tabs[i]
            tab.setText(String.format(TAB_LABEL, i + 1))
        }
        workoutPagerAdapter.removeFragment(currIndex, exercises)
        workoutPagerAdapter.notifyDataSetChanged()
        checkAndGoToSummary()
        if (numExs <= 1) {
            workoutPagerAdapter.hideDelete()
        }
    }

    private fun checkAndGoToSummary() {
        if (addedExs >= numExs) {
            workout.exercises = exercises
            val newWorkoutSummaryIntent = Intent(this, Summary::class.java)
            newWorkoutSummaryIntent.putExtra(EXTRA_WORKOUT, workout)
            newWorkoutSummaryIntent.putExtra(
                EXTRA_CALLING_ACTIVITY,
                Summary.CALLING_ACTIVITY.EXERCISES_ENTRY
            )
            startActivityForResult(newWorkoutSummaryIntent, REQ_NEW_WORKOUT_SUMMARY)
        }
    } //ExEntry.ExEntryDataListener//Override////////////////////////////////////////////////////////

    companion object {
        // Constants
        // --------------------------------------------------------------------------------------------
        const val REQ_NEW_WORKOUT_SUMMARY = 1002
        const val TAB_LABEL = "Exercise %s"
    }
}
