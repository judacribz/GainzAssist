package ca.judacribz.gainzassist.models;

import android.arch.persistence.room.*;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.arch.persistence.room.ForeignKey.*;
import static ca.judacribz.gainzassist.util.Preferences.*;

@Entity(tableName = "sessions",
        foreignKeys =
            @ForeignKey(
                entity = Workout.class,
                parentColumns = "id",
                childColumns = "workout_id",
                onDelete = CASCADE,
                onUpdate = CASCADE),
        indices = {
                @Index("workout_id"),
                @Index(value = {"workout_id", "timestamp"})})
public class Session {

    // Global Vars
    // --------------------------------------------------------------------------------------------
    @PrimaryKey(autoGenerate = true)
    int id;

    @ColumnInfo(name = "workout_id")
    int workoutId;

    long timestamp;

    @ColumnInfo(name = "workout_name")
    String workoutName;

    @Ignore
    ArrayList<Exercise> sessionSets = new ArrayList<>();

    @Ignore
    Map<String, Float> avgWeights = new HashMap<>();
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // Session Constructors                                                                     //
    // ######################################################################################### //
    public Session() {
    }

    @Ignore
    public Session(Workout workout) {
        setWorkoutId(workout.getId());
        setWorkoutName(workout.getName());
        setTimestamp(-1);
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

    public int getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(int workoutId) {
        this.workoutId = workoutId;
    }

    public void setWorkoutName(String workoutName) {
        this.workoutName = workoutName;
    }

    public String getWorkoutName() {
        return workoutName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
            this.timestamp = (timestamp == -1) ? new Date().getTime() : timestamp;
    }


    public ArrayList<Exercise> getSessionSets() {
        return sessionSets;
    }

    public Map<String, Float> getAvgWeights() {
        return avgWeights;
    }
    // ============================================================================================

    public void addExercise(Exercise exercise, float weightChange) {
        float weight = 0.0f;
        float expectedReps = (float)exercise.getReps();
        for (ExerciseSet exerciseSet : exercise.getSetsList()) {
            weight += exerciseSet.getWeight() * (float)exerciseSet.getReps()/expectedReps;
        }
        weight = weight / exercise.getSetsList().size() + weightChange;
        weight -= weight % weightChange;

        this.sessionSets.add(exercise);

        this.avgWeights.put(exercise.getName(), weight);
    }

    public void remLastExercise() {

        this.sessionSets.remove(this.sessionSets.size() - 1);
    }

    /* Misc function used to store Session information in the firebase db */
    public Map<String, Object> toMap() {
        Map<String, Object>
                exsMap = new HashMap<>(),
                sessionMap = new HashMap<String, Object>() {{
                    put(SESSION_ID, id);
                    put(WORKOUT_NAME, workoutName);
                    put(WORKOUT_ID, workoutId);
                }};

        for (Exercise exSets : sessionSets){
            exsMap.put(String.valueOf(exSets.getExerciseNumber()), exSets.setsToMap());
        }
        sessionMap.put(EXERCISES, exsMap);

        return  sessionMap;
    }

    public Map<String, Object> sessionStateMap(int exerciseIndex, int setIndex) {
        Map<String, Object>
                sessionStateMap = new HashMap<>(),
                sessionMap = this.toMap();

        sessionStateMap.put(SESSION, sessionMap);
        sessionStateMap.put(EXERCISE_INDEX, exerciseIndex);
        sessionStateMap.put(SET_INDEX, setIndex);

        return  sessionStateMap;
    }
}
