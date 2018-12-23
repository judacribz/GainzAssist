package ca.judacribz.gainzassist.models;

import android.arch.persistence.room.*;
import org.parceler.Parcel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.arch.persistence.room.ForeignKey.*;

@Parcel
@Entity(tableName = "exercise_sets")
public class ExerciseSet {

    // Global Vars
    // --------------------------------------------------------------------------------------------
    @PrimaryKey
    long id = -1;

    @ColumnInfo(name = "session_id")
    long sessionId;

    @ColumnInfo(name = "exercise_id")
    long exerciseId;

    @ColumnInfo(name = "set_number")
    int setNumber;
    int reps;
    float weight;

    @ColumnInfo(name = "exercise_name")
    public String exerciseName;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // ExerciseSet Constructors                                                                          //
    // ######################################################################################### //
    public ExerciseSet() {
    }

    @Ignore
    public ExerciseSet(Exercise exercise, int setNumber, int reps, float weight) {
        this(exercise.getId(), exercise.getName(), setNumber, reps, weight);
    }

    @Ignore
    public ExerciseSet(long exerciseId, String exerciseName, int setNumber, int reps, float weight) {
        setId(-1);
        setExerciseId(exerciseId);
        setExerciseName(exerciseName);
        setSetNumber(setNumber);
        setReps(reps);
        setWeight(weight);
    }
    // ######################################################################################### //

    // Getters and setters
    // ============================================================================================
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = (id == -1) ? new Date().getTime() : id;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }
    public String getExerciseName() {
        return exerciseName;
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


    // Misc function used to store ExerciseSet information in the FireBase db
    Map<String, Object> toMap() {
        Map<String, Object> exerciseSetMap = new HashMap<>();

        exerciseSetMap.put("id", id);
        exerciseSetMap.put("reps", reps);
        exerciseSetMap.put("weight", weight);

        return exerciseSetMap;
    }
}