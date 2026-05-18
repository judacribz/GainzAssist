package ca.judacribz.gainzassist.activities.main

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.add_workout.WorkoutEntry
import ca.judacribz.gainzassist.activities.authentication.Login
import ca.judacribz.gainzassist.activities.main.fragments.Resume
import ca.judacribz.gainzassist.activities.main.fragments.Settings
import ca.judacribz.gainzassist.activities.main.fragments.Workouts
import ca.judacribz.gainzassist.adapters.WorkoutPagerAdapter
import ca.judacribz.gainzassist.databinding.ActivityMainBinding
import ca.judacribz.gainzassist.interfaces.OnWorkoutReceivedListener
import ca.judacribz.gainzassist.models.Workout
import ca.judacribz.gainzassist.models.db.WorkoutViewModel
import ca.judacribz.gainzassist.util.UI.ProgressHandler
import ca.judacribz.gainzassist.util.UI.handleBackButton
import ca.judacribz.gainzassist.util.UI.setInitTheme
import ca.judacribz.gainzassist.util.UI.setToolbar
import com.miguelcatalan.materialsearchview.MaterialSearchView
import org.parceler.Parcels
import java.util.*

class Main : AppCompatActivity(), MaterialSearchView.OnQueryTextListener, OnWorkoutReceivedListener {

    companion object {
        const val EXTRA_LOGOUT_USER = "ca.judacribz.gainzassist.EXTRA_LOGOUT_USER"
        const val EXTRA_WORKOUT = "ca.judacribz.gainzassist.activities.main.Main.EXTRA_WORKOUT"
        const val EXTRA_CALLING_ACTIVITY = "ca.judacribz.gainzassist.activities.main.Main.EXTRA_CALLING_ACTIVITY"
        const val EXERCISES_ENTRY = "ca.judacribz.gainzassist.activities.main.Main.EXERCISES_ENTRY"
        
        private val progressHandler = ProgressHandler()
    }

    private val FMTS: ArrayList<Fragment> = arrayListOf(
        Resume.getInstance(),
        Workouts.getInstance(),
        Settings.getInstance()
    )

    var tabLayoutOnPageChangeListener: TabLayout.TabLayoutOnPageChangeListener? = null
    private var viewPagerOnTabSelectedListener: ViewPagerOnTabSelectedListener? = null
    var search: MenuItem? = null
    var addWorkout: MenuItem? = null
    var layInflater: LayoutInflater? = null

    var pos: Int = 0

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInitTheme(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setToolbar(this, R.string.app_name, false)

        tabLayoutOnPageChangeListener = TabLayout.TabLayoutOnPageChangeListener(binding.tlayNavbar)
        viewPagerOnTabSelectedListener = ViewPagerOnTabSelectedListener(binding.vpFmtContainer)
    }

    private inner class ViewPagerOnTabSelectedListener(viewPager: ViewPager) :
        TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
        override fun onTabSelected(tab: TabLayout.Tab) {
            super.onTabSelected(tab)
            pos = tab.position
            binding.msvWorkouts.closeSearch()

            if (search != null && addWorkout != null) {
                when (tab.position) {
                    2 -> {
                        search!!.isVisible = false
                        addWorkout!!.isVisible = false
                    }
                    0 -> {
                        addWorkout!!.isVisible = false
                    }
                    1 -> {
                        search!!.isVisible = true
                        addWorkout!!.isVisible = true
                    }
                }
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
            super.onTabUnselected(tab)
        }
    }

    override fun onResume() {
        super.onResume()
        setupPager()
        binding.msvWorkouts.setOnQueryTextListener(this)
        binding.msvWorkouts.setVoiceSearch(true)
    }

    private fun setupPager() {
        layInflater = layoutInflater
        binding.vpFmtContainer.addOnPageChangeListener(tabLayoutOnPageChangeListener!!)
        binding.tlayNavbar.addOnTabSelectedListener(viewPagerOnTabSelectedListener!!)

        binding.vpFmtContainer.adapter = WorkoutPagerAdapter(
            supportFragmentManager,
            FMTS
        )
        binding.vpFmtContainer.currentItem = 1
    }

    override fun onPause() {
        super.onPause()
        binding.msvWorkouts.setOnQueryTextListener(null)
        binding.msvWorkouts.setVoiceSearch(false)
        binding.vpFmtContainer.removeOnPageChangeListener(tabLayoutOnPageChangeListener!!)
        binding.tlayNavbar.removeOnTabSelectedListener(viewPagerOnTabSelectedListener!!)
    }

    override fun onBackPressed() {
        handleBackButton(this)
    }

    override fun onCreateOptionsMenu(mainMenu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, mainMenu)
        search = mainMenu.findItem(R.id.act_search)
        binding.msvWorkouts.setMenuItem(search)
        addWorkout = mainMenu.findItem(R.id.act_add_workout)
        return super.onCreateOptionsMenu(mainMenu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (matches != null && matches.size > 0) {
                val searchWrd = matches[0]
                if (!TextUtils.isEmpty(searchWrd)) {
                    binding.msvWorkouts.setQuery(searchWrd, false)
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onQueryTextChange(newText: String): Boolean {
        (FMTS[1] as Workouts).onQueryTextChange(newText)
        return false
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.act_add_workout -> startActivity(Intent(this, WorkoutEntry::class.java))
            R.id.act_logout -> {
                val logoutIntent = Intent(this, Login::class.java)
                logoutIntent.putExtra(EXTRA_LOGOUT_USER, true)
                startActivity(logoutIntent)
                ViewModelProviders.of(this).get(WorkoutViewModel::class.java).deleteAllWorkouts()
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
            intent.putExtra(extraKey, Parcels.wrap(workout))
            startActivity(intent)
        }
    }
}
