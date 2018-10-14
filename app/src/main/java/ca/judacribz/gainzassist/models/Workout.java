package ca.judacribz.gainzassist.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Workout implements Parcelable {

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
}