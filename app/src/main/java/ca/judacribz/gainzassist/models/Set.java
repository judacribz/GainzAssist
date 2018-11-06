package ca.judacribz.gainzassist.models;

import android.arch.persistence.room.*;
import org.parceler.Parcel;

import java.util.HashMap;
import java.util.Map;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Parcel
@Entity(tableName = "sets",
        primaryKeys = {"set_number", "exercise_id"},
        foreignKeys = @ForeignKey(
                entity = Exercise.class,
                parentColumns = "id",
                childColumns = "exercise_id",
                onDelete = CASCADE),
        indices = {@Index(value = {"exercise_id", "set_number"}, unique = true)})
public class Set {

    // Global Vars
    // --------------------------------------------------------------------------------------------
    @ColumnInfo(name = "set_number")
    int setNumber;
    @ColumnInfo(name = "exercise_id")
    int exerciseId;
    int reps;
    float weight;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // Set Constructors                                                                          //
    // ######################################################################################### //
    /* Required empty constructor for firebase */
    @Ignore
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

    public int getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
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