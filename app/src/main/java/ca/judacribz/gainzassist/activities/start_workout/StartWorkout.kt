package ca.judacribz.gainzassist.activities.start_workout

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindView
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.how_to_videos.HowToVideos
import ca.judacribz.gainzassist.adapters.WorkoutPagerAdapter
import ca.judacribz.gainzassist.models.Exercise
import ca.judacribz.gainzassist.models.Exercise.SetsType
import ca.judacribz.gainzassist.models.Workout
import ca.judacribz.gainzassist.models.db.WorkoutViewModel
import ca.judacribz.gainzassist.util.Misc.readValue
import ca.judacribz.gainzassist.util.Preferences.removeIncompleteWorkoutPref
import ca.judacribz.gainzassist.util.Preferences.getIncompleteSessionPref
import ca.judacribz.gainzassist.util.Preferences.removeIncompleteSessionPref
import ca.judacribz.gainzassist.util.Preferences.addIncompleteSessionPref
import ca.judacribz.gainzassist.util.Preferences.addIncompleteWorkoutPref
import ca.judacribz.gainzassist.util.UI.setInitView
import org.parceler.Parcels
import java.util.*

class StartWorkout : AppCompatActivity(), CurrWorkout.WarmupsListener {

    companion object {
        const val EXTRA_HOW_TO_VID = "ca.judacribz.gainzassist.activities.start_workout.EXTRA_HOW_TO_VID"
    }

    private val currWorkout = CurrWorkout.getInstance()
    var workout: Workout? = null
    var exercises: ArrayList<Exercise>? = null

    @BindView(R.id.tlay_navbar)
    lateinit var tabLayout: TabLayout

    @BindView(R.id.vp_fmt_container)
    lateinit var viewPager: ViewPager

    var layInflater: LayoutInflater? = null
    var setsView: View? = null
    var setList: RecyclerView? = null
    var tvExerciseName: TextView? = null
    var lp: LinearLayout.LayoutParams? = null
    var adapter: SetsAdapter? = null
    var warmupAdapter = ArrayList<SetsAdapter>()
    var mainAdapters = ArrayList<SetsAdapter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInitView(this, R.layout.activity_start_workout, R.string.resume_workout, true)

        val bundle = intent.extras
        if (bundle != null) {
            workout = Parcels.unwrap(bundle.getParcelable(ca.judacribz.gainzassist.activities.main.Main.EXTRA_WORKOUT))
            exercises = workout!!.exercises
        }

        tabLayout.setupWithViewPager(viewPager)

        lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp!!.setMargins(0, 10, 0, 10)

        setCurrSession()
    }

    fun setCurrSession() {
        currWorkout.setDataListener(this)
        val w = workout
        if (w != null && removeIncompleteWorkoutPref(this, w.name!!)) {
            @Suppress("UNCHECKED_CAST")
            currWorkout.setRetrievedWorkout(
                readValue(getIncompleteSessionPref(this, w.name!!)) as Map<String, Any>,
                w
            )
            removeIncompleteSessionPref(this, w.name!!)
        } else {
            currWorkout.setCurrWorkout(w!!)
        }
    }

    override fun onPause() {
        super.onPause()
        handleLeavingScreen()
    }

    fun handleLeavingScreen() {
        val w = workout
        if (w != null) {
            val jsonStr = currWorkout.saveSessionState()
            if (jsonStr.isNotEmpty()) {
                addIncompleteSessionPref(
                    this,
                    w.name!!,
                    jsonStr
                )
            }
            addIncompleteWorkoutPref(this, w.name!!)
            currWorkout.resetLocks()
        }
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
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            override fun onTabSelected(tab: TabLayout.Tab) {
                super.onTabSelected(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                super.onTabUnselected(tab)
            }
        })

        if (warmups.size == 0) {
            tabLayout.removeTabAt(0)
        }

        viewPager.adapter = WorkoutPagerAdapter(
            supportFragmentManager,
            exercises!!,
            warmups
        )
        viewPager.currentItem = tabLayout.tabCount - 2
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
        setList!!.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        adapter = SetsAdapter(exercise.setsList)
        setList!!.adapter = adapter

        when (setsType) {
            SetsType.MAIN_SET -> mainAdapters.add(adapter!!)
            SetsType.WARMUP_SET -> warmupAdapter.add(adapter!!)
        }
    }
}
