package ca.judacribz.gainzassist.models;

import android.arch.persistence.room.*;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "sets",
        primaryKeys = {"set_number", "exercise_id"},
        foreignKeys = @ForeignKey(
                entity = Exercise.class,
                parentColumns = "id",
                childColumns = "exercise_id",
                onDelete = CASCADE))
public class Set implements Parcelable {

    // Global Vars
    // --------------------------------------------------------------------------------------------
    @ColumnInfo(name = "set_number")
    private int setNumber;
    @ColumnInfo(name = "exercise_id")
    private long exerciseId;
    private int reps;
    private float weight;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // Set Constructors                                                                          //
    // ######################################################################################### //
    /* Required empty constructor for firebase */
    @Ignore
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

    long getExerciseId() {
        return exerciseId;
    }

    void setExerciseId(long exerciseId) {
        this.exerciseId = exerciseId;
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