package ca.gainzassist.activities.start_workout

import android.content.Intent
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import ca.gainzassist.R
import ca.gainzassist.activities.how_to_videos.HowToVideos
import ca.gainzassist.adapters.WorkoutPagerAdapter
import ca.gainzassist.databinding.ActivityStartWorkoutBinding
import ca.gainzassist.models.Exercise
import ca.gainzassist.models.Exercise.SetsType
import ca.gainzassist.models.Workout
import ca.gainzassist.util.Misc.readValue
import ca.gainzassist.util.Preferences.removeIncompleteWorkoutPref
import ca.gainzassist.util.Preferences.getIncompleteSessionPref
import ca.gainzassist.util.Preferences.removeIncompleteSessionPref
import ca.gainzassist.util.Preferences.addIncompleteSessionPref
import ca.gainzassist.util.Preferences.addIncompleteWorkoutPref
import ca.gainzassist.util.UI.setInitTheme
import ca.gainzassist.util.UI.setToolbar
import com.facebook.rebound.ui.Util.dpToPx
import org.parceler.Parcels
import java.util.*

class StartWorkout : AppCompatActivity(), CurrWorkout.WarmupsListener {

    companion object {
        const val EXTRA_HOW_TO_VID = "ca.gainzassist.activities.start_workout.EXTRA_HOW_TO_VID"
    }

    private val currWorkout = CurrWorkout.getInstance()
    var workout: Workout? = null
    var exercises: ArrayList<Exercise>? = null

    private lateinit var binding: ActivityStartWorkoutBinding

    var layInflater: LayoutInflater? = null
    var setsView: View? = null
    var setList: RecyclerView? = null
    var tvExerciseName: TextView? = null
    var lp: RelativeLayout.LayoutParams? = null
    var adapter: SetsAdapter? = null
    var warmupAdapter = ArrayList<SetsAdapter>()
    var mainAdapters = ArrayList<SetsAdapter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        workout = Parcels.unwrap(intent.getParcelableExtra(ca.gainzassist.activities.main.Main.EXTRA_WORKOUT))
        exercises = workout!!.exercises
        setInitTheme(this)
        binding = ActivityStartWorkoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setToolbar(this, workout!!.name!!, true)

        lp = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        val dpval = dpToPx(5f, resources)
        lp!!.setMargins(dpval, dpval, dpval, dpval)
    }

    override fun onResume() {
        super.onResume()
        if (binding.vpFmtContainer.adapter == null) {
            setCurrSession()
        }
    }

    fun setCurrSession() {
        currWorkout.setDataListener(this)

        val currentWorkout = workout ?: return
        val workoutName = currentWorkout.name ?: return

        if (removeIncompleteWorkoutPref(this, workoutName)) {
            try {
                val savedSession = getIncompleteSessionPref(this, workoutName)

                if (savedSession.isNullOrEmpty()) {
                    removeIncompleteSessionPref(this, workoutName)
                    currWorkout.setCurrWorkout(currentWorkout)
                    return
                }

                @Suppress("UNCHECKED_CAST")
                currWorkout.setRetrievedWorkout(
                    readValue(savedSession) as Map<String, Any?>,
                    currentWorkout
                )

                removeIncompleteSessionPref(this, workoutName)
            } catch (ex: Exception) {
                com.orhanobut.logger.Logger.e(ex, "Failed to restore incomplete workout. Starting fresh.")
                removeIncompleteSessionPref(this, workoutName)
                currWorkout.setCurrWorkout(currentWorkout)
            }
        } else {
            currWorkout.setCurrWorkout(currentWorkout)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        currWorkout.resetIndices()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        handleLeavingScreen()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        currWorkout.unsetTimer()
        handleLeavingScreen()
    }

    fun handleLeavingScreen() {
        val jsonStr = currWorkout.saveSessionState()
        if (jsonStr.isNotEmpty()) {
            addIncompleteSessionPref(
                this,
                workout!!.name!!,
                jsonStr
            )
        }
        addIncompleteWorkoutPref(this, workout!!.name!!)
        currWorkout.resetLocks()
    }

    override fun onCreateOptionsMenu(mainMenu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_start_workout, mainMenu)
        return super.onCreateOptionsMenu(mainMenu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.act_how_to -> {
                val intent = Intent(this, HowToVideos::class.java)
                intent.putExtra(EXTRA_HOW_TO_VID, currWorkout.currExName)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun warmupsGenerated(warmups: ArrayList<Exercise>) {
        setupPager(warmups)
    }

    private fun setupPager(warmups: ArrayList<Exercise>) {
        layInflater = layoutInflater
        binding.vpFmtContainer.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(binding.tlayNavbar))
        binding.tlayNavbar.addOnTabSelectedListener(object : TabLayout.ViewPagerOnTabSelectedListener(binding.vpFmtContainer) {
            override fun onTabSelected(tab: TabLayout.Tab) {
                super.onTabSelected(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                super.onTabUnselected(tab)
            }
        })

        if (warmups.size == 0) {
            binding.tlayNavbar.removeTabAt(0)
        }

        binding.vpFmtContainer.adapter = WorkoutPagerAdapter(
            supportFragmentManager,
            exercises!!,
            warmups
        )
        binding.vpFmtContainer.currentItem = binding.tlayNavbar.tabCount - 2
    }

    fun displaySets(
        setsType: SetsType,
        exercise: Exercise,
        llSets: LinearLayout
    ) {
        layInflater = layoutInflater
        setsView = layInflater!!.inflate(R.layout.part_horizontal_rv, llSets, false)
        setsView!!.layoutParams = lp
        llSets.addView(setsView, 0)

        tvExerciseName = setsView!!.findViewById(R.id.tv_exercise_name)
        tvExerciseName!!.text = exercise.name

        setList = setsView!!.findViewById(R.id.rv_exercise_sets)
        setList!!.setHasFixedSize(true)
        setList!!.setLayoutManager(LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        ))

        adapter = SetsAdapter(exercise.setsList)
        setList!!.adapter = adapter

        when (setsType) {
            SetsType.MAIN_SET -> mainAdapters.add(adapter!!)
            SetsType.WARMUP_SET -> warmupAdapter.add(adapter!!)
        }
    }
}
