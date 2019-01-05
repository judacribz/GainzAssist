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
                onUpdate = CASCADE,
                onDelete = CASCADE),
                indices = {@Index(value = {"workout_id", "exercise_number"})})
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
    @PrimaryKey
    long id = -1;

    @ColumnInfo(name = "workout_id")
    long workoutId = -1;

    @ColumnInfo(name = "exercise_number")
    int exerciseNumber;

    String name;
    String type;
    String equipment;
    int sets;
    int reps;
    float weight;

    @Ignore
    float weightChange, minWeight;

    @Ignore
    ArrayList<ExerciseSet> setsList = new ArrayList<>();
    @Ignore
    ArrayList<ExerciseSet> finSets = new ArrayList<>();

    public enum SetsType {
        WARMUP_SET,
        MAIN_SET
    }

    @Ignore
    SetsType setsType = SetsType.MAIN_SET;
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
        setId(-1);
        setExerciseNumber(exerciseNumber);
        setName(name);
        setType(type);
        setEquipment(equipment);
    }
    // ============================================================================================

    // Getters and setters
    // ============================================================================================
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = (id == -1) ? new Date().getTime() : id;
    }

    public long getWorkoutId() {
        return this.workoutId;
    }

    public void setWorkoutId(long workoutId) {
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
            Logger.d("SET MIN WEIGHT");
            this.minWeight = BB_MIN_WEIGHT;
            this.weightChange = BB_WEIGHT_CHANGE;
        } else if (DUMBBELL.equals(this.equipment)) {
            this.minWeight = DB_MIN_WEIGHT;
            this.weightChange = DB_WEIGHT_CHANGE;
        } else {
            this.minWeight = MIN_WEIGHT;
            this.weightChange = WEIGHT_CHANGE;
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

    public ArrayList<ExerciseSet> getFinishedSetsList() {
        return this.finSets;
    }

    public void setSetsList(@Nullable ArrayList<ExerciseSet> setsList) {
        if (setsList != null) {
            this.setsList = setsList;
        } else {
            for (int i = 0; i < sets; i++) {
                this.setsList.add(new ExerciseSet(id, name, i, reps, weight));
            }
        }
    }

    public void updateSet(ExerciseSet set) {
        this.finSets.set(set.getSetNumber(), set);
    }

    public void addSet(ExerciseSet set, boolean genId) {
        if (genId) {
            set.setId(-1);
        }
        this.finSets.add(set);
    }

    public float getAvgWeight() {
        float weight = 0.0f;
        for (ExerciseSet exerciseSet : setsList) {
            weight += exerciseSet.getWeight();
        }
Logger.d("setlist size " + setsList.size());
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
            put("id",        id);
            put("name",      name);
            put("type",      type);
            put("equipment", equipment);
            put("sets",      sets);
            put("reps",      reps);
            put("weight",    weight);
        }};
    }

    public Map<String, Object> setsToMap() {
        Map<String, Object>
                setMap = new HashMap<>(),
                exMap = toMap();

        for (ExerciseSet set : this.finSets) {
            setMap.put(String.valueOf(set.getSetNumber()), set.toMap());
        }

        exMap.put("setList", setMap);

        return exMap;
    }
}
