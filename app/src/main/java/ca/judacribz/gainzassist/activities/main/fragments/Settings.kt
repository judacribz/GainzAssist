package ca.judacribz.gainzassist.activities.main.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.OnClick
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.util.Preferences.setTheme

class Settings : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        ButterKnife.bind(this, view)
        return view
    }

    @OnClick(R.id.btnBlue)
    fun setBlue() {
        setTheme(context, "blue")
        activity?.recreate()
    }

    @OnClick(R.id.btnGreen)
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
