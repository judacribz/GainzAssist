package ca.judacribz.gainzassist.activities.start_workout;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.Session;
import ca.judacribz.gainzassist.models.Set;
import ca.judacribz.gainzassist.models.Workout;
import ca.judacribz.gainzassist.models.db.WorkoutViewModel;

import java.util.ArrayList;

import static ca.judacribz.gainzassist.models.Exercise.SetsType.*;
import static ca.judacribz.gainzassist.util.Calculations.getOneRepMax;
import static ca.judacribz.gainzassist.util.firebase.Database.addWorkoutSessionFirebase;

public class CurrWorkout {

    // Constants
    // --------------------------------------------------------------------------------------------
    private static final CurrWorkout INST = new CurrWorkout();

    private static final String BARBELL = "barbell";
    public static final float BB_MIN_WEIGHT = 45.0f;
    public static final float MIN_WEIGHT = 0.0f;
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

    private Set currSet;
    private float
            currWeight,
            currMinWeight = MIN_WEIGHT,
            currWeightChange = WEIGHT_CHANGE;

    private ArrayList<Exercise>
            currWarmups,
            currMains;

    private int
            set_i,
            ex_i,
            currReps,
            numWarmups,
            numMains;
    private long currRestTime;

    private Session currSession;

    private ArrayList<Set> finishedSets = new ArrayList<>();

    private Context context;
    // --------------------------------------------------------------------------------------------

    private RestTimeSetListener restTimeSetListener;


    public interface RestTimeSetListener {
        public void startTimer(long timeInMillis);
    }

    public void setRestTimeSetListener(RestTimeSetListener restTimeSetListener) {
        this.restTimeSetListener = restTimeSetListener;
        //TODO make deterministic
        if (!timerSet) {
            setTimer();
        }
    }


    // ######################################################################################### //
    // WorkoutScreen Constructor/Instance                                                        //
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


    void setCurrWorkout(Workout workout) {
        this.currWorkout = workout;
        this.currSession = new Session(workout);
        genWarmups(workout.getExercises());
    }

    private void genWarmups(ArrayList<Exercise> exercises) {
        ArrayList<Exercise> allExs = new ArrayList<>();
        ArrayList<Exercise> warmups = new ArrayList<>();
        Exercise warmup;
        ArrayList<Set> sets;
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
            sets = new ArrayList<>();
            sets.add(new Set(ex, setNum++, reps, minWeight));
            percWeight = minWeight / weight;
            do {
                newWeight = percWeight * weight;
                newWeight -= newWeight % 5;
                sets.add(new Set(ex, setNum++, reps, newWeight));

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
                    sets,
                    WARMUP_SET
            );
            warmups.add(warmup);
            allExs.add(warmup);
            allExs.add(ex);
        }

        setCurrWarmupExercises(warmups);
        setCurrExercises(allExs);
    }

    private void setCurrMainExercises(ArrayList<Exercise> exercises) {
        this.currMains = exercises;
        this.numMains = exercises.size();
    }

    private void setCurrWarmupExercises(ArrayList<Exercise> exercises) {
        this.currWarmups = exercises;
        this.numWarmups = exercises.size();
    }

    private void setCurrExercises(ArrayList<Exercise> allExercises) {
        this.currWorkout.setExercises(allExercises);
        this.ex_i = 0;
        setCurrExercise(currWorkout.getExercise(this.ex_i));
    }

    public boolean finishCurrSet() {
        this.set_i++;

        if (currExercise.getSetsType() == MAIN_SET) {
            currSet.setReps(currReps);
            currSet.setWeight(currWeight);
            finishedSets.add(currSet);
        }

        // End of sets for an exercise
        if (atEndOfSets()) {
            this.ex_i++;

            currSession.addExerciseSets(currExercise.getName(), finishedSets, currWeightChange);

            // End of all exercises for this workout session
            if (atEndOfExercises()) {
                currSession.setTimestamp(-1);

                ViewModelProviders.of((FragmentActivity) context).get(WorkoutViewModel.class).insertSession(currSession);

                return false;

            // Not end of all exercises, set next exercise
            } else {
                setCurrExercise(currWorkout.getExercise(this.ex_i));
            }

            if (currExercise.getSetsType() == MAIN_SET) {
                finishedSets = new ArrayList<>();
            }

        // End of set, set next set from current exercise
        } else {
            setCurrSet(currExercise.getSet(this.set_i));
        }

        return true;
    }

    private boolean atEndOfSets() {
        return this.set_i >= this.currExercise.getNumSets();
    }

    private boolean atEndOfExercises() {
        return (this.ex_i >= this.currWorkout.getNumExercises());
    }

    private void setCurrExercise(Exercise exercise) {
        this.currExercise = exercise;
        setCurrEquip(this.currExercise.getEquipment());

        this.set_i = 0;
        setCurrSet(this.currExercise.getSet(this.set_i));
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

    private void setCurrSet(Set set) {
        this.currSet = set;
        setCurrReps(this.currSet.getReps());
        this.currWeight = this.currSet.getWeight();
    }

    public ArrayList<Exercise> getWarmups() {
        return currWarmups;
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
        return currSet.getReps();
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
    boolean timerSet;
    private void setCurrRestTime() {
        this.currRestTime =  (this.currSet.getReps() <= 6) ? HEAVY_REST_TIME : LIGHT_REST_TIME;

        setTimer();
    }

    private void setTimer() {
        if (restTimeSetListener != null) {
            restTimeSetListener.startTimer(this.currRestTime);
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


    void reset() {
        this.ex_i = 0;
        this.set_i = 0;
    }
}
