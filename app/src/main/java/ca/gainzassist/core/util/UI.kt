package ca.gainzassist.core.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import ca.gainzassist.R
import ca.gainzassist.data.local.preferences.Preferences.getThemePref

object UI {

    private var backPressedTwice = false

    fun handleBackButton(context: Context) {
        if (backPressedTwice) {
            context.startActivity(
                Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_HOME)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } else {
            backPressedTwice = true
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({ backPressedTwice = false }, 2000)
        }
    }

    fun setInitTheme(act: Activity) {
        val col = getThemePref(act)
        if (col != null) {
            if (col == "blue") {
                act.setTheme(R.style.BlueTheme)
            } else if (col == "green") {
                act.setTheme(R.style.GreenTheme)
            }
        }
    }
}
