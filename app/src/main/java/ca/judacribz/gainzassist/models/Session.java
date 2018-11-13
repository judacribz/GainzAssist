package ca.judacribz.gainzassist.models;

import android.arch.persistence.room.*;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.arch.persistence.room.ForeignKey.SET_NULL;

@Entity(tableName = "sessions",
        foreignKeys =
            @ForeignKey(
                entity = Workout.class,
                parentColumns = "id",
                childColumns = "workout_id",
                onDelete = SET_NULL),
        indices = {@Index(value = {"workout_id", "timestamp"}, unique = true)})
public class Session {

    // Global Vars
    // --------------------------------------------------------------------------------------------
    @PrimaryKey(autoGenerate = true)
    int id;

    @ColumnInfo(name = "workout_id")
    int workoutId;
    long timestamp;

    @Ignore
    Map<String, ArrayList<Set>> sessionSets = new HashMap<>();

    @Ignore
    Map<String, Float> avgWeights = new HashMap<>();

    @Ignore
    String workoutName;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // Session Constructors                                                                     //
    // ######################################################################################### //
    // ######################################################################################### //
    /* Required empty constructor for firebase */
    public Session() {
    }

    @Ignore
    public Session(Workout workout) {
        setWorkoutId(workout.getId());
        setWorkoutName(workout.getName());
        this.timestamp = new Date().getTime();
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

    public void setTimestamp() {
        this.timestamp = new Date().getTime();
    }

    public Map<String, ArrayList<Set>> getSessionSets() {
        return sessionSets;
    }
    public Map<String, Float> getAvgWeights() {
        return avgWeights;
    }
    // ============================================================================================

    public void addExerciseSets(String exerciseName, ArrayList<Set> sets, float weightChange) {
        sessionSets.put(exerciseName, sets);

        float weight = 0.0f;
        for (Set set : sets) {
            weight += set.getWeight();
        }

        avgWeights.put(exerciseName, weight/sets.size() + weightChange);
    }

    /* Helper function used to store Session information in the firebase db */
    public Map<String, Object> toMap() {
        Map<String, Object> sessionMap = new HashMap<>();
        Map<String, Object> exMap = new HashMap<>();
        Map<String, Object> setMap;

        sessionMap.put("workoutName", workoutName);

        for (Map.Entry<String, ArrayList<Set>> exSets : sessionSets.entrySet()){

            setMap = new HashMap<>();
            for (Set set : exSets.getValue()) {
                setMap.put(String.valueOf(set.getSetNumber()), set.toMap());
            }
            exMap.put(exSets.getKey(), setMap);
        }

        sessionMap.put("sets", exMap);

        return  sessionMap;

    }
}
