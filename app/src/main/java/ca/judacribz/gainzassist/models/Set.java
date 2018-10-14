package ca.judacribz.gainzassist.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class Set implements Parcelable {

    // Global Vars
    // --------------------------------------------------------------------------------------------
    private int setNumber, reps;
    private float weight;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // Set Constructors                                                                          //
    // ######################################################################################### //
    /* Required empty constructor for firebase */
    public Set() {
    }

    public Set(int setNumber, int reps, float weight) {
        this.setNumber = setNumber;
        this.reps = reps;
        this.weight = weight;
    }
    // ######################################################################################### //

    // Getters and setters
    // ============================================================================================
    public int getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(int setNumber) {
        this.setNumber = setNumber;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
    // ============================================================================================


    // Helper function used to store Set information in the firebase db
    Map<String, Object> toMap() {
        Map<String, Object> set = new HashMap<>();

        set.put("reps", reps);
        set.put("weight", weight);

        return set;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.setNumber);
        dest.writeInt(this.reps);
        dest.writeFloat(this.weight);
    }

    protected Set(Parcel in) {
        this.setNumber = in.readInt();
        this.reps = in.readInt();
        this.weight = in.readFloat();
    }

    public static final Parcelable.Creator<Set> CREATOR = new Parcelable.Creator<Set>() {
        @Override
        public Set createFromParcel(Parcel source) {
            return new Set(source);
        }

        @Override
        public Set[] newArray(int size) {
            return new Set[size];
        }
    };
}