package ca.judacribz.gainzassist.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

    /* Sets the title toolbar using a string id */
    public static String setToolbar(Activity activity, int titleId, boolean setBackArrow) {
        return setToolbar(activity, activity.getResources().getString(titleId), setBackArrow);
    }

    /* Sets the title toolbar for the Activity */
    public static String setToolbar(Activity activity, String title, boolean setBackArrow) {
        // Set the toolbar to the activity
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

        // Set the title for the toolbar
        ((TextView) activity.findViewById(R.id.title)).setText(title);

        return title;
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
