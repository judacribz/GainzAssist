package ca.judacribz.gainzassist.models

import android.arch.persistence.room.*
import ca.judacribz.gainzassist.util.Misc.exerciseToMap
import org.parceler.Parcel
import org.parceler.Parcel.Serialization
import java.util.*

@Parcel(Serialization.BEAN)
@Entity(
    tableName = "workouts",
    indices = [Index(value = ["name"], unique = true)]
)
class Workout {

    @PrimaryKey
    var id: Long = -1

    var name: String? = null

    @Ignore
    var exercises = ArrayList<Exercise>()

    constructor()

    @Ignore
    constructor(name: String?, exercises: ArrayList<Exercise>?) {
        this.initId(-1)
        this.name = name
        if (exercises != null) {
            this.exercises = (exercises)
        }
    }

    fun initId(id: Long) { this.id = if (id == -1L) Date().time else id }

    fun addExercise(exercise: Exercise?) {
        if (exercise != null) {
            if (id != -1L) {
                exercise.workoutId = id
            }
            exercises.add(exercise)
        }
    }

    fun removeExercise(exercise: Exercise) {
        exercises.remove(exercise)
        exercises.trimToSize()
        for (ex in exercises) {
            ex.exerciseNumber = exercises.indexOf(ex)
        }
    }

    fun getExerciseNumber(exerciseName: String): Int {
        val ex = getExerciseFromName(exerciseName)
        return ex?.exerciseNumber ?: -1
    }

    fun getExerciseFromName(exName: String): Exercise? {
        for (exercise in exercises) {
            if (exercise.name == exName) {
                return exercise
            }
        }
        return null
    }

    fun getExerciseFromIndex(exIndex: Int): Exercise {
        return exercises[exIndex]
    }

    fun toMap(): Map<String, Any?> {
        val workout = HashMap<String, Any?>()
        val exs = exerciseToMap(exercises)
        workout["id"] = id
        workout["exercises"] = exs
        return workout
    }

    fun containsExercise(exerciseName: String): Boolean {
        for (exercise in exercises) {
            if (exercise.name.equals(exerciseName, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    val exerciseNames: ArrayList<String>
        get() {
            val names = ArrayList<String>()
            for (exercise in exercises) {
                exercise.name?.let(names::add)
            }
            return names
        }

    val numExercises: Int
        get() = exercises.size
}
