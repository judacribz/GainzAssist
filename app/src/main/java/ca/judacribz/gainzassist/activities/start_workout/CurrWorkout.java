package ca.judacribz.gainzassist.activities.start_workout;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.ExerciseSet;
import ca.judacribz.gainzassist.models.Session;
import ca.judacribz.gainzassist.models.Workout;
import ca.judacribz.gainzassist.models.db.WorkoutViewModel;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Map;

import static ca.judacribz.gainzassist.models.Exercise.SetsType.MAIN_SET;
import static ca.judacribz.gainzassist.models.Exercise.SetsType.WARMUP_SET;
import static ca.judacribz.gainzassist.util.Calculations.getOneRepMax;
import static ca.judacribz.gainzassist.util.Misc.enablePrettyMapper;
import static ca.judacribz.gainzassist.util.Misc.readValue;
import static ca.judacribz.gainzassist.util.Misc.writeValueAsString;
import static ca.judacribz.gainzassist.util.Preferences.*;

public class CurrWorkout {

    // Constants
    // --------------------------------------------------------------------------------------------
    private static final CurrWorkout INST = new CurrWorkout();

    private static final String BARBELL = "barbell";
    public static final float BB_MIN_WEIGHT = 45.0f;
    public static final float MIN_WEIGHT = 2.5f;
    public static final float BB_WEIGHT_CHANGE = 5.0f;
    public static final float WEIGHT_CHANGE = 2.5f;
    public static final int MIN_REPS = 0;
    private static final long HEAVY_REST_TIME = 180000;
    private static final long LIGHT_REST_TIME = 90000;
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    private Workout currWorkout;
    private Exercise currExercise;

    private ExerciseSet currExerciseSet;
    private float
            currWeight,
            currMinWeight = MIN_WEIGHT,
            currWeightChange = WEIGHT_CHANGE;

    private ArrayList<Exercise>
            currWarmups,
            currMains;

    private int
            set_i = -1,
            ex_i = -1,
            currReps,
            numWarmups,
            numMains;
    private long currRestTime;
    private boolean timerSet;

    private Session currSession = null;
    private ArrayList<ExerciseSet> finishedSets = new ArrayList<>();

    private Context context;
    // --------------------------------------------------------------------------------------------


    // Interfaces
    // --------------------------------------------------------------------------------------------
    private TimerListener timerListener;

    public interface TimerListener {
        void startTimer(long timeInMillis);
    }
    public void setTimerListener(TimerListener timerListener) {
        this.timerListener = timerListener;
        //TODO make deterministic
        if (!timerSet) {
            setTimer();
        }
    }

    private DataListener dataListener;
    public interface DataListener {
        void warmupsGenerated(ArrayList<Exercise> warmups);
    }
    void setDataListener(DataListener dataListener) {
        this.dataListener = dataListener;
    }
    // --------------------------------------------------------------------------------------------


    // ######################################################################################### //
    // CurrWorkout Constructor/Instance                                                        //
    // ######################################################################################### //
    private CurrWorkout() {
    }

    public static CurrWorkout getInstance() {
        return INST;
    }
    // ######################################################################################### //

    public void setContext(Context context) {
        this.context = context;
    }

    void retrieveCurrWorkout(Workout workout) {
            setRetrievedWorkout(readValue(getIncompleteSessionPref(context, workout.getName())), workout);

    }

    private void setRetrievedWorkout(Map<String, Object> map, Workout workout) {
        Map<String, Object> sessionMap;
        Map<String, Object> exMap, setMap;
        ArrayList<ExerciseSet> warmupSets;

        currWarmups = new ArrayList<>();
        this.currWorkout = workout;
        this.currSession = new Session(workout);

        this.ex_i = Integer.valueOf(String.valueOf(map.get(EXERCISE_INDEX)));
        this.set_i = Integer.valueOf(String.valueOf(map.get(SET_INDEX)));

        sessionMap =  readValue(readValue(map.get(SESSION)).get(SETS));
        for (Map.Entry<String, Object> sessEntry : sessionMap.entrySet()) {
            String exNum = sessEntry.getKey();
            Exercise exercise = workout.getExerciseFromIndex(Integer.valueOf(exNum));
            setCurrEquip(exercise.getEquipment());
            exMap = readValue(sessionMap.get(exNum));

            this.finishedSets = new ArrayList<>();
            for (Map.Entry<String, Object> exEntry : exMap.entrySet()) {
                setMap = readValue(exEntry.getValue());
                this.finishedSets.add(new ExerciseSet(
                        exercise,
                        Integer.valueOf(exEntry.getKey()),
                        Integer.valueOf(String.valueOf(setMap.get(REPS))),
                        Float.valueOf(String.valueOf(setMap.get(WEIGHT)))
                ));
            }

            this.currSession.addExerciseSets(
                    exercise,
                    this.finishedSets,
                    this.currWeightChange
            );
        }

        genWarmups(workout.getExercises());

        enablePrettyMapper();
        writeValueAsString(this.currSession.sessionStateMap(
                ex_i,
                set_i
        ));
    }

    void setCurrWorkout(Workout workout) {
        this.currWorkout = workout;
        this.currSession = new Session(workout);
        genWarmups(workout.getExercises());
    }

    private void genWarmups(ArrayList<Exercise> exercises) {
        ArrayList<Exercise>
                allExs = new ArrayList<>(),
                warmups = new ArrayList<>();
        ArrayList<ExerciseSet> exerciseSets;
        Exercise warmup;

        int exId;
        float oneRepMax, minWeight, weight, percWeight, newWeight;
        int reps, setNum;
        String equip;

        setCurrMainExercises(exercises);

        for (Exercise ex: exercises) {
            ex.setSetsType(MAIN_SET);
            equip = ex.getEquipment();

            minWeight = BARBELL.equals(equip.toLowerCase()) ? BB_MIN_WEIGHT : MIN_WEIGHT;
            weight = ex.getAvgWeight();
            if (weight == minWeight) {
                allExs.add(ex);
                continue;
            }

            //Todo: use onerepmax
            oneRepMax = getOneRepMax(ex.getAvgReps(), ex.getAvgWeight());
            reps = ex.getAvgReps();

            setNum = 1;
            exerciseSets = new ArrayList<>();
            exerciseSets.add(new ExerciseSet(ex, setNum++, reps, minWeight));
            percWeight = minWeight / weight;
            do {
                newWeight = percWeight * weight;
                newWeight -= newWeight % 5;
                exerciseSets.add(new ExerciseSet(ex, setNum++, reps, newWeight));

                percWeight += 0.2f;
                if (reps - 2 > 0)
                    reps -= 2;
                else if (reps - 1 > 0)
                    --reps;

            } while (percWeight < 0.8f);

            warmup = new Exercise(
                    ex.getExerciseNumber(),
                    ex.getName(),
                    ex.getType(),
                    ex.getEquipment(),
                    exerciseSets,
                    WARMUP_SET
            );
            warmups.add(warmup);
            allExs.add(warmup);
            allExs.add(ex);
        }

        setCurrWarmupExercises(warmups);
        setAllCurrExercises(allExs);
    }

    private void setCurrMainExercises(ArrayList<Exercise> exercises) {
        this.currMains = exercises;
        this.numMains = exercises.size();
    }

    private void setCurrWarmupExercises(ArrayList<Exercise> warmups) {
        this.currWarmups = warmups;
        this.numWarmups = warmups.size();

        dataListener.warmupsGenerated(warmups);
    }

    private void setAllCurrExercises(ArrayList<Exercise> allExercises ) {
        this.currWorkout.setExercises(allExercises);

        if (this.ex_i == -1) {
            this.ex_i = 0;
        }

        setCurrExercise(this.currWorkout.getExerciseFromIndex(this.ex_i));
    }

    public boolean finishCurrSet() {
        this.set_i++;

        if (!getIsWarmup()) {
            currExerciseSet.setReps(currReps);
            currExerciseSet.setWeight(currWeight);
            finishedSets.add(currExerciseSet);
            Logger.d("I should not be in here");
        }

        // End of sets for an exercise
        if (atEndOfSets()) {
            this.set_i = 0;
            this.ex_i++;

            Logger.d(this.currExercise.getName());
            this.currSession.addExerciseSets(this.currExercise, finishedSets, currWeightChange);

            // End of all exercises for this workout session
            if (atEndOfExercises()) {
                this.currSession.setTimestamp(-1);
                ViewModelProviders.of((FragmentActivity) context)
                        .get(WorkoutViewModel.class)
                        .insertSession(this.currSession);

                removeSessPrefAndSetWorkout();
                resetIndices();

                return false;

            // Not end of all exercises, set next exercise
            } else {
                setCurrExercise(currWorkout.getExerciseFromIndex(this.ex_i));
                finishedSets = new ArrayList<>();
            }




        // End of set, set next set from current exercise
        } else {
            setCurrExerciseSet(currExercise.getSet(this.set_i));
        }

        return true;
    }

    private boolean atEndOfSets() {
        return this.set_i >= this.currExercise.getNumSets();
    }

    private boolean atEndOfExercises() {
        return (this.ex_i >= this.currWorkout.getNumExercises());
    }

    public int getExInd() {
        return  this.ex_i;
    }

    public void setExInd(int ex_i) {
        this.ex_i = ex_i;
    }

    private void setCurrExercise(Exercise exercise) {
        this.currExercise = exercise;
        if (getIsWarmup()) {
            this.finishedSets = new ArrayList<>();
        } else {
            if (this.finishedSets.size() - 1 == this.set_i) {
                this.finishedSets = new ArrayList<>();
            }
        }

        setCurrEquip(this.currExercise.getEquipment());

        if (this.set_i == -1) {
            this.set_i = 0;
        }
        setCurrExerciseSet(this.currExercise.getSet(this.set_i));
    }

    private void setCurrEquip(String equip) {
        if (BARBELL.equals(equip.toLowerCase())) {
            this.currMinWeight = BB_MIN_WEIGHT;
            this.currWeightChange = BB_WEIGHT_CHANGE;
        } else {
            this.currMinWeight = MIN_WEIGHT;
            this.currWeightChange = WEIGHT_CHANGE;
        }
    }

    private void setCurrExerciseSet(ExerciseSet exerciseSet) {
        this.currExerciseSet = exerciseSet;
        setCurrReps(this.currExerciseSet.getReps());
        this.currWeight = this.currExerciseSet.getWeight();
    }

    public ArrayList<Exercise> getWarmups() {
        return currWarmups;
    }

    public String getWorkoutName() {
        return currWorkout.getName();
    }

    public int getCurrNumExs() {
        return (this.currExercise.getSetsType() == WARMUP_SET) ? this.numWarmups : this.numMains;
    }

    public int getCurrExNum() {
        return ((this.currExercise.getSetsType() == WARMUP_SET)
                ? currWarmups.indexOf(currExercise)
                : currMains.indexOf(currExercise))
                + 1;
    }

    public String getCurrExName() {
        return currExercise.getName();
    }

    public String getCurrEquip() {
        return currExercise.getEquipment();
    }

    public int getCurrNumSets() {
        return currExercise.getNumSets();
    }

    public int getCurrSetNum() {
        return set_i + 1;
    }

    /* Reps------------------------------------------------------------------------------------- */
    public int getCurrReps() {
        return currExerciseSet.getReps();
    }

    public void incReps() {
        setCurrReps(++currReps);
    }

    public void decReps() {
        setCurrReps(--currReps);
    }

    public boolean setCurrReps(int reps) {
        this.currReps = reps;

        if (this.currExercise.getSetsType() == MAIN_SET) {
            setCurrRestTime();
        }

        return this.currReps == MIN_REPS;
    }

    private void setCurrRestTime() {
        this.currRestTime =  (this.currExerciseSet.getReps() <= 6) ? HEAVY_REST_TIME : LIGHT_REST_TIME;

        setTimer();
    }

    private void setTimer() {
        if (timerListener != null) {
            timerListener.startTimer(this.currRestTime);
            timerSet = true;
        } else {
            timerSet = false;
        }
    }

    public long getCurrRestTime() {
        return this.currRestTime;
    }
    /* Reps-end--------------------------------------------------------------------------------- */

    /* Weight----------------------------------------------------------------------------------- */
    public float getCurrWeight() {
        return this.currWeight;
    }

    public void incWeight() {
        setWeight(this.currWeight + this.currWeightChange);
    }

    public void decWeight() {
        setWeight(Math.max(this.currMinWeight, this.currWeight - this.currWeightChange));
    }

    public boolean setWeight(float weight) {
        this.currWeight = weight;

        return this.currWeight == this.currMinWeight;
    }

    public float getCurrMinWeight() {
        return currMinWeight;
    }
    /* Weight-end------------------------------------------------------------------------------- */


    public boolean getIsWarmup() {
        return WARMUP_SET.equals(this.currExercise.getSetsType());
    }

    public Session getCurrSession() {
        return this.currSession;
    }

    private void resetIndices() {
        this.ex_i = 0;
        this.set_i = 0;
    }

    void saveSessionState() {

        if (!getIsWarmup() && !finishedSets.isEmpty()) {
            this.currSession.addExerciseSets(this.currExercise, this.finishedSets, this.currWeightChange);
        }

        enablePrettyMapper();
        String jsonStr = writeValueAsString(this.currSession.sessionStateMap(
                ex_i,
                set_i
        ));

        if (!jsonStr.isEmpty()) {
            addIncompleteSessionPref(
                    context,
                    getWorkoutName(),
                    jsonStr
            );
        }

    }

    private void removeSessPrefAndSetWorkout() {
        if (removeIncompleteWorkoutPref(context, currWorkout.getName())) {
            removeIncompleteSessionPref(context, currWorkout.getName());
        }
    }
}
