package ca.judacribz.gainzassist.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;

import static ca.judacribz.gainzassist.activities.start_workout.StartWorkout.workout;

public class WorkoutHelper extends SQLiteOpenHelper {

    // Constants
    // ============================================================================================
    private static final int    DATABASE_VERSION = 1;
    private static final String TABLE_WORKOUTS   = "workouts";
    private static final String TABLE_SESSIONS   = "sessions";

    // Column names
    private static final String WORKOUT_NAME  = "workoutName";
    private static final String EXERCISES     = "exercises";
    private static final String EMAIL         = "email";

    private static final String TIMESTAMP     = "timestamp";
    private static final String EXERCISE_NAME = "exerciseName";
    private static final String SETS          = "sets";


    private static final String CREATE_STATEMENT = "CREATE TABLE %s";
    // Workouts table create statement
    private static final String CREATE_STATEMENT_WORKOUTS = String.format(
            CREATE_STATEMENT,
            TABLE_WORKOUTS + " (" +
                    WORKOUT_NAME  + " text not null," +
                    EXERCISES     + " blob not null," +
                    EMAIL         + " varchar2(100) not null," +
                    "PRIMARY KEY (" + WORKOUT_NAME + ", " + EMAIL + ")" +
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
                    EMAIL         + " varchar2(100) not null," +
                    "PRIMARY KEY (" + TIMESTAMP + ", " + EMAIL + ")" +
            ")"
    );

    private static final String DROP_STATEMENT = "DROP TABLE %s";
    private static final String DROP_STATEMENT_WORKOUTS = String.format(DROP_STATEMENT, TABLE_WORKOUTS);
    private static final String DROP_STATEMENT_SESSIONS =  String.format(DROP_STATEMENT, TABLE_SESSIONS);
    // ============================================================================================

    // Global Vars
    // ============================================================================================
    private String email;
    private Context context;
    private Gson gson;
    // ============================================================================================

    // ######################################################################################### //
    // WorkoutHelper Constructor                                                                 //
    // ######################################################################################### //
    public WorkoutHelper(Context context) {
        super(context, "workouts", null, DATABASE_VERSION);
        this.context = context;
        this.email = CurrUser.getInstance().getEmail();

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
    private byte[] getBlobFromList(ArrayList<?> list) {
        return gson.toJson(list).getBytes();
    }

    /* Convert Blob of Exercises to Exercise ArrayList format */
    private ArrayList<?> getListFromBlob(byte[] blob) {
        return gson.fromJson(new String(blob), new TypeToken<ArrayList<?>>() {}.getType());
    }

    /* Checks to see if the database exists */
    private boolean exists() {
        return (new File(context.getDatabasePath(TABLE_WORKOUTS).toString())).exists();
    }


    // CRUD functions
    // ============================================================================================
    // CREATE
    // --------------------------------------------------------------------------------------------
    /* Creates a db entry for the workout
     */
    public void addWorkout(Workout workout) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues newValues = new ContentValues();

        newValues.put(WORKOUT_NAME,  workout.getName());
        newValues.put(EXERCISES,     getBlobFromList(workout.getExercises()));
        newValues.put(EMAIL,         email);

        db.insert(TABLE_WORKOUTS, null, newValues);
    }

    /* Creates a db entry for each workout in the provided ArrayList
     */
    public void addWorkouts(ArrayList<Workout> workouts) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues newValues;

        for (Workout workout : workouts) {
            // put that data into the database
            newValues = new ContentValues();

            newValues.put(WORKOUT_NAME, workout.getName());
            newValues.put(EXERCISES, getBlobFromList(workout.getExercises()));
            newValues.put(EMAIL, email);
            db.insert(TABLE_WORKOUTS, null, newValues);
        }
    }

    /* Creates a db entry for the session
     */
    public void addSession(Session session) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues newValues = new ContentValues();

        ArrayList<String> exerciseNames = session.getExerciseNames();
        ArrayList<ArrayList<Set>> sets = session.getAllSets();

        newValues.put(TIMESTAMP, session.getTimestamp());
        newValues.put(WORKOUT_NAME, session.getWorkoutName());
        newValues.put(EMAIL, email);

        for (int i = 0; i < exerciseNames.size(); i++) {
            newValues.put(EXERCISE_NAME, exerciseNames.get(i));
            newValues.put(SETS, getBlobFromList(sets.get(i)));

            db.insert(TABLE_SESSIONS, null, newValues);
            newValues.clear();
        }
    }
    // --------------------------------------------------------------------------------------------

    // RETRIEVE
    // --------------------------------------------------------------------------------------------
    /* Checks if user's email exists in db
     */
    public boolean emailExists() {
        boolean emailExists = false;

        if (exists()) {
            SQLiteDatabase db = this.getReadableDatabase();

            // Get unique workout names
            String[] column = new String[]{EMAIL};
            String where = EMAIL + " = ?";
            String[] whereArgs = new String[]{email};

            Cursor workoutCursor = db.query(
                    true, // true for unique
                    TABLE_WORKOUTS,
                    column,
                    where,
                    whereArgs,
                    EMAIL,
                    null,
                    null,
                    null
            );

            if (workoutCursor.getCount() == 1) {
                emailExists = true;
            }
            workoutCursor.close();
        }

        return emailExists;
    }

    /* Check if a workout exists in the database
     */
    public boolean workoutExists(String workoutName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Workout workout = null;

        String[] columns   = new String[] {EXERCISES};
        String where       = WORKOUT_NAME + " = ? AND " + EMAIL + " = ?";
        String[] whereArgs = new String[] {workoutName, email};

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

    /* Get a Workout object using the workout name
     */
    public Workout getWorkout(String workoutName) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Exercise> exercises = new ArrayList<>();
        Workout workout = null;

        String[] columns   = new String[] {EXERCISES};
        String where       = WORKOUT_NAME + " = ? AND " + EMAIL + " = ?";
        String[] whereArgs = new String[] {workoutName, email};

        Cursor cursor = db.query(TABLE_WORKOUTS, columns, where, whereArgs, "", "", "");

        if (cursor.moveToFirst()) {

            exercises = (ArrayList<Exercise>) getListFromBlob(cursor.getBlob(0));

            if (exercises != null)
                workout = new Workout(workoutName, exercises);
        }
        cursor.close();

        return workout;
    }

    /* Gets all workouts in the db and returns a list of Workout objects
     */
    public ArrayList<Workout> getAllWorkouts() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Workout> workouts = new ArrayList<>();
        ArrayList<Exercise> exercises;

        String[] column      = new String[] {WORKOUT_NAME, EXERCISES};
        String where         = EMAIL + " = ?";
        String[] whereArgs   = new String[] {email};

        Cursor cursor = db.query(TABLE_WORKOUTS, column, where, whereArgs, WORKOUT_NAME, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                exercises = (ArrayList<Exercise>) getListFromBlob(cursor.getBlob(1));

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
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> workoutNames = new ArrayList<>();

        // Get workout names
        String[] column      = new String[] {WORKOUT_NAME};
        String where         = EMAIL + " = ?";
        String[] whereArgs   = new String[] {email};

        if (email != null) {
            Cursor cursor = db.query(TABLE_WORKOUTS, column, where, whereArgs, WORKOUT_NAME, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    workoutNames.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return workoutNames;
    }


    // UPDATE
    // --------------------------------------------------------------------------------------------
    /* Updates the weight for an exercise in a workout
     */
//    public boolean updateWeight(String workoutName, String exerciseName, float newWeight) {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//
//        ContentValues newValues = new ContentValues();
//        newValues.put(WEIGHT, newWeight);
//
//        String where = EMAIL + " = ? AND " + WORKOUT_NAME + " = ? AND " + EXERCISE_NAME + " = ?";
//        String[] whereArgs = new String[] {email, workoutName, exerciseName};
//
//        int numRows = db.update(TABLE_WORKOUTS, newValues, where, whereArgs);
//
//        return (numRows == 1);
//    }

    // DELETE
    // --------------------------------------------------------------------------------------------
    /* Deletes a workout
     */
    public boolean deleteWorkout(String workoutName) {
        SQLiteDatabase db = this.getWritableDatabase();

        String where       = WORKOUT_NAME + " = ? AND " + EMAIL + " = ?";
        String[] whereArgs = new String[] {workoutName, email};
        int numRows        = db.delete(TABLE_WORKOUTS, where, whereArgs);

        return (numRows > 0);
    }

    /* Deletes all workouts
     */
    public void deleteAllWorkouts() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_WORKOUTS, "", new String[] {});
    }
    // --------------------------------------------------------------------------------------------
    // ============================================================================================
}
