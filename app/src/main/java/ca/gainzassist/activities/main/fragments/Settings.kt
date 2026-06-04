package ca.gainzassist.activities.main.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ca.gainzassist.BuildConfig
import ca.gainzassist.R
import ca.gainzassist.activities.authentication.Login
import ca.gainzassist.activities.main.Main
import ca.gainzassist.activities.main.fragments.settings.SettingsScreen
import ca.gainzassist.activities.main.fragments.settings.SettingsUiState
import ca.gainzassist.models.db.WorkoutViewModel
import ca.gainzassist.util.Preferences
import com.google.firebase.auth.FirebaseAuth

class Settings : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SettingsScreen(
                    uiState = getSettingsUiState(),
                    onSignOutClick = { logout() },
                    onPrivacyPolicyClick = { openPrivacyPolicy() },
                    onAccountDeletionClick = { openAccountDeletion() },
                    onContactSupportClick = { contactSupport() }
                )
            }
        }
    }

    private fun getSettingsUiState(): SettingsUiState {
        val email = FirebaseAuth.getInstance().currentUser?.email
            ?: context?.let { Preferences.getEmailPref(it) }

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
            Toast.makeText(context, "Browser not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAccountDeletion() {
        val url = getString(R.string.account_deletion_url)
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "Browser not available", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, "Mail app not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logout() {
        val logoutIntent = Intent(activity, Login::class.java).apply {
            putExtra(Main.EXTRA_LOGOUT_USER, true)
        }
        startActivity(logoutIntent)

        activity?.let {
            ViewModelProvider(it)[WorkoutViewModel::class.java].deleteAllWorkouts()
            it.finish()
        }
    }

    companion object {
        private const val MAIL_TO = "mailto:"

        @JvmStatic
        fun getInstance(): Settings {
            return Settings()
        }
    }
}
