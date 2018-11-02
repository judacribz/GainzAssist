package ca.judacribz.gainzassist.models.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.Set;
import ca.judacribz.gainzassist.models.Workout;
import com.google.android.gms.common.collect.Sets;

import java.util.List;

import static ca.judacribz.gainzassist.models.db.WorkoutRepo.RepoTask.*;
import static ca.judacribz.gainzassist.models.db.WorkoutRepo.TableTxn.*;

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
        setRepoAsyncConfig(INSERT_WORKOUT, WORKOUTS_TXN, workout, null);
    }

    void insertExercise(Exercise exercise) {
        setRepoAsyncConfig(INSERT_EXERCISE, EXERCISES_TXN, exercise, null);
    }

    void insertSet(Set set) {
        setRepoAsyncConfig(INSERT_SET, SETS_TXN, set, null);
    }
    // --------------------------------------------------------------------------------------------


    // RETRIEVE
    // --------------------------------------------------------------------------------------------
    LiveData<List<Workout>> getAllWorkoutsLive() {
        return workoutDao.getAll();
    }

    LiveData<Workout> getWorkout(long id) {
        return workoutDao.get(id);
    }

    void getWorkoutFromName(Context context, String name) {
        setRepoAsyncConfig(GET_WORKOUT, WORKOUTS_TXN, name, context);
    }

    LiveData<List<Exercise>> getExercisesFromWorkout(long workoutId) {
        return exerciseDao.getLiveFromWorkout(workoutId);
    }

    LiveData<Exercise> getExercise(long id) {
        return exerciseDao.get(id);
    }

    LiveData<List<String>> getAllUniqueExerciseNames() {
        return exerciseDao.getAllUniqueNames();
    }

    LiveData<List<Set>> getSetsFromExercise(long exerciseId) {
        return setDao.getLiveFromExercise(exerciseId);
    }
    // --------------------------------------------------------------------------------------------


    // UPDATE
    // --------------------------------------------------------------------------------------------
    void updateWorkout(Workout workout) {
        setRepoAsyncConfig(UPDATE_WORKOUT, WORKOUTS_TXN, workout, null);
    }

    void updateExercise(Exercise exercise) {
        setRepoAsyncConfig(UPDATE_EXERCISE, EXERCISES_TXN, exercise, null);
    }

    void updateSet(Set set) {
        setRepoAsyncConfig(UPDATE_SET, SETS_TXN, set, null);
    }
    // --------------------------------------------------------------------------------------------


    // DELETE
    // --------------------------------------------------------------------------------------------
    void deleteAllWorkouts() {
        setRepoAsyncConfig(DELETE_ALL_WORKOUTS, WORKOUTS_TXN, null, null);
    }

    public void deleteWorkout(String workoutName) {
        setRepoAsyncConfig(DELETE_WORKOUT, WORKOUTS_TXN, workoutName, null);
    }

    void deleteExercise(Exercise exercise) {
        setRepoAsyncConfig(DELETE_EXERCISE, EXERCISES_TXN, exercise, null);
    }

    void deleteSet(Set set) {
        setRepoAsyncConfig(DELETE_SET, SETS_TXN, set, null);
    }
    // --------------------------------------------------------------------------------------------


    public enum TableTxn {
        WORKOUTS_TXN,
        EXERCISES_TXN,
        SETS_TXN
    }

    private static void setRepoAsyncConfig(RepoTask repoTask,
                                           TableTxn tableTxn,
                                           Object obj,
                                           @Nullable Context context) {

        RepoAsyncTask repoAsyncTask = new RepoAsyncTask();
        switch(tableTxn) {
            case WORKOUTS_TXN:
                switch (repoTask) {
                    case INSERT_WORKOUT:
                        repoAsyncTask.setWorkout((Workout) obj);
                        break;

                    case GET_WORKOUT:
                    case DELETE_WORKOUT:
                        if (context != null) {
                            repoAsyncTask.setOnWorkoutReceivedListener(
                                    (RepoAsyncTask.OnWorkoutReceivedListener) context
                            );
                        }
                        repoAsyncTask.setWorkoutName((String) obj);
                        break;
                }
            break;

            case EXERCISES_TXN:
                repoAsyncTask.setExercise((Exercise) obj);
                break;

            case SETS_TXN:
                repoAsyncTask.setSet((Set) obj);
                break;
        }

        repoAsyncTask.execute(repoTask);
    }



    public static class RepoAsyncTask extends AsyncTask<RepoTask, Void, Void> {

        OnWorkoutReceivedListener onWorkoutReceivedListener = null;

        public interface OnWorkoutReceivedListener {
            void onWorkoutsReceived(Workout workout);
        }

        private String workoutName = null;
        private Workout workout = null;
        private Exercise exercise = null;
        private Set set = null;

        private long id = -1;

        RepoAsyncTask() {
        }

        void setWorkout(Workout workout) {
            this.workout = workout;
        }

        void setWorkoutName(String workoutName) {
            this.workoutName = workoutName;
        }

        void setExercise(Exercise exercise) {
            this.exercise = exercise;
        }

        void setSet(Set set) {
            this.set = set;
        }

        void setOnWorkoutReceivedListener(OnWorkoutReceivedListener onWorkoutReceivedListener) {
            this.onWorkoutReceivedListener = onWorkoutReceivedListener;
        }

        @Override
        protected Void doInBackground(RepoTask... tasks) {

            for (RepoTask task : tasks) {
                switch (task) {
                    case GET_WORKOUT:
                        workout = workoutDao.getFromName(workoutName);
                        for (Exercise exercise : exerciseDao.getFromWorkout(workout.getId())) {
                            for (Set set :setDao.getFromExercise(exercise.getId())) {
                                exercise.addSet(set);
                            }
                            workout.addExercise(exercise);
                        }
                        onWorkoutReceivedListener.onWorkoutsReceived(workout);
                        break;


                    case INSERT_WORKOUT:
                        long wid = workoutDao.insert(workout);
                        for (Exercise exercise : workout.getExercises()) {
                            exercise.setWorkoutId(wid);
                            setRepoAsyncConfig(INSERT_EXERCISE, EXERCISES_TXN, exercise, null);
                        }
                        break;
                    case INSERT_EXERCISE:
                        long eid = exerciseDao.insert(exercise);
                        for (Set set : exercise.getSets()) {
                            set.setExerciseId(eid);
                            setRepoAsyncConfig(INSERT_SET, SETS_TXN, set, null);
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

                    case DELETE_ALL_WORKOUTS:
                        workoutDao.deleteAll();

                    case DELETE_WORKOUT:
                        workoutDao.delete(workoutName);
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
