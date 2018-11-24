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
    SparseArray<ArrayList<ExerciseSet>> sessionSets = new SparseArray<>();

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


    public SparseArray<ArrayList<ExerciseSet>> getSessionSets() {
        return sessionSets;
    }

    public Map<String, Float> getAvgWeights() {
        return avgWeights;
    }
    // ============================================================================================

    public void addExerciseSets(Exercise exercise, ArrayList<ExerciseSet> exerciseSets, float weightChange) {
        this.sessionSets.put(exercise.getExerciseNumber(), exerciseSets);

        float weight = 0.0f;
        for (ExerciseSet exerciseSet : exerciseSets) {
            weight += exerciseSet.getWeight();
        }

        this.avgWeights.put(exercise.getName(), weight/ exerciseSets.size() + weightChange);
    }

    /* Misc function used to store Session information in the firebase db */
    public Map<String, Object> toMap() {
        Map<String, Object>
                sessionMap = new HashMap<>(),
                exMap = new HashMap<>(),
                setMap;

        sessionMap.put("workoutName", workoutName);

        for (int i = 0; i < sessionSets.size(); i++){

            setMap = new HashMap<>();
            for (ExerciseSet exerciseSet : sessionSets.get(i)) {
                setMap.put(String.valueOf(exerciseSet.getSetNumber()), exerciseSet.toMap());
            }
            exMap.put(String.valueOf(i), setMap);
        }

        sessionMap.put(SETS, exMap);

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
