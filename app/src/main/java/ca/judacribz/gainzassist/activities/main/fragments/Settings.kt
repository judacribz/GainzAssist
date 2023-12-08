package ca.judacribz.gainzassist.activities.main.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import butterknife.OnClick
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.main.Main
import ca.judacribz.gainzassist.util.Preferences

// --------------------------------------------------------------------------------------------
// ######################################################################################### //
// WarmupsList Constructor/Instance                                                        //
// ######################################################################################### //
class Settings : Fragment() {
    // --------------------------------------------------------------------------------------------
    // Global Vars
    // --------------------------------------------------------------------------------------------
    var act: Main? = null

    // UI Elements
    var btnWorkouts: Button? = null

    // ######################################################################################### //
    // Fragment Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun onAttach(context: Context) {
        super.onAttach(context)
        act = context as Main
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_settings, container, false)
        ButterKnife.bind(this, view)
        return view
    }

    //Fragment//Override///////////////////////////////////////////////////////////////////////////
    // Click Handling
    // ============================================================================================
    @OnClick(R.id.btnBlue)
    fun setBlue() {
        Preferences.setTheme(this.context, "blue")
        act?.setTheme(R.style.BlueTheme)
        act!!.recreate()
    }

    @OnClick(R.id.btnGreen)
    fun setGreen() {
        Preferences.setTheme(this.context, "green")
        act?.setTheme(R.style.GreenTheme)
        act!!.recreate()
    } //=Click=Handling==============================================================================

    companion object {
        val instance = Settings()
    }
}
