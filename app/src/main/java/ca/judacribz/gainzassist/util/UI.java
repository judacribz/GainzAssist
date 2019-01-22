package ca.judacribz.gainzassist.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.*;
import butterknife.ButterKnife;

import ca.judacribz.gainzassist.R;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import io.alterac.blurkit.BlurLayout;

import static ca.judacribz.gainzassist.util.Preferences.getThemePref;

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
       layout="@layout/part_title_bar"/>
     */
    public static void setInitView(Activity act, int layoutId, int titleId, boolean setBackArrow) {
        setInitView(act, layoutId, act.getResources().getString(titleId), setBackArrow);
    }
    public static void setInitView(Activity act, int layoutId, String title, boolean setBackArrow) {
        setInitTheme(act);
        act.setContentView(layoutId);
        ButterKnife.bind(act);
        if (title != null) {
            setToolbar((AppCompatActivity) act, title, setBackArrow);
        }
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
        // ExerciseSet the part_title_bar to the activity
        act.setSupportActionBar((Toolbar) act.findViewById(R.id.toolbar));


        ActionBar actionBar = act.getSupportActionBar();
        if (actionBar != null) {

            actionBar.setDisplayShowTitleEnabled(false);

            if (setBackArrow) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
            }
        }

        // ExerciseSet the title for the part_title_bar
        ((TextView) act.findViewById(R.id.title)).setText(title);

        return title;
    }

    public static void setInitTheme(Activity act) {
        String col = getThemePref(act);

        if (col != null) {
            if (col.equals("blue")) {
                act.setTheme(R.style.BlueTheme);
            }    else if (col.equals("green")) {

                act.setTheme(R.style.GreenTheme);
            }
        }
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
        return spr.getSelectedItem().toString().toLowerCase();
    }


    /* Gets integer value of text from EditText element */
    public static int getTextInt(TextView tv) {
        return Integer.valueOf(getTextString(tv));
    }

    /* Gets float value of text from EditText element */
    public static float getTextFloat(EditText et) {
        return Float.valueOf(getTextString(et));
    }

    public static String getTextString(TextView tv) {
        return tv.getText().toString().trim();
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

        return (int) (dpWidth / 180);
    }


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


    public static void handleFocusLeft(EditText et, Number min, Number res) {
        String etStr = getTextString(et);

        if (etStr.isEmpty()) {
            et.setText(String.valueOf(res));
        } else if(Float.valueOf(etStr) < min.floatValue()) {
            et.setText(String.valueOf(min));
        }
    }


    /* Text changed handling */
    public static void setVisibleIfDisabled(View view) {
        if (!view.isEnabled()) {
            view.setEnabled(true);
            view.setVisibility(View.VISIBLE);
        }
    }

    public static Number handleNumChanged(View  view, String str, Number min) {
        Number num = (str.isEmpty()) ? min : Float.valueOf(str);

        if (num.floatValue() <= min.floatValue()) {
            setGoneIfEnabled(view);
        }

        return num;
    };

    public static void setGoneIfEnabled(View view) {
        if (view.isEnabled()) {
            view.setEnabled(false);
            view.setVisibility(View.GONE);
        }
    }

    public static void setText(EditText view, Number num) {
        view.setText(String.valueOf(num));
    }


    /* Progress display */
    public static class ProgressHandler extends Handler {

        private static String DOT = "...";

        ProgressDialog progress;
        BlurLayout blurLayout;
        String msg, newMsg;
        int count = 0;

        public void setProgress(Context context, String msg, BlurLayout blurLayout) {
            this.progress = new ProgressDialog(context);
            this.blurLayout = blurLayout;
            this.msg = msg + DOT;

            this.progress.setMessage(this.msg);
            this.progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }

        public  void setTitle(String title) {
            this.progress.setTitle(title);
        }

        public void show() {
            if (this.progress != null) {
                this.progress.show();
                if (this.blurLayout != null) {
                    blurLayout.setVisibility(View.VISIBLE);
                    blurLayout.setTop(0);
                    blurLayout.startBlur();
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (progress.isShowing()) {
                                Thread.sleep(10);
                                progress.setMessage(msg.substring(0, msg.length() - 1 - (++count % 3)));
                                blurLayout.pauseBlur();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }

        public void dismiss() {
            if (this.progress != null) {
                this.progress.dismiss();
                this.blurLayout.pauseBlur();
                this.blurLayout.setVisibility(View.GONE);
            }
        }
    }

}
