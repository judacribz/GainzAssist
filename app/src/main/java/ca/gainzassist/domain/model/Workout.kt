package ca.gainzassist.domain.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import ca.gainzassist.core.util.Misc.exerciseToMap
import java.util.Date

@Entity(
    tableName = "workouts",
    indices = [Index(value = ["name"], unique = true)]
)
class Workout : Parcelable {

    @PrimaryKey
    var id: Long = -1
        set(value) {
            field = if (value == -1L) Date().time else value
        }
    var name: String? = null

    @Ignore
    var exercises = ArrayList<Exercise>()

    constructor()

    @Ignore
    constructor(name: String?, exercises: ArrayList<Exercise>?) {
        this.id = -1
        this.name = name
        if (exercises != null) {
            this.exercises = (exercises)
        }
    }

    fun addExercise(exercise: Exercise?) {
        if (exercise != null) {
            if (id != -1L) {
                exercise.workoutId = id
            }
            exercises.add(exercise)
        }
    }

    fun getExerciseFromIndex(exIndex: Int): Exercise = exercises[exIndex]

    fun toMap(): Map<String, Any?> {
        val workout = HashMap<String, Any?>()
        val exs = exerciseToMap(exercises)
        workout["id"] = id
        workout["exercises"] = exs
        return workout
    }

    val numExercises: Int
        get() = exercises.size

    constructor(parcel: Parcel) {
        id = parcel.readLong()
        name = parcel.readString()
        exercises = parcel.createTypedArrayList(Exercise.CREATOR) ?: ArrayList()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeTypedList(exercises)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Workout> {
        override fun createFromParcel(parcel: Parcel): Workout = Workout(parcel)
        override fun newArray(size: Int): Array<Workout?> = arrayOfNulls(size)
    }
}
