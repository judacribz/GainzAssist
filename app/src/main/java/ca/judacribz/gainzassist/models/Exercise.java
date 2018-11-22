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
                indices = {@Index(value = {"workout_id", "exercise_number"}, unique = true)})
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

    @ColumnInfo(name = "exercise_number")
    int exerciseNumber;

    String name;
    String type;
    String equipment;
    int sets;
    int reps;
    float weight;

    @Ignore
    ArrayList<ExerciseSet> setsList = new ArrayList<>();

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

    @Ignore
    public Exercise(int exerciseNumber,
                    String name,
                    String type,
                    String equipment,
                    ArrayList<ExerciseSet> setsList,
                    SetsType setsType) {
        this.exerciseNumber = exerciseNumber;
        this.name           = name;
        this.type           = type;
        this.equipment      = equipment;
        this.setsList       = setsList;
        setSetsType(setsType);
    }

    @Ignore
    public Exercise(int exerciseNumber,
                    String name,
                    String type,
                    String equipment,
                    int sets,
                    int reps,
                    float weight) {
        this.exerciseNumber = exerciseNumber;
        this.name           = name;
        this.type           = type;
        this.equipment      = equipment;
        this.sets           = sets;
        this.reps           = reps;
        this.weight         = weight;

        setSetsList(null);
    }

    @Ignore
    public Exercise(int exerciseNumber,
                    String name,
                    String type,
                    String equipment,
                    int sets,
                    int reps,
                    float weight,
                    SetsType setsType) {
        this(exerciseNumber, name, type, equipment, sets, reps, weight);
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

    public int getExerciseNumber() {
        return exerciseNumber;
    }

    public void setExerciseNumber(int exerciseNumber) {
        this.exerciseNumber = exerciseNumber;
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


    public ArrayList<ExerciseSet> getSetsList() {
        return setsList;
    }

    public void setSetsList(@Nullable ArrayList<ExerciseSet> setsList) {
        if (setsList != null) {
            this.setsList = setsList;
        } else {
            for (int i = 0; i < sets; i++) {
                this.setsList.add(new ExerciseSet(id, name, i+1, reps, weight));
            }
        }
    }

    public void addSet(ExerciseSet exerciseSet) {
        this.setsList.add(exerciseSet);
    }

    public float getAvgWeight() {
        float weight = 0.0f;
        for (ExerciseSet exerciseSet : setsList) {
            weight += exerciseSet.getWeight();
        }

        return weight / (float) getSets();
    }

    public int getAvgReps() {
        int reps = 0;
        for (ExerciseSet exerciseSet : setsList) {
            reps += exerciseSet.getReps();
        }

        return reps / getSets();
    }

    public ExerciseSet getSet(int setIndex) {
        return this.setsList.get(setIndex);
    }

    public int getNumSets() {
        return this.setsList.size();
    }
    // ============================================================================================


    /* Misc function used to store Exercise information in the firebase db */
    public Map<String, Object> toMap() {
        Map<String, Object> exercise = new HashMap<>();

        exercise.put("name",      name);
        exercise.put("type",      type);
        exercise.put("equipment", equipment);
        exercise.put("sets", sets);
        exercise.put("reps", reps);
        exercise.put("weight", weight);


        return exercise;
    }

    public Map<String, Object> setsToMap() {
        Map<String, Object>
                exercise = new HashMap<>(),
                setMap = new HashMap<>();

        exercise.put("name",      name);
        exercise.put("type",      type);
        exercise.put("equipment", equipment);

        for (ExerciseSet set : setsList) {
            setMap.put(String.valueOf(set.getSetNumber()), set.toMap());
        }

        exercise.put("sets", setMap);


        return exercise;
    }
}
