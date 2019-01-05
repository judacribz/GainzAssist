package ca.judacribz.gainzassist.models.db;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.ExerciseSet;
import ca.judacribz.gainzassist.models.Session;
import ca.judacribz.gainzassist.models.Workout;

import java.util.List;

public class WorkoutViewModel extends AndroidViewModel {
    private WorkoutRepo workoutRepo;

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

    public void insertSession(Session... sessions) {
        for (Session session : sessions)
            workoutRepo.insertSession(session, true);
    }

    public void insertSet(ExerciseSet... exerciseSets) {
        for (ExerciseSet exerciseSet : exerciseSets)
            workoutRepo.insertSet(exerciseSet);
    }
    // --------------------------------------------------------------------------------------------


    // RETRIEVE
    // --------------------------------------------------------------------------------------------
    public LiveData<List<Workout>> getAllWorkouts() {
        return workoutRepo.getAllWorkoutsLive();
    }
    public LiveData<List<Session>> getAllSessions() {
        return workoutRepo.getAllSessionsLive();
    }
    public LiveData<List<ExerciseSet>> getAllSets() {
        return workoutRepo.getAllSetsLive();
    }

    public LiveData<Workout> getWorkout(int id) {
        return workoutRepo.getWorkout(id);
    }

    public void getWorkoutFromName(Context context, String name) {
        workoutRepo.getWorkoutFromName(context, name);
    }

    public LiveData<List<Exercise>> getExercisesFromWorkout(int workoutId) {
        return workoutRepo.getExercisesFromWorkout(workoutId);
    }

    public LiveData<Exercise> getExercise(long id) {
        return workoutRepo.getExercise(id);
    }

    public LiveData<List<String>> getAllUniqueExerciseNames() {
        return workoutRepo.getAllUniqueExerciseNames();
    }

    public LiveData<List<ExerciseSet>> getSetsFromExercise(long exerciseId) {
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

    void updateSet(ExerciseSet exerciseSet) {
        workoutRepo.updateSet(exerciseSet);
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

    public void deleteSet(ExerciseSet exerciseSet) {
        workoutRepo.deleteSet(exerciseSet);
    }
    // --------------------------------------------------------------------------------------------
}
