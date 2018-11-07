package ca.judacribz.gainzassist.models;

import android.arch.persistence.room.*;
import android.support.annotation.Nullable;
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
    int sets;
    int reps;
    float weight;

    @Ignore
    ArrayList<Set> setsList = new ArrayList<>();

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

    public Exercise(String name, String type, String equipment, ArrayList<Set> setsList, SetsType setsType) {
        this.name      = name;
        this.type      = type;
        this.equipment = equipment;
        this.setsList  = setsList;
        setSetsType(setsType);
    }


    public Exercise(String name, String type, String equipment, int sets, int reps, float weight) {
        this.name      = name;
        this.type      = type;
        this.equipment = equipment;
        this.sets      = sets;
        this.reps      = reps;
        this.weight    = weight;

        setSetsList(null);
    }

    public Exercise(String name, String type, String equipment, int sets, int reps, float weight, SetsType setsType) {
        this(name, type, equipment, sets, reps, weight);
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

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
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

    public void setSetsType(SetsType setsType) {
        this.setsType = setsType;
    }

    public SetsType getSetsType() {
        return setsType;
    }


    public ArrayList<Set> getSetsList() {
        return setsList;
    }

    public void setSetsList(@Nullable ArrayList<Set> setsList) {
        if (setsList != null) {
            this.setsList = setsList;
        } else {
            for (int i = 0; i < sets; i++) {
                this.setsList.add(new Set(i+1, reps, weight));
            }
        }
    }

    public void addSet(Set set) {
        this.setsList.add(set);
    }

    public float getAvgWeight() {
        float weight = 0.0f;
        for (Set set : setsList) {
            weight += set.getWeight();
        }

        return weight / (float) getSets();
    }

    public int getAvgReps() {
        int reps = 0;
        for (Set set : setsList) {
            reps += set.getReps();
        }

        return reps / getSets();
    }
    // ============================================================================================


    /* Helper function used to store Exercise information in the firebase db */
    Map<String, Object> toMap() {
        Map<String, Object> exercise = new HashMap<>();

        exercise.put("name",      name);
        exercise.put("type",      type);
        exercise.put("equipment", equipment);
        exercise.put("sets", sets);
        exercise.put("reps", reps);
        exercise.put("weight", weight);


        return exercise;
    }
}
