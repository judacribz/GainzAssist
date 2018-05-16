package ca.judacribz.gainzassist.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import ca.judacribz.gainzassist.R;

public class UI {

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    private static boolean backPressedTwice = false;
    // --------------------------------------------------------------------------------------------


    /* Exits app and goes to home screen if back pressed twice from this screen */
    public static void handleBackButton(Context context) {
        if (backPressedTwice) {
            Intent exitApp = new Intent(Intent.ACTION_MAIN);
            exitApp.addCategory(Intent.CATEGORY_HOME);
            exitApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(exitApp);
        } else {
            backPressedTwice = true;
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    backPressedTwice = false;
                }
            }, 2000);
        }
    }

    /* Sets the title partial_titlebar using a string id */
    public static String setToolbar(Activity activity, int titleId, boolean setBackArrow) {
        return setToolbar(activity, activity.getResources().getString(titleId), setBackArrow);
    }

    /* Sets the title partial_titlebar for the Activity */
    public static String setToolbar(Activity activity, String title, boolean setBackArrow) {
        // Set the partial_titlebar to the activity
        ((AppCompatActivity) activity).setSupportActionBar(
                (Toolbar) activity.findViewById(R.id.toolbar));


        ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
        if (actionBar != null) {

            actionBar.setDisplayShowTitleEnabled(false);
            if (setBackArrow) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
            }
        }

        // Set the title for the partial_titlebar
        ((TextView) activity.findViewById(R.id.title)).setText(title);

        return title;
    }

    /* Sets the spinner with the given array resource in the Activity */
    public static void setSpinnerWithArray(Activity act, int arrResId, Spinner spr) {

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(act,
                                                arrResId,
                                                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spr.setAdapter(adapter);
    }


    private static String getText(TextView tv) {
        return tv.getText().toString();
    }

    public static int getInt(TextView tv) {
        return Integer.valueOf(getText(tv));
    }

    public static float getFloat(TextView tv) {
        return Float.valueOf(getText(tv));
    }


    public static boolean validateForm(Activity act, EditText[] ets) {
        boolean isValid = true;
        String text;

        for (EditText et : ets) {
            text = et.getText().toString().trim();

            if (text.isEmpty()) {
                et.setError(act.getString(R.string.err_required));
                isValid = false;
            }
        }

        return isValid;
    }



//        /* Checks to see if the database exists */
//        public static boolean exists(Context context) {
//            File dbFile = new File(context.getDatabasePath(
//                    context.getResources().getString(
//                            R.string.workouts
//                    ).toLowerCase()
//            ).toString());
//
//            return dbFile.exists();
//        }

}
