package ca.judacribz.gainzassist.models.db;

import static ca.judacribz.gainzassist.util.firebase.Database.addWorkoutSessionFirebase;
import static ca.judacribz.gainzassist.util.firebase.Database.deleteWorkoutFirebase;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.LiveData;

import com.google.firebase.database.DataSnapshot;

import java.util.List;

import ca.judacribz.gainzassist.interfaces.OnWorkoutReceivedListener;
import ca.judacribz.gainzassist.models.Exercise;
import ca.judacribz.gainzassist.models.ExerciseSet;
import ca.judacribz.gainzassist.models.Session;
import ca.judacribz.gainzassist.models.Workout;

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
        new Thread(() -> {
            workoutDao.insert(workout);
            insertExercise(workout.exercises.toArray(new Exercise[0]));
        }).start();
    }

    void insertExercise(Exercise...exercises) {
        for (Exercise ex : exercises) {
            exerciseDao.insert(ex);
        }
    }

    public void insertSession(Session session, boolean toFireBase) {
        new Thread(() -> {
            sessionDao.insert(session);
            for (Exercise ex : session.getSessionExs()) {
                insertSet(ex.getFinishedSetsList().toArray(new ExerciseSet[0]));
            }
        }).start();

        if (toFireBase) {
            addWorkoutSessionFirebase(session);
        }
    }

    static void insertSet(ExerciseSet...exerciseSets) {
        for (ExerciseSet set : exerciseSets) {
            new Thread(() -> setDao.insert(set)).start();
        }
    }
    // --------------------------------------------------------------------------------------------


    // RETRIEVE
    // --------------------------------------------------------------------------------------------
    LiveData<List<Workout>> getAllWorkoutsLive() {
        return workoutDao.getAll();
    }
    LiveData<List<Session>> getAllSessionsLive() {
        return sessionDao.getAll();
    }

    LiveData<List<ExerciseSet>> getAllSetsLive() {
        return setDao.getAll();
    }

    LiveData<Workout> getWorkout(long id) {
        return workoutDao.get(id);
    }
    OnWorkoutReceivedListener onWorkoutReceivedListener;
    private void setOnWorkoutReceivedListener(OnWorkoutReceivedListener onWorkoutReceivedListener) {
        this.onWorkoutReceivedListener = onWorkoutReceivedListener;
    }
    void getWorkoutFromName(Context context, String name) {
        new Thread(() -> {
            setOnWorkoutReceivedListener((OnWorkoutReceivedListener) context);
            Workout workout = workoutDao.getFromName(name);

            for (Exercise exercise : exerciseDao.getFromWorkout(workout.getId())) {
                exercise.setSetsList(null);
                workout.addExercise(exercise);
            }

            onWorkoutReceivedListener.onWorkoutsReceived(workout);
            setOnWorkoutReceivedListener(null);
        }).start();

    }

    LiveData<List<Exercise>> getExercisesFromWorkout(long workoutId) {
        return exerciseDao.getLiveFromWorkout(workoutId);
    }

    LiveData<Exercise> getExercise(long id) {
        return exerciseDao.getLive(id);
    }

    LiveData<List<String>> getAllUniqueExerciseNames() {
        return exerciseDao.getAllUniqueNames();
    }

    LiveData<List<ExerciseSet>> getSetsFromExercise(long exerciseId) {
        return setDao.getLiveFromExercise(exerciseId);
    }
    // --------------------------------------------------------------------------------------------


    // UPDATE
    // --------------------------------------------------------------------------------------------
    public void updateWorkout(Workout workout) {
        new Thread(() -> workoutDao.update(workout)).start();
        updateExercise(workout.exercises.toArray(new Exercise[0]));
    }

    void updateExercise(Exercise...exercises) {
        for (Exercise ex : exercises) {
            new Thread(() -> {
                if (exerciseDao.get(ex.getId()) == null) {
                    insertExercise(ex);
                } else {
                    exerciseDao.update(ex);
                }
            }).start();
        }
    }
    // --------------------------------------------------------------------------------------------


    // DELETE
    // --------------------------------------------------------------------------------------------
    void deleteAllWorkouts() {
        new Thread(() -> workoutDao.deleteAll()).start();
    }

    public void deleteWorkout(Workout workout) {
        new Thread(() -> workoutDao.delete(workout)).start();
        deleteWorkoutFirebase(workout.name);
    }

    void deleteWorkout(String workoutName) {
        new Thread(() -> workoutDao.delete(workoutName)).start();
        deleteWorkoutFirebase(workoutName);
    }

    void deleteExercise(Exercise...exercise) {
        new Thread(() -> exerciseDao.delete(exercise)).start();
    }

    void deleteSet(ExerciseSet exerciseSet) {
        new Thread(() -> setDao.delete(exerciseSet)).start();
    }
    // --------------------------------------------------------------------------------------------
    // ============================================================================================
}
