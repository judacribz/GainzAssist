package ca.judacribz.gainzassist.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import ca.judacribz.gainzassist.R;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.ExerciseSet;
import ca.judacribz.gainzassist.models.Session;
import ca.judacribz.gainzassist.models.Workout;
import com.google.firebase.database.DataSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;

import java.lang.reflect.Type;
import java.util.*;

public class Helper {
    private static final String EMAIL = "email";
    private static final String INCOMPLETE_WORKOUTS = "incomplete workouts";
    private static final String WORKOUT_EX_IND = "%s exercise index";

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

    public static String getEmailPref(Context context) {
        return getSharedPref(context, R.string.file_user_info).getString(EMAIL, null);
    }

    public static void setUserInfoPref(Context context, String email, String uid) {
        SharedPreferences.Editor editor = getSharedPref(context, R.string.file_user_info).edit();
        editor.putString(EMAIL, email);
        editor.apply();
    }

    public static void addIncompleteWorkoutPref(Context context, String workoutName) {
        Set<String> incompleteWorkouts = getIncompleteWorkouts(context);
        if (incompleteWorkouts == null) {
            incompleteWorkouts = new HashSet<>();
        }
        incompleteWorkouts.add(workoutName);

        Logger.d(incompleteWorkouts);

        addIncompleteWorkoutPref(context, incompleteWorkouts);
    }

    public static void addIncompleteWorkoutPref(Context context, Set<String> incompleteWorkouts) {
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

    public static void addIncompleteSessionPref(Context context, String workoutName, int exInd) {
        SharedPreferences.Editor editor = getSharedPref(context, R.string.file_workout_info).edit();
        editor.putInt(String.format(WORKOUT_EX_IND, workoutName), exInd);
        editor.apply();
    }


    public static int getIncompleteSessionPref(Context context, String workoutName) {
        return getSharedPref(
                context,
                R.string.file_workout_info
        ).getInt(String.format(WORKOUT_EX_IND, workoutName), -1);
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

    public static Workout extractWorkout(DataSnapshot workoutShot) {
        ArrayList<Exercise> exercises = new ArrayList<>();
        Exercise exercise;
        ArrayList<ExerciseSet> exerciseSets;
        ExerciseSet exerciseSet;
        String setNum;

        for (DataSnapshot exerciseShot : workoutShot.child("exercises").getChildren()) {

            // Adds exerciseSets to exercise object, and add exercise to exercises list
            if (exerciseShot != null) {
                exercise = exerciseShot.getValue(Exercise.class);
                if (exercise != null) {
                    exercise.setExerciseNumber(
                            Integer.valueOf(Objects.requireNonNull(exerciseShot.getKey()))
                    );
                    exercises.add(exercise);
                }
            }
        }

        return new Workout(workoutShot.getKey(), exercises);
    }

    public static Workout extractSession(DataSnapshot sessionShot) {

        for (DataSnapshot exerciseShot : sessionShot.child("sets").getChildren()) {

            for (DataSnapshot setShot : exerciseShot.getChildren()) {

                ExerciseSet exerciseSet = setShot.getValue(ExerciseSet.class);
                if (exerciseSet != null) {
                    exerciseSet.setSetNumber(Integer.valueOf(Objects.requireNonNull(setShot.getKey())));

                    exerciseSet.setExerciseName(exerciseShot.getKey());
                }

            }
        }

        return null;
    }


    public static Map<String, Object> exerciseToMap(ArrayList<Exercise> exercises)  {
        Map<String, Object> exs = new HashMap<>();
        for (Exercise exercise: exercises) {
            exs.put(String.valueOf(exercise.getExerciseNumber()), exercise.toMap());
        }

        return exs;
    }
}
