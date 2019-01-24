package ca.judacribz.gainzassist.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.ExerciseSet;
import ca.judacribz.gainzassist.models.Session;
import ca.judacribz.gainzassist.models.Workout;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.firebase.database.DataSnapshot;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.*;

import static ca.judacribz.gainzassist.constants.ExerciseConst.*;


public class Misc {

    private static ObjectMapper mapper = new ObjectMapper();

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
        long workoutId = Long.valueOf(String.valueOf(workoutShot.child("id").getValue()));


        for (DataSnapshot exerciseShot : workoutShot.child("exercises").getChildren()) {

            // Adds exerciseSets to exercise object, and add exercise to exercises list
            if (exerciseShot != null) {
                exercise = exerciseShot.getValue(Exercise.class);

                if (exercise != null) {
                    exercise.setExerciseNumber(
                            Integer.valueOf(Objects.requireNonNull(exerciseShot.getKey()))
                    );
                    exercise.setWorkoutId(workoutId);
                    exercises.add(exercise);
                }
            }
        }
        Workout workout = new Workout(workoutShot.getKey(), exercises);
        workout.setId(workoutId);

        return workout;
    }

    public static Session extractSession(DataSnapshot sessionShot) {
        Session session = sessionShot.getValue(Session.class);

        if (session != null) {
            long timestamp = session.getTimestamp();
            Exercise exercise;
            ExerciseSet set;
            session.setTimestamp(Long.valueOf(Objects.requireNonNull(sessionShot.getKey())));

            String exerciseName;
            long exerciseId;
            long workoutId;
            for (DataSnapshot exerciseShot : sessionShot.child(EXERCISES).getChildren()) {
                exercise = exerciseShot.getValue(Exercise.class);

                if (exercise != null) {
                    exerciseId = exercise.getId();
                    exerciseName = exercise.getName();

                    exercise.setExerciseNumber(Integer.valueOf(Objects.requireNonNull(exerciseShot.getKey())));

                    for (DataSnapshot setShot : exerciseShot.child(SET_LIST).getChildren()) {
                        set = setShot.getValue(ExerciseSet.class);

                        if (set != null) {
                            set.setSetNumber(Integer.valueOf(Objects.requireNonNull(setShot.getKey())));
                            set.setExerciseId(exerciseId);
                            set.setExerciseName(exerciseName);
                            set.setSessionId(timestamp);
                            exercise.addSet(set, false);
                        }
                    }

                    session.addExercise(exercise);
                }
            }
        }

        return session;
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

    public static void enablePrettyMapper() {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static Map<String, Object> readValue(Object childObj) {
        return readValue(writeValueAsString(childObj));
    }

    public static String writeValueAsString(Object object) {
        String jsonStr = "";

        try {
            jsonStr = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return  jsonStr;
    }

    public static Map<String, Object> readValue(String childStr) {
        Map<String, Object> childMap = new HashMap<>();

        try {
            childMap = mapper.readValue(
                    childStr,
                    new TypeReference<HashMap<String, Object>>() {}
            );
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return childMap;
    }

    public static void shrinkTo(List list, int newSize) {
        int size = list.size();
        for (int i = newSize; i < size; i++) {
            list.remove(list.size() - 1);
        }
    }
}
