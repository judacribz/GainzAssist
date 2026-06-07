package ca.gainzassist.activities.main

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.gainzassist.BuildConfig
import ca.gainzassist.R
import ca.gainzassist.activities.add_workout.Summary
import ca.gainzassist.activities.add_workout.WorkoutEntry
import ca.gainzassist.activities.authentication.Login
import ca.gainzassist.activities.main.fragments.settings.SettingsUiState
import ca.gainzassist.activities.start_workout.StartWorkout
import ca.gainzassist.presentation.main.MainViewModel
import ca.gainzassist.presentation.main.MainViewModelEvent
import ca.gainzassist.ui.components.MainTopBar
import ca.gainzassist.util.Preferences
import ca.gainzassist.util.UI.handleBackButton
import ca.gainzassist.util.UI.setInitTheme
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.parceler.Parcels

class Main : AppCompatActivity() {

    companion object {
        const val EXTRA_LOGOUT_USER = "ca.gainzassist.EXTRA_LOGOUT_USER"
        const val EXTRA_WORKOUT = "ca.gainzassist.activities.main.Main.EXTRA_WORKOUT"
        const val EXTRA_CALLING_ACTIVITY = "ca.gainzassist.activities.main.Main.EXTRA_CALLING_ACTIVITY"
        const val EXERCISES_ENTRY = "ca.gainzassist.activities.main.Main.EXERCISES_ENTRY"
        private const val MAIL_TO = "mailto:"
    }

    private val mainViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInitTheme(this)

        setContent {
            val state by mainViewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                mainViewModel.events.collect { event ->
                    when (event) {
                        is MainViewModelEvent.AddWorkout -> openWorkoutEntry()
                        is MainViewModelEvent.EditWorkout -> {
                            val editIntent = Intent(this@Main, Summary::class.java).apply {
                                putExtra(Summary.EXTRA_CALLING_ACTIVITY, Summary.CallingActivity.WORKOUTS_LIST)
                                putExtra(Summary.EXTRA_WORKOUT, Parcels.wrap(event.workout))
                            }
                            startActivity(editIntent)
                        }
                        is MainViewModelEvent.Error -> {
                            Toast.makeText(this@Main, event.message, Toast.LENGTH_SHORT).show()
                        }
                        MainViewModelEvent.LoggedOut -> {
                            val logoutIntent = Intent(this@Main, Login::class.java).apply {
                                putExtra(EXTRA_LOGOUT_USER, true)
                            }
                            startActivity(logoutIntent)
                            finish()
                        }
                        is MainViewModelEvent.StartWorkout -> {
                            val intent = Intent(this@Main, StartWorkout::class.java).apply {
                                putExtra(EXTRA_WORKOUT, Parcels.wrap(event.workout))
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
                    onSearchQueryChange = { mainViewModel.onSearchQueryChanged(it) },
                    onSearchClick = { mainViewModel.onSearchExpanded() },
                    onCloseSearchClick = { mainViewModel.onSearchClosed() },
                    onAddWorkoutClick = { mainViewModel.onAddWorkoutClicked() },
                    onLogoutClick = { mainViewModel.onLogoutClicked() }
                )

                MainScreen(
                    uiState = MainUiState(
                        selectedTab = state.selectedTab,
                        resumeWorkoutNames = state.resumeWorkoutNames,
                        workoutNames = state.filteredWorkoutNames,
                        selectedWorkoutName = state.selectedWorkoutName,
                        settingsUiState = getSettingsUiState()
                    ),
                    onTabSelected = { tab -> mainViewModel.onTabSelected(tab) },
                    onResumeWorkoutClick = { workoutName -> mainViewModel.onResumeWorkoutClicked(workoutName) },
                    onWorkoutClick = { workoutName -> mainViewModel.onWorkoutClicked(workoutName) },
                    onWorkoutLongClick = { workoutName -> mainViewModel.onWorkoutLongClicked(workoutName) },
                    onDismissWorkoutDialog = { mainViewModel.onDismissWorkoutDialog() },
                    onEditWorkout = { workoutName -> mainViewModel.onEditWorkoutClicked(workoutName) },
                    onDeleteWorkout = { workoutName -> mainViewModel.onDeleteWorkoutClicked(workoutName) },
                    onSettingsSignOutClick = { mainViewModel.onLogoutClicked() },
                    onPrivacyPolicyClick = { openPrivacyPolicy() },
                    onAccountDeletionClick = { openAccountDeletion() },
                    onContactSupportClick = { contactSupport() }
                )
            }
        }
    }

    private fun openWorkoutEntry() {
        startActivity(Intent(this, WorkoutEntry::class.java))
    }

    override fun onBackPressed() {
        handleBackButton(this)
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
}
