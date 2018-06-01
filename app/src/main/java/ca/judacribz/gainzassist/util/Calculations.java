package ca.judacribz.gainzassist.util;

import android.content.Context;
import android.util.DisplayMetrics;

public class Calculations {

    // Constants
    // --------------------------------------------------------------------------------------------
    private final static float GRID_ITEM_WIDTH = 120;
    private final static float BAECHELE_CONST = 0.033333f;
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

    /* Using Baechele formula: 1RM = Weight * [(0.033333 * Number of Reps) + 1] */
    public static float getOneRepMax(int reps, float weight) {
        return weight * ((BAECHELE_CONST * reps) + 1);
    }
}
