package ca.judacribz.gainzassist.models;

import android.app.Application;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static ca.judacribz.gainzassist.models.WorkoutRepo.RepoTask.*;

public class WorkoutRepo {
    private WorkoutDao workoutDao;
    private ExerciseDao exerciseDao;
    private SetDao setDao;
    private LiveData<List<Workout>> workouts;

    public enum RepoTask {
        INSERT_EXERCISE,
        INSERT_WORKOUT,
        INSERT_SET,

        UPDATE_WORKOUT,
        UPDATE_EXERCISE,
        UPDATE_SET,

        DELETE_ALL_WORKOUTS,
        DELETE_WORKOUT,
        DELETE_EXERCISE,
        DELETE_SET
    }

    public WorkoutRepo(Application app) {
        WorkoutDatabase db = WorkoutDatabase.getDatabase(app);

        workoutDao = db.workoutDao();
        exerciseDao = db.exerciseDao();
        setDao = db.setDao();
    }

    // CRUD functions
    // ============================================================================================
    // CREATE
    // --------------------------------------------------------------------------------------------
    public void insertWorkout(Workout workout) {
        new RepoAsyncTask(workoutDao, exerciseDao, workout).execute(INSERT_WORKOUT);
    }

    void insertExercise(Exercise exercise) {
        new RepoAsyncTask(exerciseDao, exercise).execute(INSERT_EXERCISE);
    }

    void insertSet(Set set) {
        new RepoAsyncTask(setDao, set).execute(INSERT_SET);
    }
    // --------------------------------------------------------------------------------------------


    // RETRIEVE
    // --------------------------------------------------------------------------------------------
    LiveData<List<Workout>> getAllWorkouts() {
        return workoutDao.getAll();
    }

    LiveData<Workout> getWorkout(int id) {
        return workoutDao.get(id);
    }

    LiveData<Workout> getWorkoutFromName(String name) {
        return workoutDao.getFromName(name);
    }

    LiveData<List<Exercise>> getExercisesFromWorkout(int workoutId) {
        return exerciseDao.getFromWorkout(workoutId);
    }

    LiveData<Exercise> getExercise(int id) {
        return exerciseDao.get(id);
    }

    LiveData<List<String>> getAllUniqueExerciseNames() {
        return exerciseDao.getAllUniqueNames();
    }

    LiveData<List<Set>> getSetsFromExercise(int exerciseId) {
        return setDao.getSetsFromExercise(exerciseId);
    }
    // --------------------------------------------------------------------------------------------


    // UPDATE
    // --------------------------------------------------------------------------------------------
    void updateWorkout(Workout workout) {
        new RepoAsyncTask(workoutDao, exerciseDao, workout).execute(UPDATE_WORKOUT);
    }

    void updateExercise(Exercise exercise) {
        new RepoAsyncTask(exerciseDao, exercise).execute(UPDATE_EXERCISE);
    }

    void updateSet(Set set) {
        new RepoAsyncTask(setDao, set).execute(UPDATE_SET);
    }
    // --------------------------------------------------------------------------------------------


    // DELETE
    // --------------------------------------------------------------------------------------------
    void deleteAllWorkouts() {
        new RepoAsyncTask(workoutDao, null, null).execute(DELETE_ALL_WORKOUTS);
    }

    void deleteWorkout(Workout workout) {
        new RepoAsyncTask(workoutDao, null, null).execute(DELETE_WORKOUT);
    }

    void deleteExercise(Exercise exercise) {
        new RepoAsyncTask(exerciseDao, null).execute(DELETE_EXERCISE);
    }

    void deleteSet(Set set) {
        new RepoAsyncTask(setDao, set).execute(DELETE_SET);
    }
    // --------------------------------------------------------------------------------------------


    private static class RepoAsyncTask extends AsyncTask<RepoTask, Void, Void> {

        private WorkoutDao workoutDao;
        private ExerciseDao exerciseDao;
        private SetDao setDao;

        private Workout workout;
        private Exercise exercise;
        private Set set;

        private int id = -1;

        RepoAsyncTask(WorkoutDao workoutDao, @Nullable ExerciseDao exerciseDao, @Nullable Workout workout) {
            this.workoutDao = workoutDao;
            this.exerciseDao = exerciseDao;
            this.workout = workout;
        }

        RepoAsyncTask(WorkoutDao workoutDao, int workoutId) {
            this.workoutDao = workoutDao;
            this.id = workoutId;
        }

        RepoAsyncTask(ExerciseDao exerciseDao, @Nullable Exercise exercise) {
            this.exerciseDao = exerciseDao;
            this.exercise = exercise;
        }

        RepoAsyncTask(SetDao setDao, Set set) {
            this.setDao = setDao;
            this.set = set;
        }

        @Override
        protected Void doInBackground(RepoTask... tasks) {

            for (RepoTask task : tasks) {
                switch (task) {
                    case INSERT_WORKOUT:
                        workoutDao.insert(workout);

                        break;
                    case INSERT_EXERCISE:
                        exerciseDao.insert(exercise);
                        break;
                    case INSERT_SET:
                        setDao.insert(set);
                        break;


                    case UPDATE_WORKOUT:
                        workoutDao.update(workout);
                        break;
                    case UPDATE_EXERCISE:
                        exerciseDao.update(exercise);
                        break;
                    case UPDATE_SET:
                        setDao.update(set);
                        break;



                    case DELETE_WORKOUT:
                        workoutDao.delete(workout);
                        break;
                    case DELETE_EXERCISE:
                        exerciseDao.delete(exercise);
                        break;
                    case DELETE_SET:
                        setDao.delete(set);
                        break;
                }
            }

            return null;
        }

    }

}
