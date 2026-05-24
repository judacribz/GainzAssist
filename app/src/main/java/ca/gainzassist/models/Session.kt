package ca.gainzassist.models

import androidx.room.*
import android.util.SparseArray
import ca.gainzassist.constants.ExerciseConst
import java.util.*

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
        this.initTimestamp(-1)
        this.workoutId = workout.id
        this.workoutName = workout.name
    }

    fun initTimestamp(timestamp: Long) { this.timestamp = if (timestamp == -1L) Date().time else timestamp }

    fun addExercise(exercise: Exercise) {
        var weight = 0.0f
        val weightChange = exercise.weightChange
        val expectedReps = exercise.reps.toFloat()
        val finishedSets = exercise.getFinishedSetsList()
        
        for (exerciseSet in finishedSets) {
            weight += exerciseSet.weight * exerciseSet.reps.toFloat() / if (expectedReps == 0f) 1f else expectedReps
        }

        weight = weight / (if (finishedSets.size == 0) 1 else finishedSets.size).toFloat() + weightChange
        if (weightChange != 0f) {
            weight -= weight % weightChange
        }

        if (avgWeights.get(exercise.exerciseNumber, -1f) != -1f) {
            this.sessionExs[exercise.exerciseNumber] = exercise
        } else {
            this.sessionExs.add(exercise)
        }

        this.avgWeights.put(exercise.exerciseNumber, weight)
    }

    fun remLastExercise() {
        if (sessionExs.isNotEmpty()) {
            this.sessionExs.removeAt(this.sessionExs.size - 1)
        }
    }

    fun toMap(): Map<String, Any?> {
        val exsMap = HashMap<String, Any?>()
        val sessionMap = HashMap<String, Any?>()
        sessionMap[ExerciseConst.WORKOUT_NAME] = workoutName
        sessionMap[ExerciseConst.WORKOUT_ID] = workoutId
        
        for (ex in sessionExs) {
            exsMap[ex.exerciseNumber.toString()] = ex.setsToMap()
        }
        sessionMap[ExerciseConst.EXERCISES] = exsMap

        return sessionMap
    }

    fun sessionStateMap(exerciseIndex: Int, setIndex: Int): Map<String, Any?> {
        val sessionStateMap = HashMap<String, Any?>()
        val sessionMap = this.toMap()

        sessionStateMap[ExerciseConst.SESSION] = sessionMap
        sessionStateMap[ExerciseConst.EXERCISE_INDEX] = exerciseIndex
        sessionStateMap[ExerciseConst.SET_INDEX] = setIndex

        return sessionStateMap
    }
}
