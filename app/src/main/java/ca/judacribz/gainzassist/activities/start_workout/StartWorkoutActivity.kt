package ca.judacribz.gainzassist.activities.start_workout

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.how_to_videos.HowToVideos
import ca.judacribz.gainzassist.activities.main.Main
import ca.judacribz.gainzassist.activities.start_workout.CurrWorkout.WarmupsListener
import ca.judacribz.gainzassist.adapters.WorkoutPagerAdapter
import ca.judacribz.gainzassist.models.Exercise
import ca.judacribz.gainzassist.models.Exercise.SetsType
import ca.judacribz.gainzassist.models.Workout
import ca.judacribz.gainzassist.util.Misc
import ca.judacribz.gainzassist.util.Preferences
import ca.judacribz.gainzassist.util.UI
import com.facebook.rebound.ui.Util
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import com.google.android.material.tabs.TabLayout.ViewPagerOnTabSelectedListener

class StartWorkoutActivity : AppCompatActivity(), WarmupsListener {
    // --------------------------------------------------------------------------------------------
    // Global Vars
    // --------------------------------------------------------------------------------------------
    lateinit var adapter: SetsAdapter
    var warmupAdapter = ArrayList<SetsAdapter>()
    var mainAdapters = ArrayList<SetsAdapter>()
    lateinit var workout: Workout
    lateinit var exercises: ArrayList<Exercise>
    lateinit var layInflater: LayoutInflater
    lateinit var setsView: View
    lateinit var tvExerciseName: TextView
    lateinit var setList: RecyclerView
    lateinit var lp: RelativeLayout.LayoutParams
    val currWorkout = CurrWorkout

    @BindView(R.id.tlay_navbar)
    lateinit var tabLayout: TabLayout

    @BindView(R.id.vp_fmt_container)
    lateinit var viewPager: ViewPager

    // --------------------------------------------------------------------------------------------
    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        intent.getParcelableExtra<Workout>(Main.EXTRA_WORKOUT)?.let { workout = it }
        exercises = workout.exercises
        UI.setInitView(this, R.layout.activity_start_workout, workout.name, true)
        lp = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        val dpval = Util.dpToPx(5f, resources)
        lp.setMargins(dpval, dpval, dpval, dpval)
    }

    override fun onResume() {
        super.onResume()
        if (viewPager.adapter == null) {
            setCurrSession()
        }
    }

    fun setCurrSession() {
        currWorkout.setWarmupsListener(this)
        if (Preferences.removeIncompleteWorkoutPref(this, workout.name)) {
            currWorkout.setRetrievedWorkout(
                Misc.readValue(Preferences.getIncompleteSessionPref(this, workout.name)),
                workout
            )
            Preferences.removeIncompleteSessionPref(this, workout.name)
        } else {
            currWorkout.setCurrWorkout(workout)
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
        val jsonStr: String = currWorkout.saveSessionState()
        if (!jsonStr.isEmpty()) {
            Preferences.addIncompleteSessionPref(
                this,
                workout.name,
                jsonStr
            )
        }
        Preferences.addIncompleteWorkoutPref(this, workout.name)
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

    //AppCompatActivity//Override//////////////////////////////////////////////////////////////////
    // CurrWorkout.WarmupsListener Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun warmupsGenerated(warmups: ArrayList<Exercise>) {
        setupPager(warmups)
    }

    /* Setup fragments with page with icons for the tab bar */
    private fun setupPager(warmups: ArrayList<Exercise>) {
        layInflater = layoutInflater
        viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : ViewPagerOnTabSelectedListener(viewPager) {
            var icon: Drawable? = null
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
            exercises,
            warmups
        )
        viewPager.currentItem = tabLayout.tabCount - 2
        //        viewPager.setOffscreenPageLimit(3);
    }

    //CurrWorkout.WarmupsListener//Override//////////////////////////////////////////////////////////
    /* Creates horizontal recycler view lists of  set#, reps, weights for each exercise and adds
     * dynamically to the view.
     * Called in ExercisesList and WarmupsList
     */
    fun displaySets(
        setsType: SetsType,
        exercise: Exercise,
        llSets: LinearLayout
    ) {
        layInflater = layoutInflater

        // Add the listView layout which containsExercise a textView and a recyclerVIew
        setsView = layInflater.inflate(R.layout.part_horizontal_rv, llSets, false)
        setsView.setLayoutParams(lp)
        //        setsView.setId(id);
        llSets.addView(setsView, 0)

        // ExerciseSet the title in the textView within the listView layout above
        tvExerciseName = setsView.findViewById(R.id.tv_exercise_name)
        tvExerciseName.setText(exercise.name)
        setList = setsView.findViewById(R.id.rv_exercise_sets)
        setList.setHasFixedSize(true)
        setList.setLayoutManager(
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        )
        adapter = SetsAdapter(exercise.getSetsList())
        setList.setAdapter(adapter)
        when (setsType) {
            SetsType.MAIN_SET -> mainAdapters.add(adapter)
            SetsType.WARMUP_SET -> warmupAdapter.add(adapter)
        }
    }

    companion object {
        // Constants
        // --------------------------------------------------------------------------------------------
        const val EXTRA_HOW_TO_VID =
            "ca.judacribz.gainzassist.activities.start_workout.EXTRA_HOW_TO_VID"
    }
}
