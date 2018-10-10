package ca.judacribz.gainzassist.models;

import android.content.Context;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Session {

    // Global Vars
    // --------------------------------------------------------------------------------------------
    private String workoutName;
    private ArrayList<String> exerciseNames;
    private long timestamp;
    private ArrayList<ArrayList<Set>> allSets;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // Session Constructors                                                                     //
    // ######################################################################################### //
    // ######################################################################################### //
    /* Required empty constructor for firebase */
    public Session() {
    }

    public Session(Context context, String workoutName, ArrayList<ArrayList<Set>> allSets) {
        this.workoutName = workoutName;
        this.timestamp = System.currentTimeMillis()/1000;;
        this.allSets = allSets;
        this.exerciseNames = (new WorkoutHelper(context)).getAllExerciseNames(workoutName);
    }

    /* Helper function used to store Session information in the firebase db */
    Map<String, Object> toMap() {
        Map<String, Object> session = new HashMap<>();
        session.put("workoutName", workoutName);
        session.put("timestamp", timestamp);

        Map<String, Object> setMap = new HashMap<>();
        Map<String, Object> exMap = new HashMap<>();

        int i = 0;
        for (ArrayList<Set> sets: allSets) {
            for (Set set: sets) {
                setMap.put(String.valueOf(set.getSetNumber()), set.toMap());
            }

            exMap.put(exerciseNames.get(i++), setMap);
            setMap.clear();
        }

        session.put("sets", exMap);

        return session;
    }
}
