package ca.judacribz.gainzassist.activities.add_workout

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import butterknife.BindView
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.add_workout.WorkoutEntry.Companion.EXTRA_NUM_EXERCISES
import ca.judacribz.gainzassist.activities.add_workout.WorkoutEntry.Companion.EXTRA_WORKOUT_NAME
import ca.judacribz.gainzassist.activities.main.Main.Companion.EXERCISES_ENTRY
import ca.judacribz.gainzassist.activities.main.Main.Companion.EXTRA_CALLING_ACTIVITY
import ca.judacribz.gainzassist.activities.main.Main.Companion.EXTRA_WORKOUT
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

    private var workoutPagerAdapter: WorkoutPagerAdapter? = null
    var workoutName: String? = null
    var numExercises = 0
    var exercises: ArrayList<Exercise>? = null
    var workout: Workout? = null

    @BindView(R.id.tlay_navbar)
    lateinit var tabLayout: TabLayout

    @BindView(R.id.vp_fmt_container)
    lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInitView(this, R.layout.activity_exercises_entry, R.string.title_exercises_entry, true)

        val bundle = intent.extras
        if (bundle != null) {
            workoutName = bundle.getString(EXTRA_WORKOUT_NAME)
            numExercises = bundle.getInt(EXTRA_NUM_EXERCISES)
        }

        workout = Workout(workoutName, null)
        exercises = ArrayList()

        tabLayout.setupWithViewPager(viewPager)
        workoutPagerAdapter = WorkoutPagerAdapter(supportFragmentManager, numExercises)
        viewPager.adapter = workoutPagerAdapter
        viewPager.offscreenPageLimit = numExercises
    }

    override fun exerciseDoesNotExist(fmt: ExEntry, exerciseName: String, skipIndex: Int): Boolean {
        for (exercise in exercises!!) {
            if (exercises!!.indexOf(exercise) != skipIndex && exercise.name!!.toLowerCase() == exerciseName.toLowerCase()) {
                fmt.setExerciseExists()
                return false
            }
        }
        return true
    }

    override fun exerciseDataReceived(exercise: Exercise, update: Boolean) {
        val index = exercise.exerciseNumber
        if (update) {
            exercises!![index] = exercise
        } else {
            if (index < exercises!!.size) {
                exercises!![index] = exercise
            } else {
                exercises!!.add(exercise)
            }

            if (index < numExercises - 1) {
                viewPager.currentItem = index + 1
            } else {
                workout!!.exercises = exercises!!
                val intent = Intent(this, Summary::class.java)
                intent.putExtra(EXTRA_WORKOUT, Parcels.wrap(workout))
                intent.putExtra(EXTRA_CALLING_ACTIVITY, EXERCISES_ENTRY)
                startActivity(intent)
            }
        }
    }

    override fun deleteExercise(exercise: Exercise?, index: Int) {
        if (exercises!!.size > MIN_INT) {
            exercises!!.removeAt(index)
            shrinkTo(exercises, exercises!!.size)
            workoutPagerAdapter!!.removeFragment(index, exercises!!)
            numExercises--
        }
    }
}
