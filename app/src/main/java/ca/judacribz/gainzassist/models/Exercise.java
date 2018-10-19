package ca.judacribz.gainzassist.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Exercise implements Parcelable {

    // Global Vars
    // --------------------------------------------------------------------------------------------
    private String name, type, equipment;
    private ArrayList<Set> sets;

    public enum SetsType {
        WARMUP_SET,
        MAIN_SET
    }
    private SetsType setsType = null;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // Exercise Constructors                                                                     //
    // ######################################################################################### //
    // ######################################################################################### //
    public Exercise() {
        /* Required empty constructor for Firebase */
    }

    public Exercise(String name, String type, String equipment, ArrayList<Set> sets) {
        this.name      = name;
        this.type      = type;
        this.equipment = equipment;
        this.sets      = sets;
    }

    public Exercise(String name, String type, String equipment, ArrayList<Set> sets, SetsType setsType) {
        this(name, type, equipment, sets);
        setSetsType(setsType);
    }
    // ============================================================================================

    // Getters and setters
    // ============================================================================================
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public ArrayList<Set> getSets() {
        return sets;
    }

    public void setSets(ArrayList<Set> sets) {
        this.sets = sets;
    }

    public int getNumSets() {
        return sets.size();
    }

    public void setSetsType(SetsType setsType) {
        this.setsType = setsType;
    }

    public SetsType getSetsType() {
        return setsType;
    }

    public float getAvgWeight() {
        float weight = 0.0f;
        for (Set set : sets) {
            weight += set.getWeight();
        }

        return weight / (float) getNumSets();
    }

    public int getAvgReps() {
        int reps = 0;
        for (Set set : sets) {
            reps += set.getReps();
        }

        return reps / getNumSets();
    }
    // ============================================================================================


    /* Helper function used to store Exercise information in the firebase db */
    Map<String, Object> toMap() {
        Map<String, Object> exercise = new HashMap<>();

        exercise.put("type",      type);
        exercise.put("equipment", equipment);

        Map<String, Object> setMap = new HashMap<>();

        for (Set set: sets) {
            setMap.put(String.valueOf(set.getSetNumber()), set.toMap());
        }

        exercise.put("sets", setMap);

        return exercise;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.type);
        dest.writeString(this.equipment);
        dest.writeTypedList(this.sets);
    }

    protected Exercise(Parcel in) {
        this.name = in.readString();
        this.type = in.readString();
        this.equipment = in.readString();
        this.sets = in.createTypedArrayList(Set.CREATOR);
    }

    public static final Parcelable.Creator<Exercise> CREATOR = new Parcelable.Creator<Exercise>() {
        @Override
        public Exercise createFromParcel(Parcel source) {
            return new Exercise(source);
        }

        @Override
        public Exercise[] newArray(int size) {
            return new Exercise[size];
        }
    };
}
