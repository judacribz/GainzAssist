package ca.judacribz.gainzassist.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;

public class WorkoutHelper extends SQLiteOpenHelper {

    // Constants
    // ============================================================================================
    private static final int    DATABASE_VERSION = 1;
    private static final String TABLE_WORKOUTS   = "workouts";
    private static final String TABLE_SESSIONS   = "sessions";

    // Column names
    private static final String WORKOUT_NAME  = "workoutName";
    private static final String EXERCISES     = "exercises";

    private static final String TIMESTAMP     = "timestamp";
    private static final String EXERCISE_NAME = "exerciseName";
    private static final String SETS          = "setsList";


    private static final String CREATE_STATEMENT = "CREATE TABLE %s";
    // Workouts table create statement
    private static final String CREATE_STATEMENT_WORKOUTS = String.format(
            CREATE_STATEMENT,
            TABLE_WORKOUTS + " (" +
                    WORKOUT_NAME  + " text not null," +
                    EXERCISES     + " blob not null," +
                    "PRIMARY KEY (" + WORKOUT_NAME + ")" +
            ")"
    );

    // Sessions table create statement
    private static final String CREATE_STATEMENT_SESSIONS = String.format(
            CREATE_STATEMENT,
            TABLE_SESSIONS + " (" +
                    TIMESTAMP     + " integer not null," +
                    WORKOUT_NAME  + " text not null," +
                    EXERCISE_NAME + " text not null," +
                    SETS          + " blob not null," +
                    "PRIMARY KEY (" + TIMESTAMP + ")" +
            ")"
    );

    private static final String DROP_STATEMENT = "DROP TABLE %s";
    private static final String DROP_STATEMENT_WORKOUTS = String.format(DROP_STATEMENT, TABLE_WORKOUTS);
    private static final String DROP_STATEMENT_SESSIONS =  String.format(DROP_STATEMENT, TABLE_SESSIONS);
    // ============================================================================================

    // Global Vars
    // ============================================================================================
    private SQLiteDatabase db;
    private ContentValues cv;
    private Context context;
    private Gson gson;
    // ============================================================================================

    // ######################################################################################### //
    // WorkoutHelper Constructor                                                                 //
    // ######################################################################################### //
    public WorkoutHelper(Context context) {
        super(context, "workouts", null, DATABASE_VERSION);
        this.context = context;

        cv = new ContentValues();
        gson = new Gson();
    }
    // ######################################################################################### //


    // SQLiteOpenHelper Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    // TODO: needs to save data from old db before drop and write to new db when changing schema
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersionNum, int newVersionNum) {

        // delete the old database
        db.execSQL(DROP_STATEMENT_WORKOUTS);
        db.execSQL(DROP_STATEMENT_SESSIONS);

        // re-create the database
        createTables(db);
    }

    private void createTables(SQLiteDatabase db) {
        // create the database, using CREATE SQL statement
        db.execSQL(CREATE_STATEMENT_WORKOUTS);
        db.execSQL(CREATE_STATEMENT_SESSIONS);
    }
    //SQLiteOpenHelper//Override///////////////////////////////////////////////////////////////////


    /* Convert Exercise ArrayList to Blob format */
    private byte[] getBlobFromExercises(ArrayList<Exercise> exercises) {
        return gson.toJson(exercises).getBytes();
    }

    /* Convert Blob of Exercises to Exercise ArrayList format */
    private ArrayList<Exercise> getExercisesFromBlob(byte[] blob) {
        return gson.fromJson(new String(blob), new TypeToken<ArrayList<Exercise>>() {}.getType());
    }

    /* Convert Exercise ArrayList to Blob format */
    private byte[] getBlobFromSets(ArrayList<ExerciseSet> exerciseSets) {
        return gson.toJson(exerciseSets).getBytes();
    }

    /* Convert Blob of Exercises to Exercise ArrayList format */
    private ArrayList<ExerciseSet> getSetsFromBlob(byte[] blob) {
        return gson.fromJson(new String(blob), new TypeToken<ArrayList<ExerciseSet>>() {}.getType());
    }

    /* Checks to see if the database exists */
    public boolean exists() {
        return (new File(context.getDatabasePath(TABLE_WORKOUTS).toString())).exists();
    }


    // CRUD functions
    // ============================================================================================
    // CREATE
    // --------------------------------------------------------------------------------------------
    /* Creates a db entry for each workout in the provided ArrayList
     */
//    public void addWorkouts(ArrayList<ExerciseConst> workouts) {
//        db = this.getWritableDatabase();
//        cv.clear();
//        cv.put(EMAIL, email);
//
//        for (ExerciseConst workout : workouts) {
//            addWorkout(workout, true);
//        }
//    }

    /* Creates a db entry for the workout
     */
    public void addWorkout(Workout workout) {
        db = this.getWritableDatabase();
        cv.clear();
        cv.put(WORKOUT_NAME,  workout.getName());
        cv.put(EXERCISES,     getBlobFromExercises(workout.getExercises()));

//        long i =  db.insert(TABLE_WORKOUTS, null, cv);

//        Toast.makeText(context, "adding" + i, Toast.LENGTH_SHORT).show();
    }

    /* Creates a db entry for the session
     */
//    public void addSession(Session session) {
//        db = this.getWritableDatabase();
//        cv.clear();
//
//        ArrayList<String> exerciseNames = session.getExerciseNames();
//        ArrayList<ArrayList<ExerciseSet>> sets = session.getAllSets();
//
//        cv.put(TIMESTAMP, session.getTimestamp());
//        cv.put(WORKOUT_NAME, session.getWorkoutName());
//
//        for (int i = 0; i < exerciseNames.size(); i++) {
//            cv.put(EXERCISE_NAME, exerciseNames.get(i));
//            cv.put(SETS, getBlobFromSets(sets.get(i)));
//
//            db.insert(TABLE_SESSIONS, null, cv);
//            cv.clear();
//        }
//    }
    // --------------------------------------------------------------------------------------------

    // RETRIEVE
    // --------------------------------------------------------------------------------------------
    /* Check if a workout exists in the database
     */
    public boolean workoutExists(String workoutName) {
        db = this.getReadableDatabase();
        Workout workout = null;

        String[] columns   = new String[] {EXERCISES};
        String where       = WORKOUT_NAME + " = ?";
        String[] whereArgs = new String[] {workoutName};

        Cursor cursor = db.query(TABLE_WORKOUTS, columns, where, whereArgs, "", "", "");
        cursor.close();

        return (cursor.getCount() >= 1);
    }


    /* Gets all exercise names in the db and returns a list of Strings
     */
    ArrayList<String> getAllExerciseNames(String workoutName) {
        ArrayList<String> exerciseNames = new ArrayList<>();
        for (Exercise exercise : getWorkout(workoutName).getExercises()) {
            exerciseNames.add(exercise.getName());
        }

        return exerciseNames;
    }

    /* Get a ExerciseConst object using the workout name
     */
    public Workout getWorkout(String workoutName) {
        ArrayList<Exercise> exercises;
        Workout workout = null;

        db = this.getReadableDatabase();

        String[] columns   = new String[] {EXERCISES};
        String   where     = WORKOUT_NAME + " = ?";
        String[] whereArgs = new String[] {workoutName};

        Cursor cursor = db.query(TABLE_WORKOUTS, columns, where, whereArgs, "", "", "");

        if (cursor.moveToFirst()) {

            exercises = getExercisesFromBlob(cursor.getBlob(0));

            if (exercises != null)
                workout = new Workout(workoutName, exercises);
        }
        cursor.close();

        return workout;
    }

    /* Gets all workouts in the db and returns a list of ExerciseConst objects
     */
    public ArrayList<Workout> getAllWorkouts() {
        db = this.getReadableDatabase();
        ArrayList<Workout> workouts = new ArrayList<>();
        ArrayList<Exercise> exercises;

        String[] column      = new String[] {WORKOUT_NAME, EXERCISES};

        Cursor cursor = db.query(TABLE_WORKOUTS, column, null, null, WORKOUT_NAME, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                exercises = (ArrayList<Exercise>) getExercisesFromBlob(cursor.getBlob(1));

                if (exercises != null)
                    workouts.add(new Workout(cursor.getString(0), exercises));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return workouts;
    }

    /* Gets all workout names in the db and returns a list of Strings
     */
    public ArrayList<String> getAllWorkoutNames() {
        db = this.getReadableDatabase();
        ArrayList<String> workoutNames = new ArrayList<>();

        // Get workout names
        String[] column      = new String[] {WORKOUT_NAME};
        Cursor cursor = db.query(TABLE_WORKOUTS, column, null, null, WORKOUT_NAME, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                workoutNames.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return workoutNames;
    }


    // UPDATE
    // --------------------------------------------------------------------------------------------
    /* Updates the weight for an exercise in a workout
     */
//    public boolean updateWeight(String workoutName, String exerciseName, float newWeight) {
//        db = this.getWritableDatabase();
//
//
//        cv.clear();
//        cv.put(WEIGHT, newWeight);
//
//        String where = EMAIL + " = ? AND " + WORKOUT_NAME + " = ? AND " + EXERCISE_NAME + " = ?";
//        String[] whereArgs = new String[] {email, workoutName, exerciseName};
//
//        int numRows = db.update(TABLE_WORKOUTS, cv, where, whereArgs);
//
//        return (numRows == 1);
//    }

        public boolean updateWorkout(String workoutName, Workout workout) {
        db = this.getWritableDatabase();

        cv.clear();
        cv.put(EXERCISES, getBlobFromExercises(workout.getExercises()));

        String where = WORKOUT_NAME + " = ?";
        String[] whereArgs = new String[] {workoutName};

        int numRows = db.update(TABLE_WORKOUTS, cv, where, whereArgs);

        return (numRows == 1);
    }

    // DELETE
    // --------------------------------------------------------------------------------------------
    /* Deletes a workout
     */
    public boolean deleteWorkout(String workoutName) {
        db = this.getWritableDatabase();

        String where       = WORKOUT_NAME + " = ?";
        String[] whereArgs = new String[] {workoutName};
        int numRows        = db.delete(TABLE_WORKOUTS, where, whereArgs);

        return (numRows > 0);
    }

    /* Deletes all workouts
     */
    public void deleteAllWorkouts() {
        db = this.getWritableDatabase();

        db.delete(TABLE_WORKOUTS, null, null);
    }
    // --------------------------------------------------------------------------------------------
    // ============================================================================================
}
