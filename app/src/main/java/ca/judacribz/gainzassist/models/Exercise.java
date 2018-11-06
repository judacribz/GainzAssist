package ca.judacribz.gainzassist.models;

import android.arch.persistence.room.*;
import org.parceler.Parcel;

import java.util.*;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Parcel
@Entity(tableName = "exercises",
        foreignKeys = @ForeignKey(
                entity = Workout.class,
                parentColumns = "id",
                childColumns = "workout_id",
                onDelete = CASCADE),
                indices = {@Index(value = {"workout_id", "name"}, unique = true)})
public class Exercise {

    @Ignore
    public static final ArrayList<String> EQUIPMENT_TYPES = new ArrayList<>(Arrays.asList(
            "Barbell",
            "Dumbbell",
            "N/A"
    ));
    @Ignore
    public static final ArrayList<String> EXERCISE_TYPES = new ArrayList<>(Arrays.asList(
            "Strength",
            "Cardiovascular",
            "Plyometrics"
    ));

    // Global Vars
    // --------------------------------------------------------------------------------------------
    @PrimaryKey(autoGenerate = true)
    int id;

    @ColumnInfo(name = "workout_id")
    int workoutId;
    String name;
    String type;
    String equipment;

    @Ignore
    ArrayList<Set> sets = new ArrayList<>();

    public enum SetsType {
        WARMUP_SET,
        MAIN_SET
    }
    @Ignore
    SetsType setsType = null;
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
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(int workoutId) {
        this.workoutId = workoutId;
    }
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

    public void addSet(Set set) {
        this.sets.add(set);
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

        exercise.put("name",      name);
        exercise.put("type",      type);
        exercise.put("equipment", equipment);

        Map<String, Object> setMap = new HashMap<>();

        for (Set set: sets) {
            setMap.put(String.valueOf(set.getSetNumber()), set.toMap());
        }

        exercise.put("sets", setMap);

        return exercise;
    }
}
