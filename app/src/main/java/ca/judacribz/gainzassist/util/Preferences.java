package ca.judacribz.gainzassist.util;

import android.content.Context;
import android.content.SharedPreferences;
import ca.judacribz.gainzassist.R;
import com.orhanobut.logger.Logger;

import java.util.HashSet;
import java.util.Set;

public class Preferences {
    private static final String EMAIL = "email";
    private static final String INCOMPLETE_WORKOUTS = "incomplete workouts";
    private static final String WORKOUT_EX_IND = "%s exercise index";

    public static final String MAIN_EXERCISES = "main exercises";
    public static final String WARMUP_EXERCISES = "warmup exercises";
    public static final String EXERCISE_INDEX = "exercise index";
    public static final String SET_INDEX = "set index";
    public static final String SESSION = "session";
    public static final String SETS = "sets";
    public static final String REPS = "reps";
    public static final String WEIGHT = "weight";

    // User Info
    // -------------------------------------------------------------------------------------------
    public static String getEmailPref(Context context) {
        return getSharedPref(context, R.string.file_user_info).getString(EMAIL, null);
    }

    public static void setUserInfoPref(Context context, String email, String uid) {
        SharedPreferences.Editor editor = getSharedPref(context, R.string.file_user_info).edit();
        editor.putString(EMAIL, email);
        editor.apply();
    }
    // -------------------------------------------------------------------------------------------


    // Workouts
    // -------------------------------------------------------------------------------------------
    public static void addIncompleteWorkoutPref(Context context, String workoutName) {
        Set<String> incompleteWorkouts = getIncompleteWorkouts(context);
        if (incompleteWorkouts == null) {
            incompleteWorkouts = new HashSet<>();
        }
        incompleteWorkouts.add(workoutName);
        addIncompleteWorkoutPref(context, incompleteWorkouts);
    }

    private static void addIncompleteWorkoutPref(Context context, Set<String> incompleteWorkouts) {
        SharedPreferences.Editor editor = getSharedPref(context, R.string.file_workout_info).edit();
        editor.putStringSet(INCOMPLETE_WORKOUTS, incompleteWorkouts);
        editor.apply();
    }

    private static Set<String> getIncompleteWorkouts(Context context) {
        return getSharedPref(
                context,
                R.string.file_workout_info
        ).getStringSet(INCOMPLETE_WORKOUTS, null);
    }

    public static boolean removeIncompleteWorkoutPref(Context context, String workoutName) {
        Set<String> incompleteWorkouts = getIncompleteWorkouts(context);
        if (incompleteWorkouts == null) {
            return false;
        }
        boolean removed = incompleteWorkouts.remove(workoutName);

        if (incompleteWorkouts.size() == 0) {
            SharedPreferences.Editor editor = getSharedPref(context, R.string.file_workout_info).edit();
            editor.remove(INCOMPLETE_WORKOUTS);
            editor.apply();
        } else {
            addIncompleteWorkoutPref(context, incompleteWorkouts);
        }
        return removed;
    }
    // -------------------------------------------------------------------------------------------


    // Workout Sessions    // Workouts
    //    // -------------------------------------------------------------------------------------------
    public static void addIncompleteSessionPref(Context context, String workoutName, String sessionJson) {
        SharedPreferences.Editor editor = getSharedPref(context, R.string.file_workout_info).edit();
        editor.putString(String.format(WORKOUT_EX_IND, workoutName), sessionJson);
        editor.apply();
    }


    public static String getIncompleteSessionPref(Context context, String workoutName) {
        return getSharedPref(
                context,
                R.string.file_workout_info
        ).getString(String.format(WORKOUT_EX_IND, workoutName), null);
    }

    public static void removeIncompleteSessionPref(Context context, String workoutName) {
        SharedPreferences.Editor editor = getSharedPref(context, R.string.file_workout_info).edit();
        editor.remove(String.format(WORKOUT_EX_IND, workoutName));
        editor.apply();
    }

    public static SharedPreferences getSharedPref(Context context, int fileId) {
        return context.getSharedPreferences(
                context.getString(fileId),
                Context.MODE_PRIVATE
        );
    }
    // -------------------------------------------------------------------------------------------
}
