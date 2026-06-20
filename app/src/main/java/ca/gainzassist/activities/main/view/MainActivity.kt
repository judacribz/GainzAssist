package ca.gainzassist.activities.main.view

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.activity.addCallback
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.gainzassist.BuildConfig
import ca.gainzassist.R
import ca.gainzassist.activities.add_workout.summary.view.CallingActivity
import ca.gainzassist.activities.add_workout.summary.view.SummaryActivity
import ca.gainzassist.activities.add_workout.workout_entry.view.WorkoutEntryActivity
import ca.gainzassist.activities.authentication.login.view.LoginActivity
import ca.gainzassist.activities.base.GainzBaseActivity
import ca.gainzassist.activities.main.MainViewModel
import ca.gainzassist.activities.main.MainViewModelEvent
import ca.gainzassist.activities.main.view.tab_screens.SettingsUiState
import ca.gainzassist.activities.start_workout.view.StartWorkoutActivity
import ca.gainzassist.core.util.UI
import ca.gainzassist.data.local.preferences.Preferences
import ca.gainzassist.ui.components.MainTopBar
import ca.gainzassist.ui.components.MainTopBarActions
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : GainzBaseActivity() {

    private val mainViewModel: MainViewModel by viewModel()

    @Composable
    override fun InnerContent() {
        val state by mainViewModel.state.collectAsStateWithLifecycle()
        LaunchedEffect(Unit) {
            mainViewModel.events.collect { event ->
                when (event) {
                    is MainViewModelEvent.AddWorkout -> openWorkoutEntry()
                    is MainViewModelEvent.EditWorkout -> {
                        val editIntent =
                            Intent(this@MainActivity, SummaryActivity::class.java).apply {
                                putExtra(
                                    SummaryActivity.EXTRA_CALLING_ACTIVITY,
                                    CallingActivity.WORKOUTS_LIST
                                )
                                putExtra(
                                    SummaryActivity.EXTRA_WORKOUT,
                                    event.workout
                                )
                            }
                        startActivity(editIntent)
                    }

                    is MainViewModelEvent.Error -> {
                        Toast.makeText(this@MainActivity, event.message, Toast.LENGTH_SHORT)
                            .show()
                    }

                    MainViewModelEvent.LoggedOut -> {
                        val logoutIntent =
                            Intent(this@MainActivity, LoginActivity::class.java).apply {
                                putExtra(EXTRA_LOGOUT_USER, true)
                            }
                        startActivity(logoutIntent)
                        finish()
                    }

                    is MainViewModelEvent.StartWorkout -> {
                        val intent = Intent(
                            /* packageContext = */ this@MainActivity,
                            /* cls = */ StartWorkoutActivity::class.java
                        ).apply {
                            putExtra(EXTRA_WORKOUT, event.workout)
                        }
                        startActivity(intent)
                    }
                }
            }
        }
        Column(Modifier.fillMaxSize()) {
            MainTopBar(
                selectedTab = state.selectedTab,
                isSearchExpanded = state.isSearchExpanded,
                searchQuery = state.searchQuery,
                actions = MainTopBarActions(
                    onSearchQueryChange = { mainViewModel.onSearchQueryChanged(it) },
                    onSearchClick = { mainViewModel.onSearchExpanded() },
                    onCloseSearchClick = { mainViewModel.onSearchClosed() },
                    onAddWorkoutClick = { mainViewModel.onAddWorkoutClicked() },
                    onLogoutClick = { mainViewModel.onLogoutClicked() }
                )
            )
            MainScreen(
                uiState = MainUiState(
                    selectedTab = state.selectedTab,
                    resumeWorkoutNames = state.resumeWorkoutNames,
                    workoutNames = state.filteredWorkoutNames,
                    selectedWorkoutName = state.selectedWorkoutName,
                    settingsUiState = getSettingsUiState()
                ),
                actions = MainScreenActions(
                    onTabSelected = { tab -> mainViewModel.onTabSelected(tab) },
                    onResumeWorkoutClick = { workoutName ->
                        mainViewModel.onResumeWorkoutClicked(workoutName)
                    },
                    onWorkoutClick = { workoutName -> mainViewModel.onWorkoutClicked(workoutName) },
                    onWorkoutLongClick = { workoutName ->
                        mainViewModel.onWorkoutLongClicked(workoutName)
                    },
                    onDismissWorkoutDialog = { mainViewModel.onDismissWorkoutDialog() },
                    onEditWorkout = { workoutName -> mainViewModel.onEditWorkoutClicked(workoutName) },
                    onDeleteWorkout = { workoutName ->
                        mainViewModel.onDeleteWorkoutClicked(workoutName)
                    },
                    onSettingsSignOutClick = { mainViewModel.onLogoutClicked() },
                    onPrivacyPolicyClick = { openPrivacyPolicy() },
                    onAccountDeletionClick = { openAccountDeletion() },
                    onContactSupportClick = { contactSupport() }
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.refreshResumeWorkouts()
    }

    override fun onBeforeSetContent() {
        onBackPressedDispatcher.addCallback(this) {
            UI.handleBackButton(this@MainActivity)
        }
    }

    private fun openWorkoutEntry() = startActivity(Intent(this, WorkoutEntryActivity::class.java))

    private fun getSettingsUiState(): SettingsUiState {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: Preferences.getEmailPref(this)
        return SettingsUiState(
            signedInText = if (email != null) {
                getString(R.string.settings_signed_in_as, email)
            } else {
                getString(R.string.settings_email_unavailable)
            },
            versionText = getString(
                R.string.settings_version,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE
            )
        )
    }

    private fun openPrivacyPolicy() {
        val url = getString(R.string.privacy_policy_url)
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                /* context = */ this,
                /* text = */ getString(R.string.err_browser_unavailable),
                /* duration = */ Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openAccountDeletion() {
        val url = getString(R.string.account_deletion_url)
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                /* context = */ this,
                /* text = */ getString(R.string.err_browser_unavailable),
                /* duration = */ Toast.LENGTH_SHORT
            ).show()
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
            Toast.makeText(
                /* context = */ this,
                /* text = */ getString(R.string.err_mail_unavailable),
                /* duration = */ Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        const val EXTRA_LOGOUT_USER = "ca.gainzassist.EXTRA_LOGOUT_USER"
        const val EXTRA_WORKOUT = "ca.gainzassist.activities.main.Main.EXTRA_WORKOUT"
        private const val MAIL_TO = "mailto:"
    }
}