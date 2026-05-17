package ca.judacribz.gainzassist.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.util.SparseArray
import ca.judacribz.gainzassist.constants.ExerciseConst.EXERCISES
import ca.judacribz.gainzassist.constants.ExerciseConst.EXERCISE_INDEX
import ca.judacribz.gainzassist.constants.ExerciseConst.SESSION
import ca.judacribz.gainzassist.constants.ExerciseConst.SET_INDEX
import ca.judacribz.gainzassist.constants.ExerciseConst.WORKOUT_ID
import ca.judacribz.gainzassist.constants.ExerciseConst.WORKOUT_NAME
import java.util.Date

@Entity(
    tableName = "sessions",
    indices = [
        Index("workout_id"),
        Index(value = ["workout_id", "timestamp"])
    ]
)
class Session {

    @PrimaryKey
    var timestamp: Long = 0

    @ColumnInfo(name = "workout_id")
    var workoutId: Long = 0

    @ColumnInfo(name = "workout_name")
    var workoutName: String? = null

    @Ignore
    var sessionExs = ArrayList<Exercise>()

    @Ignore
    var avgWeights = SparseArray<Float>()

    constructor()

    @Ignore
    constructor(workout: Workout) {
        initializeTimestamp()
        workoutId = workout.id
        workoutName = workout.name
    }

    fun addExercise(exercise: Exercise) {
        if (exercise.finishedSetsList.isEmpty()) return
        
        var weight = 0.0f
        val weightChange = exercise.weightChange
        val expectedReps = exercise.reps.toFloat()
        for (exerciseSet in exercise.finishedSetsList) {
            weight += exerciseSet.weight * exerciseSet.reps.toFloat() / if (expectedReps == 0f) 1f else expectedReps
        }
        weight = weight / exercise.finishedSetsList.size + weightChange
        if (weightChange != 0f) {
            weight -= weight % weightChange
        }
        if (avgWeights.get(exercise.exerciseNumber, -1f) != -1f) {
            sessionExs[exercise.exerciseNumber] = exercise
        } else {
            sessionExs.add(exercise)
        }
        avgWeights.put(exercise.exerciseNumber, weight)
    }

    fun remLastExercise() {
        if (sessionExs.isNotEmpty()) {
            sessionExs.removeAt(sessionExs.size - 1)
        }
    }

    fun toMap(): Map<String, Any?> {
        val exsMap = HashMap<String, Any?>()
        val sessionMap = HashMap<String, Any?>()
        sessionMap[WORKOUT_NAME] = workoutName
        sessionMap[WORKOUT_ID] = workoutId
        for (ex in sessionExs) {
            exsMap[ex.exerciseNumber.toString()] = ex.setsToMap()
        }
        sessionMap[EXERCISES] = exsMap
        return sessionMap
    }

    fun sessionStateMap(exerciseIndex: Int, setIndex: Int): Map<String, Any?> {
        val sessionStateMap = HashMap<String, Any?>()
        val sessionMap = toMap()
        sessionStateMap[SESSION] = sessionMap
        sessionStateMap[EXERCISE_INDEX] = exerciseIndex
        sessionStateMap[SET_INDEX] = setIndex
        return sessionStateMap
    }

    private fun initializeTimestamp(timestamp: Long = -1L) {
        this.timestamp = if (timestamp == -1L) Date().time else timestamp
    }
}
