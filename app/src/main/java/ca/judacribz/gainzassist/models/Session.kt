package ca.judacribz.gainzassist.models

import android.util.SparseArray
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import ca.judacribz.gainzassist.constants.ExerciseConst.EXERCISES
import ca.judacribz.gainzassist.constants.ExerciseConst.EXERCISE_INDEX
import ca.judacribz.gainzassist.constants.ExerciseConst.SESSION
import ca.judacribz.gainzassist.constants.ExerciseConst.SET_INDEX
import ca.judacribz.gainzassist.constants.ExerciseConst.WORKOUT_ID
import ca.judacribz.gainzassist.constants.ExerciseConst.WORKOUT_NAME
import java.util.Date

@Entity(
    tableName = "sessions",
    indices = [Index("workout_id"), Index(value = ["workout_id", "timestamp"])]
)
class Session {
    // Global Vars
    // --------------------------------------------------------------------------------------------
    @PrimaryKey
    var timestamp: Long = 0

    // ######################################################################################### //
    // Getters and setters
    // ============================================================================================
    @ColumnInfo(name = "workout_id")
    var workoutId: Long = 0

    @ColumnInfo(name = "workout_name")
    lateinit var workoutName: String

    @Ignore
    var sessionExs = ArrayList<Exercise>()

    @Ignore
    var avgWeights = SparseArray<Float?>()

    // --------------------------------------------------------------------------------------------
    // ######################################################################################### //
    // Session Constructors                                                                     //
    // ######################################################################################### //
    constructor()

    @Ignore
    constructor(workout: Workout) {
        setTimestamp1(-1)
        workoutId = workout.getId()
        workoutName = workout.name.toString()
    }

    fun setTimestamp1(timestamp: Long) {
        this.timestamp = if (timestamp == -1L) Date().time else timestamp
    }

    // ============================================================================================
    fun addExercise(exercise: Exercise) {
        var weight = 0.0f
        val weightChange = exercise.weightChange
        val expectedReps = exercise.reps?.toFloat()
        for (exerciseSet in exercise.finishedSetsList) {
            weight += (exerciseSet.reps?.let { exerciseSet.weight?.times(it.toFloat()) } ?: 0f) / expectedReps!!
        }
        weight = weight / exercise.finishedSetsList.size + weightChange
        weight -= weight % weightChange
        if (avgWeights[exercise.exerciseNumber!!, -1f] != -1f) {
            sessionExs[exercise.exerciseNumber!!] = exercise
        } else {
            sessionExs.add(exercise)
        }
        avgWeights.put(exercise.exerciseNumber!!, weight)

//        for (Exercise ex : sessionExs) {
//            Logger.d("EXERCISE # " + ex.getName() + " " + avgWeights.get(ex.getName()));
//        }
    }

    fun remLastExercise() {
        sessionExs.removeAt(sessionExs.size - 1)
    }

    /* Misc function used to store Session information in the firebase db */
    fun toMap(): Map<String, Any> {
        val exsMap: MutableMap<String, Any> = HashMap()
        val sessionMap: MutableMap<String, Any> = object : HashMap<String, Any>() {
            init {
                put(WORKOUT_NAME, workoutName)
                put(WORKOUT_ID, workoutId)
            }
        }
        for (ex in sessionExs) {
            exsMap[ex.exerciseNumber.toString()] = ex.setsToMap()
        }
        sessionMap[EXERCISES] = exsMap
        return sessionMap
    }

    fun sessionStateMap(exerciseIndex: Int, setIndex: Int): Map<String, Any> {
        val sessionStateMap: MutableMap<String, Any> = HashMap()
        val sessionMap = this.toMap()
        sessionStateMap[SESSION] = sessionMap
        sessionStateMap[EXERCISE_INDEX] = exerciseIndex
        sessionStateMap[SET_INDEX] = setIndex
        return sessionStateMap
    }
}
