package ca.judacribz.gainzassist.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.Session;
import ca.judacribz.gainzassist.models.Set;
import ca.judacribz.gainzassist.models.Workout;
import com.google.firebase.database.DataSnapshot;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Objects;

public class Helper {
    private static final String EMAIL = "email";

    @SuppressWarnings("deprecation")
    public static boolean isMyServiceRunning(Activity act, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) act.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getEmailFromPref(Context context) {
        return context.getSharedPreferences(
                context.getString(R.string.file_user_info),
                Context.MODE_PRIVATE
        ).getString(EMAIL, null);
    }

    public static void setUserInfoInPref(Activity act, String email, String uid) {
        SharedPreferences sharedPref = act.getSharedPreferences(
                act.getString(R.string.file_user_info),
                Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(EMAIL, email);
        editor.apply();

    }

    public static Workout extractWorkout(DataSnapshot workoutShot) {
        ArrayList<Exercise> exercises = new ArrayList<>();
        Exercise exercise;
        ArrayList<Set> sets;
        Set set;
        String setNum;

        for (DataSnapshot exerciseShot : workoutShot.child("exercises").getChildren()) {

            // Adds sets to exercise object, and add exercise to exercises list
            exercise = exerciseShot.getValue(Exercise.class);
            if (exercise != null) {
                exercises.add(exercise);
            }
        }

        return new Workout(workoutShot.getKey(), exercises);
    }

    public static Workout extractSession(DataSnapshot sessionShot) {


        for (DataSnapshot exerciseShot : sessionShot.child("sets").getChildren()) {

            for (DataSnapshot setShot : exerciseShot.getChildren()) {

                Set set = setShot.getValue(Set.class);
                if (set != null) {
                    set.setSetNumber(Integer.valueOf(Objects.requireNonNull(setShot.getKey())));

                    set.setExerciseName(exerciseShot.getKey());
                    Logger.d(set.getSetNumber() + " " + set.getReps() + " " + set.getWeight());
                }

            }

        }

        return null;
    }
}
