package ca.judacribz.gainzassist.activities.add_workout

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import butterknife.BindView
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.add_workout.Summary.CALLING_ACTIVITY.EXERCISES_ENTRY
import ca.judacribz.gainzassist.activities.add_workout.Summary.Companion.EXTRA_CALLING_ACTIVITY
import ca.judacribz.gainzassist.activities.add_workout.Summary.Companion.EXTRA_WORKOUT
import ca.judacribz.gainzassist.activities.add_workout.WorkoutEntry.Companion.EXTRA_NUM_EXERCISES
import ca.judacribz.gainzassist.activities.add_workout.WorkoutEntry.Companion.EXTRA_WORKOUT_NAME
import ca.judacribz.gainzassist.adapters.WorkoutPagerAdapter
import ca.judacribz.gainzassist.constants.ExerciseConst.MIN_INT
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

    var workoutPagerAdapter: WorkoutPagerAdapter? = null
    var tabLayoutOnPageChangeListener: TabLayout.TabLayoutOnPageChangeListener? = null
    private var viewPagerOnTabSelectedListener: ViewPagerOnTabSelectedListener? = null
    var tabs = ArrayList<TabLayout.Tab>()

    var layInflater: LayoutInflater? = null

    var workout: Workout? = null
    var workoutId: Long = 0
    var exercises: ArrayList<Exercise>? = null
    var pos: Int = 0
    var numExs: Int = 0
    var addedExs = 0

    @BindView(R.id.tlay_navbar)
    lateinit var tabLayout: TabLayout

    @BindView(R.id.vp_fmt_container)
    lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInitView(
            this,
            R.layout.activity_exercises_entry,
            "Exercises Entry",
            true
        )
        val workoutEntryIntent = intent

        workout = Workout()
        workout!!.id = -1
        workoutId = workout!!.id

        workout!!.name = workoutEntryIntent.getStringExtra(EXTRA_WORKOUT_NAME)
        numExs = workoutEntryIntent.getIntExtra(EXTRA_NUM_EXERCISES, MIN_INT)

        exercises = ArrayList()
        for (i in 0 until numExs) {
            exercises!!.add(Exercise())
        }

        tabLayoutOnPageChangeListener = TabLayout.TabLayoutOnPageChangeListener(tabLayout)
        viewPagerOnTabSelectedListener = ViewPagerOnTabSelectedListener(viewPager)

        setupPager()
    }

    private inner class ViewPagerOnTabSelectedListener(viewPager: ViewPager) :
        TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
        override fun onTabSelected(tab: TabLayout.Tab) {
            super.onTabSelected(tab)

            if (tab.text == null) {
                numExs++
                exercises!!.add(Exercise())
                workoutPagerAdapter!!.addTab()
                workoutPagerAdapter!!.notifyDataSetChanged()

                pos = tab.position
                tab.text = String.format(TAB_LABEL, pos + 1)
                tab.icon = null
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

    private fun setupPager() {
        var tab: TabLayout.Tab
        layInflater = layoutInflater

        viewPager.addOnPageChangeListener(tabLayoutOnPageChangeListener!!)
        tabLayout.addOnTabSelectedListener(viewPagerOnTabSelectedListener!!)

        for (i in 0 until this.numExs) {
            tab = tabLayout.newTab().setText(String.format(TAB_LABEL, i + 1))
            tabs.add(tab)
            tabLayout.addTab(tab)
        }
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_plus))

        workoutPagerAdapter = WorkoutPagerAdapter(supportFragmentManager, this.numExs)
        viewPager.adapter = workoutPagerAdapter
        viewPager.currentItem = 0
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        if (res == RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun exerciseDoesNotExist(fmt: ExEntry, exerciseName: String, skipIndex: Int): Boolean {
        var name: String?

        for (ex in exercises!!) {
            if (skipIndex == exercises!!.indexOf(ex)) {
                continue
            }

            if (ex.name.also { name = it } != null) {
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
        exercises!![exercise.exerciseNumber] = exercise

        if (!update) {
            addedExs++
        }

        lognow()
        checkAndGoToSummary()
    }

    private fun lognow() {
        var e: Exercise
        for (i in exercises!!.indices) {
            e = exercises!![i]
            val g: String? = if (e == null) {
                "null"
            } else {
                e.name
            }
            Logger.d("INDEX = " + (i + 1) + " value=  " + g)
        }
        Logger.d("INDEX = -----------------------------------")
    }

    override fun deleteExercise(exercise: Exercise?, index: Int) {
        if (exercise != null) {
            addedExs--
        }
        this.numExs--

        exercises!!.removeAt(index)
        shrinkTo(exercises, numExs)

        lognow()

        tabLayout.removeTabAt(index)
        tabs.removeAt(index)
        var newIndex = index
        if (newIndex != 0) {
            newIndex--
        }
        tabs[newIndex].select()

        var tab: TabLayout.Tab
        for (i in newIndex until tabs.size) {
            tab = tabs[i]
            tab.text = String.format(TAB_LABEL, i + 1)
        }

        workoutPagerAdapter!!.removeFragment(index, exercises)
        workoutPagerAdapter!!.notifyDataSetChanged()

        checkAndGoToSummary()

        if (this.numExs <= 1) {
            workoutPagerAdapter!!.hideDelete()
        }
    }

    private fun checkAndGoToSummary() {
        if (addedExs >= numExs) {
            workout!!.exercises = exercises

            val newWorkoutSummaryIntent = Intent(this, Summary::class.java)
            newWorkoutSummaryIntent.putExtra(EXTRA_WORKOUT, Parcels.wrap(workout))
            newWorkoutSummaryIntent.putExtra(EXTRA_CALLING_ACTIVITY, EXERCISES_ENTRY)
            startActivityForResult(newWorkoutSummaryIntent, REQ_NEW_WORKOUT_SUMMARY)
        } else {
            setFirstEmptyTab()
        }
    }

    fun setFirstEmptyTab() {
        for (i in 0 until numExs) {
            if (exercises!![i].name == null) {
                viewPager.setCurrentItem(i, true)
                break
            }
        }
    }
}
