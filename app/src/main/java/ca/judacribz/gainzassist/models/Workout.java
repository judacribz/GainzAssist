package ca.judacribz.gainzassist.models;

import android.arch.persistence.room.*;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Entity(tableName = "workouts",
        indices = {@Index(value = {"name"}, unique = true)})
public class Workout implements Parcelable {

    // Global Vars
    // --------------------------------------------------------------------------------------------
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;

    @Ignore
    private ArrayList<Exercise> exercises = new ArrayList<>();
    // --------------------------------------------------------------------------------------------


    // ######################################################################################### //
    // Workout Constructor                                                                       //
    // ######################################################################################### //
    public Workout() {
    }


    public Workout(String name, @Nullable ArrayList<Exercise> exercises) {
        setName(name);
        if (exercises != null){
            this.exercises = exercises;
        }
    }
    // ######################################################################################### //


    // Getters and setters
    // ============================================================================================
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addExercise(Exercise exercise) {
        exercises.add(exercise);
    }

    public Exercise getExercise(String exName) {
            for (Exercise exercise : exercises) {
                if (exercise.getName().equals(exName)) {
                    return exercise;
                }
            }

            return null;
    }


    public ArrayList<Exercise> getExercises() {
        return exercises;
    }

    public String getName() {
        return this.name;
    }
    // ============================================================================================


    // Parcelable Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.exercises);
        dest.writeString(this.name);
    }

    protected Workout(Parcel in) {
        this.exercises = in.createTypedArrayList(Exercise.CREATOR);
        this.name = in.readString();
    }

    public static final Parcelable.Creator<Workout> CREATOR = new Parcelable.Creator<Workout>() {
        @Override
        public Workout createFromParcel(Parcel source) {
            return new Workout(source);
        }

        @Override
        public Workout[] newArray(int size) {
            return new Workout[size];
        }
    };
    //Parcelable//Override////////////////////////////////////////////////////////////////////////


    // Helper functions
    // --------------------------------------------------------------------------------------------
    /* Helper function used to store Workout information in the Firebase db */
    public Map<String, Object> toMap() {
        Map<String, Object> workout = new HashMap<>();
        Map<String, Object> exs = new HashMap<>();

        for (Exercise exercise: exercises) {
            exs.put(String.valueOf(exercises.indexOf(exercise)), exercise.toMap());
        }

        workout.put("exercises", exs);

        return workout;
    }


    /* Returns true if the exercise exist, false if not */
    public boolean containsExercise(String exerciseName) {
        for (Exercise exercise : exercises) {
            if (exercise.getName().toLowerCase().equals(exerciseName.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    public ArrayList<String> getExerciseNames() {
        ArrayList<String> exerciseNames = new ArrayList<>();
        for (Exercise exercise : exercises) {
            exerciseNames.add(exercise.getName());
        }

        return  exerciseNames;
    }

    public void setExercises(ArrayList<Exercise> exercises) {
        this.exercises = exercises;
    }
    // --------------------------------------------------------------------------------------------
}