package ca.judacribz.gainzassist.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;

import ca.judacribz.gainzassist.R;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.google.firebase.database.FirebaseDatabase;

public class UI {

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    private static boolean backPressedTwice = false;
    // --------------------------------------------------------------------------------------------

    /* Sets the content view, title bar and ButterKnife for the given activity
     *
     * (REQUIRED) Include following as top element in activity layout file:
     *
    <include
       android:id="@id/toolbar"
       layout="@layout/partial_title_bar"/>
     */
    public static void setInitView(Activity act, int layoutId, int titleId, boolean setBackArrow) {
        setInitView(act, layoutId, act.getResources().getString(titleId), setBackArrow);
    }
    public static void setInitView(Activity act, int layoutId, String title, boolean setBackArrow) {
        act.setContentView(layoutId);
        ButterKnife.bind(act);
        setToolbar((AppCompatActivity) act, title, setBackArrow);
    }

    /* Exits app and goes to home screen if back pressed twice from this screen */
    public static void handleBackButton(Context context) {
        if (backPressedTwice) {
            context.startActivity(
                    new Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_HOME)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            );
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

    /* Sets up the title bar */
    public static String setToolbar(AppCompatActivity act, int titleId, boolean setBackArrow) {
        return setToolbar(act, act.getResources().getString(titleId), setBackArrow);
    }
    public static String setToolbar(AppCompatActivity act, String title, boolean setBackArrow) {
        // ExerciseSet the partial_title_bar to the activity
        act.setSupportActionBar((Toolbar) act.findViewById(R.id.toolbar));


        ActionBar actionBar = act.getSupportActionBar();
        if (actionBar != null) {

            actionBar.setDisplayShowTitleEnabled(false);

            if (setBackArrow) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
            }
        }

        // ExerciseSet the title for the partial_title_bar
        ((TextView) act.findViewById(R.id.title)).setText(title);

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

    /* Gets string value from EditText element */
    public static String getTextString(Spinner spr) {
        return spr.getSelectedItem().toString();
    }

    public static String getTextString(EditText et) {
        return et.getText().toString().trim();
    }

    /* Gets integer value of text from EditText element */
    public static int getTextInt(EditText et) {
        return Integer.valueOf(getTextString(et));
    }

    /* Gets float value of text from EditText element */
    public static float getTextFloat(EditText et) {
        return Float.valueOf(getTextString(et));
    }

    /* Validates EditTexts to be non-empty or else an error is set */
    public static boolean validateForm(Activity act, EditText[] ets) {
        boolean isValid = true;

        for (EditText et : ets) {
            isValid &= validateFormEntry(act, et);
        }

        return isValid;
    }

    /* Validates an EditText to be non-empty or else an error is set */
    public static boolean validateFormEntry(Activity act, EditText et) {
        String text = et.getText().toString().trim();

        if (text.isEmpty()) {
            et.setError(act.getString(R.string.err_required));
            return false;
        }

        return true;
    }

    public static void clearForm(EditText[] ets) {
        for (EditText et : ets) {
            clearFormEntry(et);
        }
    }

    public static void clearFormEntry(EditText et) {
        et.setText("");
    }

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 180);
        return noOfColumns;
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

    public static Spring setSpring(final View view) {
        // Create a system to run the physics loop for a set of springs.
        SpringSystem springSystem = SpringSystem.create();

        // Add a spring to the system.
        Spring spring = springSystem.createSpring();
        // Add a listener to observe the motion of the spring.
        spring.addListener(new SimpleSpringListener() {

            @Override
            public void onSpringUpdate(Spring spring) {
                // You can observe the updates in the spring
                // state by asking its current value in onSpringUpdate.
                float value = (float) SpringUtil.mapValueFromRangeToRange(spring.getCurrentValue(), 0, 1, 1, 0);
//                float value = (float) spring.getCurrentValue();
//                float scale = 1f - (value * 0.9f);
                view.setScaleX(value);
                view.setScaleY(value);

                if (spring.isAtRest()) {
                    spring.setEndValue(0);
                }
            }
        });

        // Set the spring in motion; moving from 0 to 1
        return spring;
    }
}
