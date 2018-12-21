package ca.judacribz.gainzassist.models;

import android.arch.persistence.room.*;
import org.parceler.Parcel;

import java.util.HashMap;
import java.util.Map;

import static android.arch.persistence.room.ForeignKey.*;

@Parcel
@Entity(tableName = "exercise_sets",
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
                onDelete = CASCADE,
                onUpdate = CASCADE)},
        indices = {
            @Index("session_id"),
            @Index("exercise_id")})
public class ExerciseSet {

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
    public ExerciseSet(int exerciseId, String exerciseName, int setNumber, int reps, float weight) {
        setExerciseId(exerciseId);
        setExerciseName(exerciseName);
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


    // Misc function used to store ExerciseSet information in the firebase db
    Map<String, Object> toMap() {
        Map<String, Object> exerciseSetMap = new HashMap<>();

        exerciseSetMap.put("reps", reps);
        exerciseSetMap.put("weight", weight);

        return exerciseSetMap;
    }
}