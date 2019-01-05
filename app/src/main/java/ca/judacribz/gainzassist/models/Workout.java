package ca.judacribz.gainzassist.models;

import android.arch.persistence.room.*;
import org.parceler.Parcel;
import android.support.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static ca.judacribz.gainzassist.util.Misc.exerciseToMap;

@Parcel
@Entity(tableName = "workouts",
        indices = {@Index(value = {"name"}, unique = true)})
public class Workout {

    // Global Vars
    // --------------------------------------------------------------------------------------------
    @PrimaryKey
    long id = -1;
    String name;

    @Ignore
    ArrayList<Exercise> exercises = new ArrayList<>();
    // --------------------------------------------------------------------------------------------


    // ######################################################################################### //
    // ExerciseConst Constructor                                                                 //
    // ######################################################################################### //
    public Workout() {
    }


    public Workout(String name, @Nullable ArrayList<Exercise> exercises) {
        if (id == -1)
            setId(-1);
        setName(name);
        if (exercises != null){
            this.exercises = exercises;
        }
    }
    // ######################################################################################### //


    // Getters and setters
    // ============================================================================================
    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = (id == -1) ? new Date().getTime() : id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addExercise(Exercise exercise) {
        if (this.id != -1) {
            exercise.setWorkoutId(id);
        }
        this.exercises.add(exercise);
    }

    public void removeExercise(Exercise exercise) {
        exercises.remove(exercise);
        for (Exercise ex : exercises) {
            ex.setExerciseNumber(exercises.indexOf(ex));
        }
    }

    public int getExerciseNumber(String exerciseName) {
        Exercise ex = getExerciseFromName(exerciseName);

        return (ex != null) ? ex.getExerciseNumber() : -1;
    }

    public Exercise getExerciseFromName(String exName) {
        for (Exercise exercise : exercises) {
            if (exercise.getName().equals(exName)) {
                return exercise;
            }
        }

        return null;
    }

    public Exercise getExerciseFromIndex(int exIndex) {
        return this.exercises.get(exIndex);
    }

    public void setExercises(ArrayList<Exercise> exercises) {
        this.exercises = exercises;
    }

    public ArrayList<Exercise> getExercises() {
        return exercises;
    }

    public String getName() {
        return this.name;
    }
    // ============================================================================================


    // Misc functions
    // --------------------------------------------------------------------------------------------
    /* Misc function used to store ExerciseConst information in the FireBase db */
    public Map<String, Object> toMap() {
        Map<String, Object> workout = new HashMap<>();
        Map<String, Object> exs = exerciseToMap(exercises);

        workout.put("id", id);
        workout.put("exercises", exs);

        return workout;
    }


    /* Returns true if the exercise exist, false if not */
    public boolean containsExercise(String exerciseName) {
        for (Exercise exercise : exercises) {
            if (exercise.getName().toLowerCase().equals(exerciseName.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    public ArrayList<String> getExerciseNames() {
        ArrayList<String> exerciseNames = new ArrayList<>();
        for (Exercise exercise : exercises) {
            exerciseNames.add(exercise.getName());
        }

        return  exerciseNames;
    }



    public int getNumExercises() {
        return this.exercises.size();
    }

    public boolean exerciseAtNumExists(int i) {
        for (Exercise ex : exercises) {
            if (ex.getExerciseNumber() == i) {
                return true;
            }
        }

        return  false;
    }
    // --------------------------------------------------------------------------------------------
}