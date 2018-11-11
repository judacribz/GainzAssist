package ca.judacribz.gainzassist.models;

import android.arch.persistence.room.*;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static android.arch.persistence.room.ForeignKey.SET_NULL;

@Parcel
@Entity(tableName = "sets",
        foreignKeys = {
            @ForeignKey(
                entity = Session.class,
                parentColumns = "id",
                childColumns = "session_id",
                onDelete = CASCADE,
                onUpdate = CASCADE),
            @ForeignKey(
                entity = Exercise.class,
                parentColumns = "id",
                childColumns = "exercise_id",
                onDelete = SET_NULL,
                onUpdate = CASCADE)},
        indices = {@Index(value = {"session_id", "exercise_id", "set_number"}, unique = true)})
public class Set {

    // Global Vars
    // --------------------------------------------------------------------------------------------
    @PrimaryKey(autoGenerate = true)
    int id;

    @ColumnInfo(name = "session_id")
    int sessionId;
    @ColumnInfo(name = "exercise_id")
    int exerciseId;

    @ColumnInfo(name = "set_number")
    int setNumber;
    int reps;
    float weight;

    @Ignore
    String exerciseName;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // Set Constructors                                                                          //
    // ######################################################################################### //
    /* Required empty constructor for firebase */
    @Ignore
    public Set() {
    }

    public Set(int exerciseId, int setNumber, int reps, float weight) {
        setExerciseId(exerciseId);
        setSetNumber(setNumber);
        setReps(reps);
        setWeight(weight);
    }
    // ######################################################################################### //

    // Getters and setters
    // ============================================================================================


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

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
        Map<String, Object> setMap = new HashMap<>();

        setMap.put("reps", reps);
        setMap.put("weight", weight);

        return setMap;
    }
}