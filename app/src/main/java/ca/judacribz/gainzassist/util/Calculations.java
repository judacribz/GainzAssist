package ca.judacribz.gainzassist.util;

import android.content.Context;
import android.util.DisplayMetrics;

public class Calculations {

    public static int getNumColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float width = displayMetrics.widthPixels / displayMetrics.density;

        return (int) (width / 180);
    }
}
