package ca.judacribz.gainzassist.models;

import java.util.HashMap;
import java.util.Map;

public class Set {

    // Global Vars
    // --------------------------------------------------------------------------------------------
    private int setNumber, reps;
    private float weight;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // Set Constructors                                                                          //
    // ######################################################################################### //
    /* Required empty constructor for firebase */
    public Set() {
    }

    public Set(int setNumber, int reps, float weight) {
        this.setNumber = setNumber;
        this.reps = reps;
        this.weight = weight;
    }
    // ######################################################################################### //

    // Getters and setters
    // ============================================================================================
    public int getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(int setNumber) {
        this.setNumber = setNumber;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
    // ============================================================================================


    // Helper function used to store Set information in the firebase db
    Map<String, Object> toMap() {
        Map<String, Object> set = new HashMap<>();

        set.put("reps", reps);
        set.put("weight", weight);

        return set;
    }
}