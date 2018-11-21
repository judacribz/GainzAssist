package ca.judacribz.gainzassist.models;

import android.arch.persistence.room.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.arch.persistence.room.ForeignKey.*;

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
    Map<String, ArrayList<ExerciseSet>> sessionSets = new HashMap<>();

    @Ignore
    Map<String, Float> avgWeights = new HashMap<>();


    @Ignore
    Workout currWorkout;
    @Ignore
    ArrayList<Exercise> currMains, currWarmups;
    @Ignore
    int exerciseIndex = -1;

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


    public Workout getCurrWorkout() {
        return currWorkout;
    }

    public void setCurrWorkout(Workout currWorkout) {
        this.currWorkout = currWorkout;
    }

    public ArrayList<Exercise> getCurrMains() {
        return currMains;
    }

    public void setCurrMains(ArrayList<Exercise> currMains) {
        this.currMains = currMains;
    }

    public ArrayList<Exercise> getCurrWarmups() {
        return currWarmups;
    }

    public void setCurrWarmups(ArrayList<Exercise> currWarmups) {
        this.currWarmups = currWarmups;
    }

    public int getExerciseIndex() {
        return exerciseIndex;
    }

    public void setExerciseIndex(int exerciseIndex) {
        this.exerciseIndex = exerciseIndex;
    }

    public Map<String, ArrayList<ExerciseSet>> getSessionSets() {
        return sessionSets;
    }

    public Map<String, Float> getAvgWeights() {
        return avgWeights;
    }
    // ============================================================================================

    public void addExerciseSets(String exerciseName, ArrayList<ExerciseSet> exerciseSets, float weightChange) {
        sessionSets.put(exerciseName, exerciseSets);

        float weight = 0.0f;
        for (ExerciseSet exerciseSet : exerciseSets) {
            weight += exerciseSet.getWeight();
        }

        avgWeights.put(exerciseName, weight/ exerciseSets.size() + weightChange);
    }

    /* Helper function used to store Session information in the firebase db */
    public Map<String, Object> toMap() {
        Map<String, Object> sessionMap = new HashMap<>();
        Map<String, Object> exMap = new HashMap<>();
        Map<String, Object> setMap;

        sessionMap.put("workoutName", workoutName);

        for (Map.Entry<String, ArrayList<ExerciseSet>> exSets : sessionSets.entrySet()){

            setMap = new HashMap<>();
            for (ExerciseSet exerciseSet : exSets.getValue()) {
                setMap.put(String.valueOf(exerciseSet.getSetNumber()), exerciseSet.toMap());
            }
            exMap.put(exSets.getKey(), setMap);
        }

        sessionMap.put("sets", exMap);

        return  sessionMap;

    }
}
