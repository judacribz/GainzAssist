package ca.judacribz.gainzassist.util;

import android.content.Context;
import android.util.DisplayMetrics;

public class Calculations {

    // Constants
    // --------------------------------------------------------------------------------------------
    private final static float GRID_ITEM_WIDTH = 120;
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------
    /* Gets the optimum number of columns for a GridLayout based on the screen width and density */
    public static int getNumColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float width = displayMetrics.widthPixels / displayMetrics.density;

        return (int) (width / GRID_ITEM_WIDTH);
    }
}
