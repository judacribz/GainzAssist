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
    private static final String TABLE_WORKOUTS = "workouts";

    // Column names
    private static final String WORKOUT_NAME  = "workoutName";
    private static final String EXERCISES     = "exercises";
    private static final String EMAIL         = "email";

    // Workouts table create statement
    private static final String CREATE_STATEMENT =
            "CREATE TABLE " + TABLE_WORKOUTS + " (" +
                    WORKOUT_NAME  + " text not null," +
                    EXERCISES     + " blob not null," +
                    EMAIL         + " varchar2(100) not null," +
                    "PRIMARY KEY (" + WORKOUT_NAME + ", " + EMAIL + ")" +
            ")";

    private static final String DROP_STATEMENT = "DROP TABLE" + TABLE_WORKOUTS;
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

        // create the database, using CREATE SQL statement
        db.execSQL(CREATE_STATEMENT);
    }

    // TODO: needs to save data from old db before drop and write to new db when changing schema
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersionNum, int newVersionNum) {

        // delete the old database
        db.execSQL(DROP_STATEMENT);

        // re-create the database
        db.execSQL(CREATE_STATEMENT);
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
        newValues.put(EXERCISES,     getBlobFromExercises(workout.getExercises()));
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
            newValues.put(EXERCISES, getBlobFromExercises(workout.getExercises()));
            newValues.put(EMAIL, email);
            db.insert(TABLE_WORKOUTS, null, newValues);
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


    /* Get a Workout object using the workout name
     */
    public Workout getWorkout(String workoutName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Workout workout = null;

        String[] columns   = new String[] {EXERCISES};
        String where       = WORKOUT_NAME + " = ? AND " + EMAIL + " = ?";
        String[] whereArgs = new String[] {workoutName, email};

        Cursor cursor = db.query(TABLE_WORKOUTS, columns, where, whereArgs, "", "", "");

        if (cursor.moveToFirst()) {
            workout = new Workout(workoutName, getExercisesFromBlob(cursor.getBlob(0)));
        }
        cursor.close();

        return workout;
    }

    /* Gets all workouts in the db and returns a list of Workout objects
     */
    public ArrayList<Workout> getAllWorkouts() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Workout> workouts = new ArrayList<>();

        String[] column      = new String[] {WORKOUT_NAME, EXERCISES};
        String where         = EMAIL + " = ?";
        String[] whereArgs   = new String[] {email};

        Cursor cursor = db.query(TABLE_WORKOUTS, column, where, whereArgs, WORKOUT_NAME, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                workouts.add(new Workout(cursor.getString(0),
                                         getExercisesFromBlob(cursor.getBlob(1))));
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
