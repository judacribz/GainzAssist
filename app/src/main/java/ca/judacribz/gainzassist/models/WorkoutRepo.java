package ca.judacribz.gainzassist.models;

import android.app.Application;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static ca.judacribz.gainzassist.models.WorkoutRepo.RepoTask.*;

public class WorkoutRepo {
    static private WorkoutDao workoutDao;
    static private ExerciseDao exerciseDao;
    static private SetDao setDao;
    private LiveData<List<Workout>> workouts;

    public enum RepoTask {
        GET_WORKOUT,
        GET_EXERCISES,

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
        new RepoAsyncTask(workout).execute(INSERT_WORKOUT);
    }

    void insertExercise(Exercise exercise) {
        new RepoAsyncTask(exercise).execute(INSERT_EXERCISE);
    }

    void insertSet(Set set) {
        new RepoAsyncTask(set).execute(INSERT_SET);
    }
    // --------------------------------------------------------------------------------------------


    // RETRIEVE
    // --------------------------------------------------------------------------------------------
    LiveData<List<Workout>> getAllWorkouts() {
        return workoutDao.getAll();
    }

    LiveData<Workout> getWorkout(long id) {
        return workoutDao.get(id);
    }


    LiveData<Workout> getWorkoutFromName(final String name) {
        return workoutDao.getFromName(name);
    }

    LiveData<List<Exercise>> getExercisesFromWorkout(long workoutId) {
        return exerciseDao.getFromWorkout(workoutId);
    }

    LiveData<Exercise> getExercise(long id) {
        return exerciseDao.get(id);
    }

    LiveData<List<String>> getAllUniqueExerciseNames() {
        return exerciseDao.getAllUniqueNames();
    }

    LiveData<List<Set>> getSetsFromExercise(long exerciseId) {
        return setDao.getSetsFromExercise(exerciseId);
    }
    // --------------------------------------------------------------------------------------------


    // UPDATE
    // --------------------------------------------------------------------------------------------
    void updateWorkout(Workout workout) {
        new RepoAsyncTask(workout).execute(UPDATE_WORKOUT);
    }

    void updateExercise(Exercise exercise) {
        new RepoAsyncTask(exercise).execute(UPDATE_EXERCISE);
    }

    void updateSet(Set set) {
        new RepoAsyncTask(set).execute(UPDATE_SET);
    }
    // --------------------------------------------------------------------------------------------


    // DELETE
    // --------------------------------------------------------------------------------------------
    void deleteAllWorkouts() {
        new RepoAsyncTask().execute(DELETE_ALL_WORKOUTS);
    }

    void deleteWorkout(Workout workout) {
        new RepoAsyncTask().execute(DELETE_WORKOUT);
    }

    void deleteExercise(Exercise exercise) {
        new RepoAsyncTask().execute(DELETE_EXERCISE);
    }

    void deleteSet(Set set) {
        new RepoAsyncTask(set).execute(DELETE_SET);
    }
    // --------------------------------------------------------------------------------------------


    private static class RepoAsyncTask extends AsyncTask<RepoTask, Void, Void> {


        private Workout workout;
        private Exercise exercise;
        private Set set;

        private long id = -1;

        RepoAsyncTask() {
        }

        RepoAsyncTask(Workout workout) {
            this.workout = workout;
        }

        RepoAsyncTask(Exercise exercise) {
            this.exercise = exercise;
        }

        RepoAsyncTask(Set set) {
            this.set = set;
        }

        @Override
        protected Void doInBackground(RepoTask... tasks) {

            for (RepoTask task : tasks) {
                switch (task) {
                    case INSERT_WORKOUT:
                        long wid = workoutDao.insert(workout);
                        for (Exercise exercise : workout.getExercises()) {
                            exercise.setWorkoutId(wid);
                            new RepoAsyncTask(exercise).execute(INSERT_EXERCISE);
                        }
                        break;
                    case INSERT_EXERCISE:
                        long eid = exerciseDao.insert(exercise);
                        for (Set set : exercise.getSets()) {
                            set.setExerciseId(eid);
                            new RepoAsyncTask(set).execute(INSERT_SET);
                        }
                        break;
                    case INSERT_SET:
                        if (setDao != null)
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
