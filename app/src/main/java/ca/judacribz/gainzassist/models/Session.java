package ca.judacribz.gainzassist.models;

import android.arch.persistence.room.*;
import android.util.SparseArray;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.arch.persistence.room.ForeignKey.*;
import static ca.judacribz.gainzassist.constants.ExerciseConst.*;

@Entity(tableName = "sessions",
        indices = {
                @Index("workout_id"),
                @Index(value = {"workout_id", "timestamp"})})
public class Session {

    // Global Vars
    // --------------------------------------------------------------------------------------------
    @PrimaryKey
    long timestamp;

    @ColumnInfo(name = "workout_id")
    long workoutId;

    @ColumnInfo(name = "workout_name")
    String workoutName;

    @Ignore
    ArrayList<Exercise> sessionExs = new ArrayList<>();

    @Ignore
    SparseArray<Float> avgWeights = new SparseArray<>();
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // Session Constructors                                                                     //
    // ######################################################################################### //
    public Session() {
    }

    @Ignore
    public Session(Workout workout) {
        setTimestamp(-1);
        setWorkoutId(workout.getId());
        setWorkoutName(workout.getName());
    }
    // ######################################################################################### //


    // Getters and setters
    // ============================================================================================
    public long getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(long workoutId) {
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


    public ArrayList<Exercise> getSessionExs() {
        return sessionExs;
    }

    public SparseArray<Float> getAvgWeights() {
        return this.avgWeights;
    }
    // ============================================================================================

    public void addExercise(Exercise exercise) {
        float
                weight = 0.0f,
                weightChange = exercise.getWeightChange(),
                expectedReps = (float)exercise.getReps();
        for (ExerciseSet exerciseSet : exercise.getFinishedSetsList()) {
            weight += exerciseSet.getWeight() * (float)exerciseSet.getReps()/expectedReps;
        }

        Logger.d(weight + "yo");
        weight = weight / exercise.getFinishedSetsList().size() + weightChange;
        weight -= weight % weightChange;

        if (avgWeights.get(exercise.getExerciseNumber(), -1f) != -1f) {
            this.sessionExs.set(exercise.getExerciseNumber(), exercise);
        } else {
            this.sessionExs.add(exercise);
        }

        this.avgWeights.put(exercise.getExerciseNumber(), weight);

//        for (Exercise ex : sessionExs) {
//            Logger.d("EXERCISE # " + ex.getName() + " " + avgWeights.get(ex.getName()));
//        }
    }

    public void remLastExercise() {

        this.sessionExs.remove(this.sessionExs.size() - 1);
    }

    /* Misc function used to store Session information in the firebase db */
    public Map<String, Object> toMap() {
        Map<String, Object>
                exsMap = new HashMap<>(),
                sessionMap = new HashMap<String, Object>() {{
                    put(WORKOUT_NAME, workoutName);
                    put(WORKOUT_ID, workoutId);
                }};

        for (Exercise ex : sessionExs){
            exsMap.put(String.valueOf(ex.getExerciseNumber()), ex.setsToMap());
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
