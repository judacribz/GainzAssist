package ca.judacribz.gainzassist.activities.main.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ca.judacribz.gainzassist.BuildConfig
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.authentication.Login
import ca.judacribz.gainzassist.activities.main.Main
import ca.judacribz.gainzassist.databinding.FragmentSettingsBinding
import ca.judacribz.gainzassist.models.db.WorkoutViewModel
import ca.judacribz.gainzassist.util.Preferences
import com.google.firebase.auth.FirebaseAuth

class Settings : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        setupAccountSection()
        setupPrivacySupportSection()
        setupAboutSection()

        return binding.root
    }

    private fun setupAccountSection() {
        val email = FirebaseAuth.getInstance().currentUser?.email
            ?: context?.let { Preferences.getEmailPref(it) }

        if (email != null) {
            binding.tvSignedInAs.text = getString(R.string.settings_signed_in_as, email)
        } else {
            binding.tvSignedInAs.text = getString(R.string.settings_email_unavailable)
        }

        binding.btnSignOut.setOnClickListener { logout() }
    }

    private fun setupPrivacySupportSection() {
        binding.btnPrivacyPolicy.setOnClickListener {
            val url = getString(R.string.privacy_policy_url)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Browser not available", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDataDeletionSupport.setOnClickListener {
            val email = getString(R.string.support_email)
            val subject = getString(R.string.data_deletion_email_subject)
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, subject)
            }
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Mail app not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAboutSection() {
        binding.tvAppVersion.text = getString(
            R.string.settings_version,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        )
    }

    private fun logout() {
        val logoutIntent = Intent(activity, Login::class.java).apply {
            putExtra(Main.EXTRA_LOGOUT_USER, true)
        }
        startActivity(logoutIntent)

        activity?.let {
            ViewModelProvider(it).get(WorkoutViewModel::class.java).deleteAllWorkouts()
            it.finish()
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(): Settings {
            return Settings()
        }
    }
}
