package ca.judacribz.gainzassist.models;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

public class WorkoutViewModel extends AndroidViewModel {
    private WorkoutRepo workoutRepo;
    private LiveData<List<Workout>> workouts;

    public WorkoutViewModel(Application app) {
        super(app);
        workoutRepo = new WorkoutRepo(app);
    }

    // CREATE
    // --------------------------------------------------------------------------------------------
    public void insertWorkout(Workout... workouts) {
        for (Workout workout : workouts)
            workoutRepo.insertWorkout(workout);
    }

    public void insertExercise(Exercise... exercises) {
        for (Exercise exercise : exercises)
            workoutRepo.insertExercise(exercise);
    }

    public void insertSet(Set... sets) {
        for (Set set : sets)
            workoutRepo.insertSet(set);
    }
    // --------------------------------------------------------------------------------------------


    // RETRIEVE
    // --------------------------------------------------------------------------------------------
    public LiveData<List<Workout>> getAllWorkouts() {
        return workoutRepo.getAllWorkouts();
    }

    public LiveData<Workout> getWorkout(long id) {
        return workoutRepo.getWorkout(id);
    }

    public LiveData<Workout> getWorkoutFromName(String name) {
        return workoutRepo.getWorkoutFromName(name);
    }

    public LiveData<List<Exercise>> getExercisesFromWorkout(long workoutId) {
        return workoutRepo.getExercisesFromWorkout(workoutId);
    }

    public LiveData<Exercise> getExercise(long id) {
        return workoutRepo.getExercise(id);
    }

    public LiveData<List<String>> getAllUniqueExerciseNames() {
        return workoutRepo.getAllUniqueExerciseNames();
    }

    public LiveData<List<Set>> getSetsFromExercise(long exerciseId) {
        return workoutRepo.getSetsFromExercise(exerciseId);
    }

    // --------------------------------------------------------------------------------------------


    // UPDATE
    // --------------------------------------------------------------------------------------------
    void updateWorkout(Workout workout) {
        workoutRepo.updateWorkout(workout);
    }

    void updateExercise(Exercise exercise) {
        workoutRepo.updateExercise(exercise);
    }

    void updateSet(Set set) {
        workoutRepo.updateSet(set);
    }
    // --------------------------------------------------------------------------------------------


    // DELETE
    // --------------------------------------------------------------------------------------------
    public void deleteAllWorkouts() {
        workoutRepo.deleteAllWorkouts();
    }

    public void deleteWorkout(Workout workout) {
        workoutRepo.deleteWorkout(workout);
    }

    public void deleteExercise(Exercise exercise) {
        workoutRepo.deleteExercise(exercise);
    }

    public void deleteSet(Set set) {
        workoutRepo.deleteSet(set);
    }
    // --------------------------------------------------------------------------------------------
}
