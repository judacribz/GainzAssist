package ca.gainzassist.activities.main

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ca.gainzassist.BuildConfig
import ca.gainzassist.R
import ca.gainzassist.activities.add_workout.Summary
import ca.gainzassist.activities.add_workout.WorkoutEntry
import ca.gainzassist.activities.authentication.Login
import ca.gainzassist.activities.main.fragments.settings.SettingsUiState
import ca.gainzassist.activities.start_workout.StartWorkout
import ca.gainzassist.interfaces.OnWorkoutReceivedListener
import ca.gainzassist.models.Workout
import ca.gainzassist.models.db.WorkoutViewModel
import ca.gainzassist.util.Preferences
import ca.gainzassist.util.UI.handleBackButton
import ca.gainzassist.util.UI.setInitTheme
import ca.gainzassist.util.UI.setToolbar
import com.google.firebase.auth.FirebaseAuth
import org.parceler.Parcels
import java.util.ArrayList

class Main : AppCompatActivity(), SearchView.OnQueryTextListener, OnWorkoutReceivedListener {

    companion object {
        const val EXTRA_LOGOUT_USER = "ca.gainzassist.EXTRA_LOGOUT_USER"
        const val EXTRA_WORKOUT = "ca.gainzassist.activities.main.Main.EXTRA_WORKOUT"
        const val EXTRA_CALLING_ACTIVITY = "ca.gainzassist.activities.main.Main.EXTRA_CALLING_ACTIVITY"
        const val EXERCISES_ENTRY = "ca.gainzassist.activities.main.Main.EXERCISES_ENTRY"
        private const val MAIL_TO = "mailto:"
    }

    private var search: MenuItem? = null
    private var addWorkout: MenuItem? = null

    // Compose states
    private var selectedTab by mutableStateOf(MainTab.WORKOUTS)
    private var allWorkouts by mutableStateOf<List<Workout>>(emptyList())
    private var currentQuery by mutableStateOf("")
    private var selectedWorkoutName by mutableStateOf<String?>(null)

    // Pending intent routing
    private var pendingIntent: Intent? = null
    private var pendingExtraKey: String? = null

    private lateinit var workoutViewModel: WorkoutViewModel
    private var composeView: ComposeView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInitTheme(this)

        workoutViewModel = ViewModelProvider(this).get(WorkoutViewModel::class.java)
        workoutViewModel.allWorkouts.observe(this, Observer { workouts ->
            allWorkouts = workouts
        })

        val rootView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val toolbarView = layoutInflater.inflate(R.layout.part_title_bar, this, false)
            toolbarView.id = R.id.toolbar
            addView(toolbarView)

            composeView = ComposeView(this@Main).apply {
                setContent {
                    val filteredWorkouts = allWorkouts.filter { 
                        it.name.orEmpty().lowercase().contains(currentQuery.lowercase()) 
                    }
                    val workoutNames = filteredWorkouts.mapNotNull { it.name }

                    val incomplete = Preferences.getIncompleteWorkouts(this@Main)
                    val resumeWorkoutNames = if (incomplete != null) {
                        allWorkouts.filter { incomplete.contains(it.name) }.mapNotNull { it.name }
                    } else {
                        emptyList()
                    }

                    MainScreen(
                        uiState = MainUiState(
                            selectedTab = selectedTab,
                            resumeWorkoutNames = resumeWorkoutNames,
                            workoutNames = workoutNames,
                            selectedWorkoutName = selectedWorkoutName,
                            settingsUiState = getSettingsUiState()
                        ),
                        onTabSelected = { tab ->
                            selectedTab = tab
                            updateMenuVisibility()
                        },
                        onResumeWorkoutClick = { workoutName ->
                            pendingIntent = Intent(this@Main, StartWorkout::class.java)
                            pendingExtraKey = EXTRA_WORKOUT
                            workoutViewModel.getWorkoutFromName(this@Main, workoutName)
                        },
                        onWorkoutClick = { workoutName ->
                            pendingIntent = Intent(this@Main, StartWorkout::class.java)
                            pendingExtraKey = EXTRA_WORKOUT
                            workoutViewModel.getWorkoutFromName(this@Main, workoutName)
                        },
                        onWorkoutLongClick = { workoutName ->
                            selectedWorkoutName = workoutName
                        },
                        onDismissWorkoutDialog = {
                            selectedWorkoutName = null
                        },
                        onEditWorkout = { workoutName ->
                            val editIntent = Intent(this@Main, Summary::class.java)
                            editIntent.putExtra(EXTRA_CALLING_ACTIVITY, Summary.CallingActivity.WORKOUTS_LIST)
                            pendingIntent = editIntent
                            pendingExtraKey = Summary.EXTRA_WORKOUT
                            workoutViewModel.getWorkoutFromName(this@Main, workoutName)
                            selectedWorkoutName = null
                        },
                        onDeleteWorkout = { workoutName ->
                            workoutViewModel.deleteWorkout(workoutName)
                            selectedWorkoutName = null
                        },
                        onSettingsSignOutClick = { logout() },
                        onPrivacyPolicyClick = { openPrivacyPolicy() },
                        onAccountDeletionClick = { openAccountDeletion() },
                        onContactSupportClick = { contactSupport() }
                    )
                }
            }
            addView(composeView, LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        }

        setContentView(rootView)
        setToolbar(this, R.string.app_name, false)
    }

    override fun onBackPressed() {
        handleBackButton(this)
    }

    override fun onCreateOptionsMenu(mainMenu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, mainMenu)
        search = mainMenu.findItem(R.id.act_search)
        val searchView = search?.actionView as? SearchView
        searchView?.setOnQueryTextListener(this)
        
        addWorkout = mainMenu.findItem(R.id.act_add_workout)
        
        updateMenuVisibility()
        return super.onCreateOptionsMenu(mainMenu)
    }

    private fun updateMenuVisibility() {
        search?.let {
            if (it.isActionViewExpanded) {
                it.collapseActionView()
            }
        }

        when (selectedTab) {
            MainTab.SETTINGS -> {
                search?.isVisible = false
                addWorkout?.isVisible = false
            }
            MainTab.RESUME -> {
                search?.isVisible = true
                addWorkout?.isVisible = false
            }
            MainTab.WORKOUTS -> {
                search?.isVisible = true
                addWorkout?.isVisible = true
            }
        }
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        currentQuery = newText ?: ""
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.act_add_workout -> startActivity(Intent(this, WorkoutEntry::class.java))
            R.id.act_logout -> logout()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onWorkoutsReceived(workout: Workout) {
        val intent = pendingIntent
        val extraKey = pendingExtraKey
        if (intent != null && extraKey != null) {
            intent.putExtra(extraKey, Parcels.wrap(workout))
            startActivity(intent)
        }
        pendingIntent = null
        pendingExtraKey = null
    }

    private fun getSettingsUiState(): SettingsUiState {
        val email = FirebaseAuth.getInstance().currentUser?.email
            ?: Preferences.getEmailPref(this)

        val signedInText = if (email != null) {
            getString(R.string.settings_signed_in_as, email)
        } else {
            getString(R.string.settings_email_unavailable)
        }

        val versionText = getString(
            R.string.settings_version,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        )

        return SettingsUiState(
            signedInText = signedInText,
            versionText = versionText
        )
    }

    private fun openPrivacyPolicy() {
        val url = getString(R.string.privacy_policy_url)
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "Browser not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAccountDeletion() {
        val url = getString(R.string.account_deletion_url)
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "Browser not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun contactSupport() {
        val email = getString(R.string.support_email)
        val subject = getString(R.string.support_email_subject)
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = MAIL_TO.toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "Mail app not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logout() {
        val logoutIntent = Intent(this, Login::class.java).apply {
            putExtra(EXTRA_LOGOUT_USER, true)
        }
        startActivity(logoutIntent)
        workoutViewModel.deleteAllWorkouts()
        finish()
    }
}
