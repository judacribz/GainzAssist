package ca.judacribz.gainzassist.models;

import android.arch.persistence.room.*;
import android.support.annotation.Nullable;
import com.orhanobut.logger.Logger;
import org.parceler.Parcel;
import java.util.*;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static ca.judacribz.gainzassist.constants.ExerciseConst.*;

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
            BARBELL,
            DUMBBELL,
            NA
    ));
    @Ignore
    public static final ArrayList<String> EXERCISE_TYPES = new ArrayList<>(Arrays.asList(
            STRENGTH,
            CARDIOVASCULAR,
            PLYOMETRICS
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
    private float weightChange, minWeight;

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
                    @Nullable ArrayList<ExerciseSet> setsList,
                    SetsType setsType) {
        setExerciseBase(exerciseNumber, name, type, equipment);
        setSetsList(setsList);
        setSetsType(setsType);
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
        setExerciseBase(exerciseNumber, name, type, equipment);
        setSets(sets);
        setReps(reps);
        setWeight(weight);
        setSetsType(setsType);
    }

    private void setExerciseBase(int exerciseNumber,
                                String name,
                                String type,
                                String equipment) {
        setExerciseNumber(exerciseNumber);
        setName(name);
        setType(type);
        setEquipment(equipment);
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
        return this.workoutId;
    }

    public void setWorkoutId(int workoutId) {
        this.workoutId = workoutId;
    }

    public int getExerciseNumber() {
        return this.exerciseNumber;
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

        if (BARBELL.equals(this.equipment)) {
            minWeight = BB_MIN_WEIGHT;
            weightChange = BB_WEIGHT_CHANGE;
        } else if (DUMBBELL.equals(this.equipment)) {
            minWeight = DB_MIN_WEIGHT;
            weightChange = DB_WEIGHT_CHANGE;
        } else {
            minWeight = MIN_WEIGHT;
            weightChange = WEIGHT_CHANGE;
        }
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public int getReps() {
        return this.reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public float getWeight() {
        return this.weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getMinWeight() {
        return this.minWeight;
    }

    public float getWeightChange() {
        return this.weightChange;
    }

    public void setSetsType(SetsType setsType) {
        this.setsType = setsType;
    }

    public SetsType getSetsType() {
        return this.setsType;
    }

    public ArrayList<ExerciseSet> getSetsList() {
        return this.setsList;
    }

    public void setSetsList(@Nullable ArrayList<ExerciseSet> setsList) {
        if (setsList != null) {
            if (this.setsList.size() < setsList.size()) {
                setSetsList(null);
            }
            for (ExerciseSet set: setsList) {
                updateSet(set);
            }
        } else {
            for (int i = 0; i < sets; i++) {
                addSet(new ExerciseSet(id, name, i, reps, weight));
            }
        }
    }
    public void updateSet(ExerciseSet set) {
        this.setsList.set(set.getSetNumber(), set);

    }

    public void addSet(ExerciseSet exerciseSet) {
        this.setsList.add(exerciseSet);

        Logger.d("size of sets = " + this.setsList.size());
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
        return new HashMap<String, Object>() {{
            put("name", name);
            put("type", type);
            put("equipment", equipment);
            put("sets", sets);
            put("reps", reps);
            put("weight", weight);
        }};
    }

    public Map<String, Object> setsToMap() {
        Map<String, Object>
                setMap = new HashMap<>(),
                exMap = new HashMap<String, Object>() {{
                    put("id",      id);
                    put("name",      name);
                    put("type",      type);
                    put("equipment", equipment);
                }};

        for (ExerciseSet set : this.setsList) {
            setMap.put(String.valueOf(set.getSetNumber()), set.toMap());
        }

        exMap.put("setList", setMap);

        return exMap;
    }
}
