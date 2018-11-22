package ca.judacribz.gainzassist.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.ExerciseSet;
import ca.judacribz.gainzassist.models.Workout;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DataSnapshot;

import java.io.IOException;
import java.util.*;


public class Misc {

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

    public static Map<String, Object> exerciseSetsToMap(ArrayList<Exercise> exercises)  {
        Map<String, Object> exs = new HashMap<>();
        for (Exercise exercise: exercises) {
            exs.put(String.valueOf(exercise.getExerciseNumber()), exercise.setsToMap());
        }

        return exs;
    }
    private static ObjectMapper mapper = new ObjectMapper();

    public static String writeValueAsString(Object object) {
        String jsonStr = "";

        try {
            jsonStr = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return  jsonStr;
    }


    public static Map<String, Object> readValue(Object childObj) {
        Map<String, Object> childMap = new HashMap<>();

        try {
            mapper.readValue(
                    writeValueAsString(childObj),
                    new TypeReference<Map<String, Object>>() {}
            );
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return childMap;
    }
}
