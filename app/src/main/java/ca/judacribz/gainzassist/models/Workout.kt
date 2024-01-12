package ca.judacribz.gainzassist.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import ca.judacribz.gainzassist.util.Misc
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.Locale

@Parcelize
@Entity(tableName = "workouts", indices = [Index(value = ["name"], unique = true)])
class Workout(
    // Global Vars
    // --------------------------------------------------------------------------------------------
    @PrimaryKey
    @JvmField
    var id: Long = -1,
    @JvmField
var name: String = "",
) : Parcelable {

    @IgnoredOnParcel
    @JvmField
    @Ignore
    var exercises = ArrayList<Exercise>()

    // --------------------------------------------------------------------------------------------
    // ######################################################################################### //
    // ExerciseConst Constructor                                                                 //
    // ######################################################################################### //
    constructor(name: String, exercises: ArrayList<Exercise>) : this() {
        if (id == -1L) setId(-1)
        this.name = name
        if (exercises != null) {
            this.exercises = exercises
        }
    }

    // ######################################################################################### //
    // Getters and setters
    // ============================================================================================
    fun getId(): Long {
        return id
    }

    fun setId(id: Long) {
        this.id = if (id == -1L) Date().time else id
    }

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
        var i: Int
        for (ex in exercises) {
            i = exercises.indexOf(ex)
            ex?.exerciseNumber = i
        }
    }

    fun getExerciseNumber(exerciseName: String): Int {
        val ex = getExerciseFromName(exerciseName)
        return ex?.exerciseNumber ?: -1
    }

    fun getExerciseFromName(exName: String): Exercise? {
        for (exercise in exercises) {
            if (exercise?.name == exName) {
                return exercise
            }
        }
        return null
    }

    fun getExerciseFromIndex(exIndex: Int): Exercise? {
        return exercises[exIndex]
    }

    // ============================================================================================
    // Misc functions
    // --------------------------------------------------------------------------------------------
    /* Misc function used to store ExerciseConst information in the FireBase db */
    fun toMap(): Map<String, Any> {
        val workout: MutableMap<String, Any> = HashMap()
        val exs = Misc.exerciseToMap(exercises)
        workout["id"] = id
        workout["exercises"] = exs
        return workout
    }

    /* Returns true if the exercise exist, false if not */
    fun containsExercise(exerciseName: String): Boolean {
        for (exercise in exercises) {
            if (exercise?.name?.lowercase(Locale.getDefault()) == exerciseName.lowercase(Locale.getDefault())
            ) {
                return true
            }
        }
        return false
    }

    val exerciseNames: ArrayList<String>
        get() {
            val exerciseNames = ArrayList<String>()
            for (exercise in exercises) {
                exercise?.name?.let { exerciseNames.add(it) }
            }
            return exerciseNames
        }
    val numExercises: Int
        get() = exercises.size // --------------------------------------------------------------------------------------------
}