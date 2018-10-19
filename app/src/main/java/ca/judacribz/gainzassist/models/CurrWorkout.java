package ca.judacribz.gainzassist.models;

import java.util.ArrayList;

import ca.judacribz.gainzassist.R;

import static ca.judacribz.gainzassist.models.Exercise.SetsType.*;
import static ca.judacribz.gainzassist.util.Calculations.getOneRepMax;

public class CurrWorkout {

    // Constants
    // --------------------------------------------------------------------------------------------
    private static final CurrWorkout INST = new CurrWorkout();

    private static final String BARBELL = "barbell";
    private static final float BB_MIN_WEIGHT = 45.0f;
    private static final float MIN_WEIGHT = 0.0f;
    private static final float BB_WEIGHT_CHANGE = 5.0f;
    private static final float WEIGHT_CHANGE = 2.5f;
    public static final int MIN_REPS = 0;
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    private Workout workout;
    private String workName, exName, exType, equip;
    private Set set;
    private int setNum, reps;
    private float weight, minWeight = MIN_WEIGHT, weightChange = WEIGHT_CHANGE;
    private boolean isWarmup = true;
    private ArrayList<Exercise> exercises, allExs;
    // --------------------------------------------------------------------------------------------

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
        this.exercises = workout.getExercises();

        genWarmups();
    }

    public void genWarmups() {
        allExs = new ArrayList<>();
        ArrayList<Set> sets;

        float oneRepMax, minWeight, weight, percWeight, newWeight;
        int reps, setNum;
        String equip;

        for (Exercise exercise: exercises) {
            exercise.setSetsType(MAIN_SET);
            sets = new ArrayList<>();
            setNum = 1;

            equip = exercise.getEquipment();
            minWeight = BARBELL.equals(equip.toLowerCase()) ? 45f : 0f;
            weight = exercise.getAvgWeight();
            if (weight == minWeight) {
                allExs.add(exercise);
                continue;
            }

            oneRepMax = getOneRepMax(exercise.getAvgReps(), exercise.getAvgWeight());
            reps = exercise.getAvgReps();

            percWeight = minWeight/weight;
            sets.add(new Set(setNum++, reps, minWeight));
            do {
                newWeight = percWeight*weight;
                newWeight -= newWeight % 5;
                sets.add(new Set(setNum++, reps, newWeight));

                percWeight += 0.2f;
                if (reps - 2 > 0)
                    reps -= 2;
                else if (reps - 1 > 0)
                    --reps;

            } while (percWeight < 0.8f);

            allExs.add(new Exercise(exercise.getName(), exercise.getType(), exercise.getEquipment(), sets, WARMUP_SET));
            allExs.add(exercise);
        }
    }

    public String getWorkName() {
        return workName;
    }

    public void setWorkName(String workName) {
        this.workName = workName;
    }

    public String getExName() {
        return exName;
    }

    public void setExName(String exName) {
        this.exName = exName;
    }

    public String getExType() {
        return exType;
    }

    public void setExType(String exType) {
        this.exType = exType;
    }

    public String getEquip() {
        return equip;
    }

    public void setEquip(String equip) {
        this.equip = equip.toLowerCase();
        if (BARBELL.equals(this.equip)) {
            this.minWeight = BB_MIN_WEIGHT;
            this.weightChange = BB_WEIGHT_CHANGE;
        } else {
            this.minWeight = MIN_WEIGHT;
            this.weightChange = WEIGHT_CHANGE;
        }
    }

    public Set getSet() {
        return set;
    }

    public void setSet(Set set) {
        this.setNum = set.getSetNumber();
        this.reps = set.getReps();
        this.weight = set.getWeight();
        this.set = set;
    }

    public int getSetNum() {
        return set.getSetNumber();
    }

    public int getReps() {
        return set.getReps();
    }

    public void incReps() {
        setReps(++reps);
    }

    public void decReps() {
        setReps(--reps);
    }

    public boolean setReps(int reps) {
        this.reps = reps;
        set.setReps(reps);

        return this.reps == MIN_REPS;
    }

    public float getWeight() {
        return set.getWeight();
    }

    public void incWeight() {
        setWeight(this.weight + this.weightChange);
    }

    public void decWeight() {
        setWeight(Math.max(minWeight, this.weight - weightChange));
    }

    public boolean setWeight(float weight) {
        this.weight = weight;
        set.setWeight(weight);

        return this.weight == minWeight;
    }

    public float getMinWeight() {
        return minWeight;
    }

    public boolean getIsWarmup() {
        return isWarmup;
    }

    public void switchSetType() {
        isWarmup = !isWarmup;
    }
}
