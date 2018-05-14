package ca.judacribz.gainzassist.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Workout {

    // Global Vars
    // --------------------------------------------------------------------------------------------
    private ArrayList<Exercise> exercises;
    private String name;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // Workout Constructor                                                                       //
    // ######################################################################################### //
    public Workout(String name, ArrayList<Exercise> exercises) {
        this.name = name;
        this.exercises = exercises;
    }
    // ######################################################################################### //

    // Getters and setters
    // ============================================================================================
    public ArrayList<Exercise> getExercises() {
        return exercises;
    }

    public String getName() {
        return this.name;
    }
    // ============================================================================================


    /* Helper function used to store Workout information in the firebase db */
    public Map<String, Object> toMap() {
        Map<String, Object> workout = new HashMap<>();

        for (Exercise exercise: exercises) {
            workout.put(exercise.getName(), exercise.toMap());
        }

        return workout;
    }
}