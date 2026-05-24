package ca.gainzassist.models

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.*

class WorkoutHelper(private val context: Context) :
    SQLiteOpenHelper(context, "workouts", null, DATABASE_VERSION) {

    private var db: SQLiteDatabase? = null
    private val cv = ContentValues()
    private val gson = Gson()

    override fun onCreate(db: SQLiteDatabase) {
        createTables(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersionNum: Int, newVersionNum: Int) {
        db.execSQL(DROP_STATEMENT_WORKOUTS)
        db.execSQL(DROP_STATEMENT_SESSIONS)
        createTables(db)
    }

    private fun createTables(db: SQLiteDatabase) {
        db.execSQL(CREATE_STATEMENT_WORKOUTS)
        db.execSQL(CREATE_STATEMENT_SESSIONS)
    }

    private fun getBlobFromExercises(exercises: ArrayList<Exercise>): ByteArray {
        return gson.toJson(exercises).toByteArray()
    }

    private fun getExercisesFromBlob(blob: ByteArray): ArrayList<Exercise> {
        return gson.fromJson(String(blob), object : TypeToken<ArrayList<Exercise>>() {}.type)
    }

    private fun getBlobFromSets(exerciseSets: ArrayList<ExerciseSet>): ByteArray {
        return gson.toJson(exerciseSets).toByteArray()
    }

    private fun getSetsFromBlob(blob: ByteArray): ArrayList<ExerciseSet> {
        return gson.fromJson(String(blob), object : TypeToken<ArrayList<ExerciseSet>>() {}.type)
    }

    fun exists(): Boolean {
        return File(context.getDatabasePath(TABLE_WORKOUTS).toString()).exists()
    }

    fun addWorkout(workout: Workout) {
        db = this.writableDatabase
        cv.clear()
        cv.put(WORKOUT_NAME, workout.name)
        cv.put(EXERCISES, getBlobFromExercises(workout.exercises))
        // db.insert(TABLE_WORKOUTS, null, cv);
    }

    fun workoutExists(workoutName: String): Boolean {
        db = this.readableDatabase
        val columns = arrayOf(EXERCISES)
        val where = "$WORKOUT_NAME = ?"
        val whereArgs = arrayOf(workoutName)
        val cursor = db!!.query(TABLE_WORKOUTS, columns, where, whereArgs, "", "", "")
        val count = cursor.count
        cursor.close()
        return count >= 1
    }

    fun getWorkout(workoutName: String): Workout? {
        var workout: Workout? = null
        db = this.readableDatabase
        val columns = arrayOf(EXERCISES)
        val where = "$WORKOUT_NAME = ?"
        val whereArgs = arrayOf(workoutName)
        val cursor = db!!.query(TABLE_WORKOUTS, columns, where, whereArgs, "", "", "")
        if (cursor.moveToFirst()) {
            val exercises = getExercisesFromBlob(cursor.getBlob(0))
            workout = Workout(workoutName, exercises)
        }
        cursor.close()
        return workout
    }

    fun getAllWorkouts(): ArrayList<Workout> {
        db = this.readableDatabase
        val workouts = ArrayList<Workout>()
        val column = arrayOf(WORKOUT_NAME, EXERCISES)
        val cursor = db!!.query(TABLE_WORKOUTS, column, null, null, WORKOUT_NAME, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                val exercises = getExercisesFromBlob(cursor.getBlob(1))
                workouts.add(Workout(cursor.getString(0), exercises))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return workouts
    }

    fun getAllWorkoutNames(): ArrayList<String> {
        db = this.readableDatabase
        val workoutNames = ArrayList<String>()
        val column = arrayOf(WORKOUT_NAME)
        val cursor = db!!.query(TABLE_WORKOUTS, column, null, null, WORKOUT_NAME, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                workoutNames.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return workoutNames
    }

    fun updateWorkout(workoutName: String, workout: Workout): Boolean {
        db = this.writableDatabase
        cv.clear()
        cv.put(EXERCISES, getBlobFromExercises(workout.exercises))
        val where = "$WORKOUT_NAME = ?"
        val whereArgs = arrayOf(workoutName)
        val numRows = db!!.update(TABLE_WORKOUTS, cv, where, whereArgs)
        return numRows == 1
    }

    fun deleteWorkout(workoutName: String): Boolean {
        db = this.writableDatabase
        val where = "$WORKOUT_NAME = ?"
        val whereArgs = arrayOf(workoutName)
        val numRows = db!!.delete(TABLE_WORKOUTS, where, whereArgs)
        return numRows > 0
    }

    fun deleteAllWorkouts() {
        db = this.writableDatabase
        db!!.delete(TABLE_WORKOUTS, null, null)
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val TABLE_WORKOUTS = "workouts"
        private const val TABLE_SESSIONS = "sessions"
        private const val WORKOUT_NAME = "workoutName"
        private const val EXERCISES = "exercises"
        private const val TIMESTAMP = "timestamp"
        private const val EXERCISE_NAME = "exerciseName"
        private const val SETS = "setsList"

        private const val CREATE_STATEMENT = "CREATE TABLE %s"
        private val CREATE_STATEMENT_WORKOUTS = String.format(
            CREATE_STATEMENT,
            "$TABLE_WORKOUTS ($WORKOUT_NAME text not null, $EXERCISES blob not null, PRIMARY KEY ($WORKOUT_NAME))"
        )
        private val CREATE_STATEMENT_SESSIONS = String.format(
            CREATE_STATEMENT,
            "$TABLE_SESSIONS ($TIMESTAMP integer not null, $WORKOUT_NAME text not null, $EXERCISE_NAME text not null, $SETS blob not null, PRIMARY KEY ($TIMESTAMP))"
        )
        private const val DROP_STATEMENT = "DROP TABLE %s"
        private val DROP_STATEMENT_WORKOUTS = String.format(DROP_STATEMENT, TABLE_WORKOUTS)
        private val DROP_STATEMENT_SESSIONS = String.format(DROP_STATEMENT, TABLE_SESSIONS)
    }
}
