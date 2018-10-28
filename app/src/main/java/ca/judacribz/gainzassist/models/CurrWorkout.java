package ca.judacribz.gainzassist.models;

import android.app.Activity;
import ca.judacribz.gainzassist.activities.start_workout.fragments.WorkoutScreen;

import java.util.ArrayList;

import static ca.judacribz.gainzassist.models.Exercise.*;
import static ca.judacribz.gainzassist.models.Exercise.SetsType.*;
import static ca.judacribz.gainzassist.util.Calculations.getOneRepMax;

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
    public static final long HEAVY_REST_TIME = 180000;
    public static final long LIGHT_REST_TIME = 90000;
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    private Workout workout;
    private String workName, currExName, currExType, equip;
    private Set currSet;
    private int currSetNum, currReps;
    private float currWeight, currMinWeight = MIN_WEIGHT, currWeightChange = WEIGHT_CHANGE;
    private ArrayList<Exercise> currWarmups, exercises, allExs;
    private ArrayList<Set> currSets;
    private Exercise currExercise;
    private int set_i, ex_i, currNumSets, currNumWarmups, currNumExs, currNumAllExs;
    private SetsType currSetsType, prevSetsType;
    private long currRestTime;
    // --------------------------------------------------------------------------------------------

    private RestTimeSetListener restTimeSetListener;

    // Ideally, your interface should be declared inside ClassB.
    public interface RestTimeSetListener {
        public void startTimer(long timeInMillis);
    }

    public void setRestTimeSetListener(RestTimeSetListener restTimeSetListener) {
        this.restTimeSetListener = restTimeSetListener;
    }


    // ######################################################################################### //
    // WorkoutScreen Constructor/Instance                                                     //
    // ######################################################################################### //
    private CurrWorkout() {
    }

    public static CurrWorkout getInstance() {
        return INST;
    }

    public void setWorkout(Workout workout) {
        setWorkName(workout.getName());
        genWarmups(workout.getExercises());
    }

    private void setWorkName(String workName) {
        this.workName = workName;
    }

    private void genWarmups(ArrayList<Exercise> exercises) {
        ArrayList<Exercise> allExs = new ArrayList<>();
        ArrayList<Exercise> warmups = new ArrayList<>();
        Exercise warmup;
        ArrayList<Set> sets;

        float oneRepMax, minWeight, weight, percWeight, newWeight;
        int reps, setNum;
        String equip;

        this.exercises = exercises;
        this.currNumExs = exercises.size();

        for (Exercise exs: exercises) {
            exs.setSetsType(MAIN_SET);
            equip = exs.getEquipment();

            minWeight = BARBELL.equals(equip.toLowerCase()) ? BB_MIN_WEIGHT : MIN_WEIGHT;
            weight = exs.getAvgWeight();
            if (weight == minWeight) {
                allExs.add(exs);
                continue;
            }

            //Todo: use onerepmax
            oneRepMax = getOneRepMax(exs.getAvgReps(), exs.getAvgWeight());
            reps = exs.getAvgReps();

            setNum = 1;
            sets = new ArrayList<>();
            sets.add(new Set(setNum++, reps, minWeight));
            percWeight = minWeight / weight;
            do {
                newWeight = percWeight * weight;
                newWeight -= newWeight % 5;
                sets.add(new Set(setNum++, reps, newWeight));

                percWeight += 0.2f;
                if (reps - 2 > 0)
                    reps -= 2;
                else if (reps - 1 > 0)
                    --reps;

            } while (percWeight < 0.8f);

            warmup = new Exercise(exs.getName(), exs.getType(), exs.getEquipment(), sets, WARMUP_SET);
            warmups.add(warmup);
            allExs.add(warmup);
            allExs.add(exs);
        }

        setCurrWarmups(warmups);
        setCurrExercises(allExs);
    }

    private void setCurrWarmups(ArrayList<Exercise> warmups) {
        this.currWarmups = warmups;
        this.currNumWarmups = warmups.size();
    }

    private void setCurrExercises(ArrayList<Exercise> allExercises) {
        this.allExs = allExercises;
        this.currNumAllExs = allExercises.size();
        this.ex_i = 0;
        setCurrExercise(allExercises.get(this.ex_i));
    }

    public boolean finishCurrSet() {
        this.set_i++;

        if (atEndOfSets()) {
            this.ex_i++;

            if (atEndOfExercises()) {
                return false;
            } else {
                setCurrExercise(allExs.get(this.ex_i));
            }
        } else {
            setCurrSet(currSets.get(this.set_i));
        }

        return true;
    }

    private boolean atEndOfSets() {
        return this.set_i >= this.currNumSets;
    }

    private boolean atEndOfExercises() {
        return this.currNumAllExs == this.ex_i;
    }

    private void setCurrExercise(Exercise exercise) {
        this.currExercise = exercise;

        setCurrExName(this.currExercise.getName());
        setCurrEquip(this.currExercise.getEquipment());
        setCurrExType(this.currExercise.getType());

        setCurrSets(this.currExercise.getSets());
        setCurrSetsType(this.currExercise.getSetsType());
    }

    private void setCurrExName(String currExName) {
        this.currExName = currExName;
    }

    private void setCurrEquip(String equip) {
        this.equip = equip.toLowerCase();
        if (BARBELL.equals(this.equip)) {
            this.currMinWeight = BB_MIN_WEIGHT;
            this.currWeightChange = BB_WEIGHT_CHANGE;
        } else {
            this.currMinWeight = MIN_WEIGHT;
            this.currWeightChange = WEIGHT_CHANGE;
        }
    }

    private void setCurrExType(String exType) {
        this.currExType = exType;
    }

    private void setCurrSets(ArrayList<Set> currSets) {
        this.currSets = currSets;
        this.currNumSets = currSets.size();
        this.set_i = 0;
        setCurrSet(this.currSets.get(this.set_i));
    }

    private void setCurrSetsType(SetsType setsType) {
        this.prevSetsType = this.currSetsType;
        this.currSetsType = setsType;
    }

    private void setCurrSet(Set set) {
        this.currSet = set;
        this.currSetNum = this.currSet.getSetNumber();
        setCurrReps(this.currSet.getReps());
        this.currWeight = this.currSet.getWeight();
    }

    public String getWorkName() {
        return workName;
    }

    public ArrayList<Exercise> getWarmups() {
        return currWarmups;
    }

    public int getCurrNumExs() {
        if (this.currSetsType == WARMUP_SET) {
            return this.currNumWarmups;
        }

        return this.currNumExs;
    }

    public int getCurrExNum() {
        if (this.currSetsType == WARMUP_SET) {
            return currWarmups.indexOf(currExercise) + 1;
        }

        return exercises.indexOf(currExercise) + 1;
    }

    public String getCurrExName() {
        return currExName;
    }

    public SetsType getCurrExType() {
        return currSetsType;
    }

    public String getCurrEquip() {
        return equip;
    }

    public int getCurrNumSets() {
        return currNumSets;
    }

    public Set getCurrSet() {
        return currSet;
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

        if (this.currSetsType == MAIN_SET) {
            setCurrRestTime();
        }

        return this.currReps == MIN_REPS;
    }
    public void setCurrRestTime() {
        this.currRestTime =  (this.currReps <= 6) ? HEAVY_REST_TIME : LIGHT_REST_TIME;
        restTimeSetListener.startTimer(this.currRestTime);
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
        return WARMUP_SET.equals(currExercise.getSetsType());
    }


    public void reset() {
        this.ex_i = 0;
        this.set_i = 0;
    }
}
