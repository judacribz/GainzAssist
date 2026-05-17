package ca.judacribz.gainzassist.util

import android.content.Context

object Calculations {
    @JvmStatic
    fun dpToPix(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}
