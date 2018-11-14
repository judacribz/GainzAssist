package ca.judacribz.gainzassist.models.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import ca.judacribz.gainzassist.interfaces.OnWorkoutReceivedListener;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.Session;
import ca.judacribz.gainzassist.models.Set;
import ca.judacribz.gainzassist.models.Workout;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ca.judacribz.gainzassist.models.db.WorkoutRepo.RepoTask.*;
import static ca.judacribz.gainzassist.models.db.WorkoutRepo.TableTxn.*;
import static ca.judacribz.gainzassist.util.Helper.extractWorkout;
import static ca.judacribz.gainzassist.util.firebase.Database.addWorkoutSessionFirebase;

public class WorkoutRepo {
    static private WorkoutDao workoutDao;
    static private ExerciseDao exerciseDao;
    static private SetDao setDao;
    static private SessionDao sessionDao;
    private DataSnapshot workoutShot;

    public enum TableTxn {
        WORKOUTS_TXN,
        EXERCISES_TXN,
        SESSIONS_TXN,
        SETS_TXN
    }

    public enum RepoTask {
        GET_WORKOUT_ID,
        GET_WORKOUT,
        GET_EXERCISES,

        INSERT_EXERCISE,
        INSERT_WORKOUT,
        INSERT_SESSION,
        INSERT_SET,

        UPDATE_WORKOUT,
        UPDATE_EXERCISE,
        UPDATE_EXERCISE_WEIGHT,
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
        sessionDao = db.sessionDao();
    }


    // CRUD functions
    // ============================================================================================
    // CREATE
    // --------------------------------------------------------------------------------------------
    public void insertWorkout(Workout workout) {
        setRepoAsyncConfig(INSERT_WORKOUT, WORKOUTS_TXN, null, workout);
    }

    void insertExercise(Exercise exercise) {
        setRepoAsyncConfig(INSERT_EXERCISE, EXERCISES_TXN, null, exercise);
    }

    void insertSession(Session session) {
        addWorkoutSessionFirebase(session);

        setRepoAsyncConfig(INSERT_SESSION, SESSIONS_TXN, null, session);
    }

    static void insertSet(Set set) {
        setRepoAsyncConfig(INSERT_SET, SETS_TXN, null, set);
    }
    // --------------------------------------------------------------------------------------------


    // RETRIEVE
    // --------------------------------------------------------------------------------------------
    LiveData<List<Workout>> getAllWorkoutsLive() {
        return workoutDao.getAll();
    }

    LiveData<Workout> getWorkout(int id) {
        return workoutDao.get(id);
    }

    public void getWorkoutFromName(Context context, String name) {
        setRepoAsyncConfig(GET_WORKOUT, WORKOUTS_TXN, context, name);
    }

    public void getWorkoutId(Context context, DataSnapshot workoutShot) {
        setRepoAsyncConfig(GET_WORKOUT_ID, WORKOUTS_TXN, context, workoutShot.getKey(), workoutShot);
    }

    LiveData<List<Exercise>> getExercisesFromWorkout(int workoutId) {
        return exerciseDao.getLiveFromWorkout(workoutId);
    }

    LiveData<Exercise> getExercise(int id) {
        return exerciseDao.get(id);
    }

    LiveData<List<String>> getAllUniqueExerciseNames() {
        return exerciseDao.getAllUniqueNames();
    }

    LiveData<List<Set>> getSetsFromExercise(int exerciseId) {
        return setDao.getLiveFromExercise(exerciseId);
    }
    // --------------------------------------------------------------------------------------------


    // UPDATE
    // --------------------------------------------------------------------------------------------
    public void updateWorkout(Workout workout) {
        setRepoAsyncConfig(UPDATE_WORKOUT, WORKOUTS_TXN, null, workout);
    }

    void updateExercise(Exercise exercise) {
        setRepoAsyncConfig(UPDATE_EXERCISE, EXERCISES_TXN, null, exercise);
    }

    void updateExerciseWeight(SparseArray<Float> newWeights) {
        setRepoAsyncConfig(UPDATE_EXERCISE_WEIGHT, EXERCISES_TXN, null, newWeights);
    }

    void updateSet(Set set) {
        setRepoAsyncConfig(UPDATE_SET, SETS_TXN, null, set);
    }
    // --------------------------------------------------------------------------------------------


    // DELETE
    // --------------------------------------------------------------------------------------------
    void deleteAllWorkouts() {
        setRepoAsyncConfig(DELETE_ALL_WORKOUTS, WORKOUTS_TXN, null, (Object) null);
    }

    public void deleteWorkout(String workoutName) {
        setRepoAsyncConfig(DELETE_WORKOUT, WORKOUTS_TXN, null, workoutName);
    }

    void deleteExercise(Exercise exercise) {
        setRepoAsyncConfig(DELETE_EXERCISE, EXERCISES_TXN, null, exercise);
    }

    void deleteSet(Set set) {
        setRepoAsyncConfig(DELETE_SET, SETS_TXN, null, set);
    }
    // --------------------------------------------------------------------------------------------
    // ============================================================================================


    private static void setRepoAsyncConfig(RepoTask repoTask,
                                           TableTxn tableTxn,
                                           @Nullable Context context,
                                           Object... obj) {

        RepoAsyncTask repoAsyncTask = new RepoAsyncTask();
        switch(tableTxn) {
            case WORKOUTS_TXN:
                switch (repoTask) {
                    case INSERT_WORKOUT:
                    case UPDATE_WORKOUT:
                        repoAsyncTask.setWorkout((Workout) obj[0]);
                        break;

                    case GET_WORKOUT_ID:
                        repoAsyncTask.setWorkoutShot((DataSnapshot) obj[1]);
                    case GET_WORKOUT:
                    case DELETE_WORKOUT:
                        if (context != null) {
                            repoAsyncTask.setOnWorkoutReceivedListener(
                                    (OnWorkoutReceivedListener) context
                            );
                        }
                        repoAsyncTask.setWorkoutName((String) obj[0]);
                        break;
                }
            break;

            case EXERCISES_TXN:

                switch (repoTask) {
                    case UPDATE_EXERCISE_WEIGHT:
//                        repoAsyncTask.setExerciseWeights((SparseArray<Float>) obj[0]);
                        break;
                    default:
                        repoAsyncTask.setExercise((Exercise) obj[0]);
                        break;
                }
                break;
            case SESSIONS_TXN:
                repoAsyncTask.setSession((Session) obj[0]);
                break;


            case SETS_TXN:
                repoAsyncTask.setSet((Set) obj[0]);
                break;
        }

        repoAsyncTask.execute(repoTask);
    }



    public static class RepoAsyncTask extends AsyncTask<RepoTask, Void, Void> {

        OnWorkoutReceivedListener onWorkoutReceivedListener = null;


        private DataSnapshot workoutShot = null;
        private String workoutName = null;
        private Workout workout = null;
        private Exercise exercise = null;
        private Map<String, Float> newWeights = null;
        private Set set = null;
        private Session session = null;
        private long timestamp = -1;
        private int workoutId = -1;

        private int id = -1;

        RepoAsyncTask() {
        }

        void setWorkout(Workout workout) {
            this.workout = workout;
            setWorkoutName(workout.getName());
        }

        void setWorkoutName(String workoutName) {
            this.workoutName = workoutName;
        }
        void setWorkoutShot(DataSnapshot workoutShot) {
            this.workoutShot = workoutShot;
        }

        void setExercise(Exercise exercise) {
            this.exercise = exercise;
        }

        void setSession(Session session) {
            this.session = session;
            setTimestamp(session.getTimestamp());
            setWorkoutId(session.getWorkoutId());
            setExerciseWeights(session.getAvgWeights());
        }

        public void setExerciseWeights(Map<String, Float> newWeights) {
            this.newWeights = newWeights;
        }

        void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        void setWorkoutId(int workoutId) {
            this.workoutId = workoutId;
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
                    case GET_WORKOUT_ID:
                        if (workoutDao.getId(workoutName) == 0) {
                            onWorkoutReceivedListener.onWorkoutsReceived(extractWorkout(workoutShot));
                        }

                        setOnWorkoutReceivedListener(null);
                        break;


                    case GET_WORKOUT:
                        workout = workoutDao.getFromName(workoutName);

                        for (Exercise exercise : exerciseDao.getFromWorkout(workout.getId())) {
                            exercise.setSetsList(null);
                            workout.addExercise(exercise);
                        }

                        onWorkoutReceivedListener.onWorkoutsReceived(workout);
                        setOnWorkoutReceivedListener(null);
                        break;


                    case INSERT_WORKOUT:
                        workoutDao.insert(workout);
                        Log.d("YOOOO", "Workout:" + workout.getName() + " id : " + workoutDao.getId(workoutName));
                        id = workoutDao.getId(workoutName);
                        for (Exercise exercise : workout.getExercises()) {
                            exercise.setWorkoutId(id);
                            setRepoAsyncConfig(INSERT_EXERCISE, EXERCISES_TXN, null, exercise);
                        }
                        break;

                    case INSERT_EXERCISE:
                        exerciseDao.insert(exercise);

                        break;

                    case INSERT_SESSION:
                        sessionDao.insert(session);
                        id = sessionDao.getId(timestamp);

                        for (Map.Entry<String, ArrayList<Set>> exSets : session.getSessionSets().entrySet()){
                            for (Set set : exSets.getValue()) {
                                set.setSessionId(id);

                                insertSet(set);
                            }
                        }

                        for (Map.Entry<String, Float> entry : newWeights.entrySet()) {
                            exerciseDao.updateWeight(entry.getValue(), exerciseDao.getId(entry.getKey(), workoutId));
                        }

                        break;

                    case INSERT_SET:
                        if (setDao != null)
                            setDao.insert(set);
                        break;


                    case UPDATE_WORKOUT:
                        id = workoutDao.getId(workoutName);
                        workout.setId(id);
                        workoutDao.update(workout);
                        for (Exercise exercise : workout.getExercises()) {
                            exercise.setWorkoutId(id);
                            setRepoAsyncConfig(UPDATE_EXERCISE, EXERCISES_TXN, null, exercise);
                        }
                        break;

                    case UPDATE_EXERCISE:
                        exercise.setId(exerciseDao.getId(exercise.getName(), exercise.getWorkoutId()));
                        exerciseDao.update(exercise);
                        break;

                    case UPDATE_SET:
                        setDao.update(set);
                        break;


                    case DELETE_ALL_WORKOUTS:
                        workoutDao.deleteAll();
                        break;

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
