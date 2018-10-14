package ca.judacribz.gainzassist.models;

import android.support.annotation.Nullable;

import java.util.ArrayList;

import ca.judacribz.gainzassist.R;

import static android.support.v4.content.res.TypedArrayUtils.getString;

public class CurrSet {

    // Constants
    // --------------------------------------------------------------------------------------------
    private static final CurrSet INST = new CurrSet();
    private static final String BARBELL = "barbell";
    private static final float BB_MIN_WEIGHT = 45.0f;
    private static final float MIN_WEIGHT = 0.0f;
    private static final float BB_WEIGHT_CHANGE = 5.0f;
    private static final float WEIGHT_CHANGE = 2.5f;
    public static final int MIN_REPS = 0;

    public static final int MIN_REACHED = 1001;
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    private String workName, exName, exType, equip;
    private Set set;
    private int setNum, reps;
    private float weight, minWeight = MIN_WEIGHT, weightChange = WEIGHT_CHANGE;

    private boolean isWarmup = true;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // CurrSet Constructor/Instance                                                     //
    // ######################################################################################### //
    private CurrSet() {
    }

    public static CurrSet getInstance() {
        return INST;
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

    public void setType(boolean type) {
        isWarmup = type;
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
