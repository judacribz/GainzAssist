package ca.judacribz.gainzassist.activities.main.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.judacribz.gainzassist.databinding.FragmentSettingsBinding
import ca.judacribz.gainzassist.util.Preferences.setTheme

class Settings : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        binding.btnBlue.setOnClickListener { setBlue() }
        binding.btnGreen.setOnClickListener { setGreen() }

        return binding.root
    }

    fun setBlue() {
        setTheme(context, "blue")
        activity?.recreate()
    }

    fun setGreen() {
        setTheme(context, "green")
        activity?.recreate()
    }

    companion object {
        @JvmStatic
        fun getInstance(): Settings {
            return Settings()
        }
    }
}
