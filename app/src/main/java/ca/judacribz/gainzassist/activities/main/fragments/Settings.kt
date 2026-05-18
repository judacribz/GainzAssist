package ca.judacribz.gainzassist.activities.main.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.judacribz.gainzassist.databinding.FragmentSettingsBinding
import ca.judacribz.gainzassist.util.Preferences.setTheme

class Settings : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        binding.btnBlue.setOnClickListener { setBlue() }
        binding.btnGreen.setOnClickListener { setGreen() }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
