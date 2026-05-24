package ca.gainzassist.util

import android.content.Context
import android.util.TypedValue

object Calculations {
    private const val GRID_ITEM_WIDTH = 120f
    private const val BAECHELE_CONST = 0.033333f

    @JvmStatic
    fun getNumColumns(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels / displayMetrics.density
        return (width / GRID_ITEM_WIDTH).toInt()
    }

    @JvmStatic
    fun getOneRepMax(reps: Int, weight: Float): Float {
        return weight * (BAECHELE_CONST * reps + 1)
    }

    @JvmStatic
    fun dpToPix(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }
}
