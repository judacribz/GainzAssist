package ca.judacribz.gainzassist.activities.main

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.add_workout.WorkoutEntry
import ca.judacribz.gainzassist.activities.authentication.Login
import ca.judacribz.gainzassist.activities.main.fragments.Resume
import ca.judacribz.gainzassist.activities.main.fragments.Settings
import ca.judacribz.gainzassist.activities.main.fragments.Workouts
import ca.judacribz.gainzassist.adapters.WorkoutPagerAdapter
import ca.judacribz.gainzassist.interfaces.OnWorkoutReceivedListener
import ca.judacribz.gainzassist.models.Workout
import ca.judacribz.gainzassist.models.db.WorkoutViewModel
import ca.judacribz.gainzassist.util.UI
import ca.judacribz.gainzassist.util.UI.ProgressHandler
import ca.judacribz.gainzassist.util.UI.handleBackButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import com.google.android.material.tabs.TabLayout.ViewPagerOnTabSelectedListener
import com.miguelcatalan.materialsearchview.MaterialSearchView

class Main : AppCompatActivity(), MaterialSearchView.OnQueryTextListener, OnWorkoutReceivedListener {
    private val FMTS: ArrayList<Fragment> = object : ArrayList<Fragment>() {
        init {
            add(Resume.instance)
            add(Workouts.instance)
            add(Settings.instance)
        }
    }
    var tabLayoutOnPageChangeListener: TabLayoutOnPageChangeListener? = null
    var viewPagerOnTabSelectedListener: ViewPagerOnTabSelectedListener? = null
    var search: MenuItem? = null
    var addWorkout: MenuItem? = null
    var layInflater: LayoutInflater? = null
    var pos = 0

    @BindView(R.id.tlay_navbar)
    lateinit var tabLayout: TabLayout

    @BindView(R.id.vp_fmt_container)
    lateinit var viewPager: ViewPager

    @BindView(R.id.msvWorkouts)
    lateinit var searchView: MaterialSearchView

    // --------------------------------------------------------------------------------------------
    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private val workoutViewModel: WorkoutViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UI.setInitView(this, R.layout.activity_main, R.string.app_name, false)
        tabLayoutOnPageChangeListener = TabLayoutOnPageChangeListener(tabLayout)
        viewPagerOnTabSelectedListener = object : ViewPagerOnTabSelectedListener(viewPager) {
            override fun onTabSelected(tab: TabLayout.Tab) {
                super.onTabSelected(tab)
                pos = tab.position
                searchView!!.closeSearch()
                if (search != null && addWorkout != null) {
                    when (tab.position) {
                        2 -> {
                            search!!.setVisible(false)
                            addWorkout!!.setVisible(false)
                        }

                        0 -> addWorkout!!.setVisible(false)
                        1 -> {
                            search!!.setVisible(true)
                            addWorkout!!.setVisible(true)
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                super.onTabUnselected(tab)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupPager()
        searchView!!.setOnQueryTextListener(this)
        searchView!!.setVoiceSearch(true)
    }

    private fun setupPager() {
        layInflater = layoutInflater
        viewPager!!.addOnPageChangeListener(tabLayoutOnPageChangeListener!!)
        tabLayout!!.addOnTabSelectedListener(viewPagerOnTabSelectedListener!!)
        viewPager!!.adapter = WorkoutPagerAdapter(
                supportFragmentManager,
                FMTS
        )
        viewPager!!.currentItem = 1
    }

    override fun onPause() {
        super.onPause()
        searchView!!.setOnQueryTextListener(null)
        searchView!!.setVoiceSearch(false)
        viewPager!!.removeOnPageChangeListener(tabLayoutOnPageChangeListener!!)
        tabLayout!!.removeOnTabSelectedListener(viewPagerOnTabSelectedListener!!)
    }

    override fun onBackPressed() {
        handleBackButton(this)
    }

    override fun onCreateOptionsMenu(mainMenu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, mainMenu)
        search = mainMenu.findItem(R.id.act_search)
        searchView!!.setMenuItem(search)
        addWorkout = mainMenu.findItem(R.id.act_add_workout)
        return super.onCreateOptionsMenu(mainMenu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            val matches = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (matches != null && matches.size > 0) {
                val searchWrd = matches[0]
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView!!.setQuery(searchWrd, false)
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // MaterialSearchView.OnQueryTextListener Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun onQueryTextChange(newText: String): Boolean {
        (FMTS[1] as Workouts).onQueryTextChange(newText)
        return false
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }


    //MaterialSearchView.OnQueryTextListener//Override/////////////////////////////////////////////
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.act_add_workout -> startActivity(Intent(this, WorkoutEntry::class.java))
            R.id.act_logout -> {
                val logoutIntent = Intent(this, Login::class.java)
                logoutIntent.putExtra(EXTRA_LOGOUT_USER, true)
                startActivity(logoutIntent)
                workoutViewModel?.deleteAllWorkouts()
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onWorkoutsReceived(workout: Workout) {
        var intent: Intent? = null
        var extraKey: String? = null
        when (pos) {
            0 -> {
                intent = (FMTS[0] as Resume).intent
                extraKey = (FMTS[0] as Resume).extraKey
            }

            1 -> {
                intent = (FMTS[1] as Workouts).intent
                extraKey = (FMTS[1] as Workouts).extraKey
            }
        }
        if (intent != null) {
            intent.putExtra(extraKey, workout)
            startActivity(intent)
        }
    }

    companion object {
        // Constants
        // --------------------------------------------------------------------------------------------
        const val EXTRA_LOGOUT_USER = "ca.judacribz.gainzassist.EXTRA_LOGOUT_USER"
        const val EXTRA_WORKOUT = "ca.judacribz.gainzassist.activities.main.Main.EXTRA_WORKOUT"

        // --------------------------------------------------------------------------------------------
        // Global Vars
        // --------------------------------------------------------------------------------------------
        private val progressHandler = ProgressHandler()
    }
}
