package ca.gainzassist.domain.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "exercise_sets")
class ExerciseSet : Parcelable {

    @PrimaryKey
    var id: Long = -1
        set(value) {
            field = if (value == -1L) Date().time else value
        }

    @ColumnInfo(name = "session_id")
    var sessionId: Long = 0

    @ColumnInfo(name = "exercise_id")
    var exerciseId: Long = 0

    @ColumnInfo(name = "set_number")
    var setNumber: Int = 0

    var reps: Int = 0

    var weight: Float = 0f

    @ColumnInfo(name = "exercise_name")
    var exerciseName: String? = null

    constructor()

    @Ignore
    constructor(exercise: Exercise, setNumber: Int, reps: Int, weight: Float) : this(
        exercise.id,
        exercise.name,
        setNumber,
        reps,
        weight
    )

    @Ignore
    constructor(exerciseId: Long, exerciseName: String?, setNumber: Int, reps: Int, weight: Float) {
        this.id = -1
        this.exerciseId = exerciseId
        this.exerciseName = exerciseName
        this.setNumber = setNumber
        this.reps = reps
        this.weight = weight
    }

    fun toMap(): Map<String, Any?> {
        val exerciseSetMap = HashMap<String, Any?>()
        exerciseSetMap["id"] = id
        exerciseSetMap["reps"] = reps
        exerciseSetMap["weight"] = weight
        return exerciseSetMap
    }

    constructor(parcel: Parcel) {
        id = parcel.readLong()
        sessionId = parcel.readLong()
        exerciseId = parcel.readLong()
        setNumber = parcel.readInt()
        reps = parcel.readInt()
        weight = parcel.readFloat()
        exerciseName = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(sessionId)
        parcel.writeLong(exerciseId)
        parcel.writeInt(setNumber)
        parcel.writeInt(reps)
        parcel.writeFloat(weight)
        parcel.writeString(exerciseName)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ExerciseSet> {
        override fun createFromParcel(parcel: Parcel): ExerciseSet = ExerciseSet(parcel)
        override fun newArray(size: Int): Array<ExerciseSet?> = arrayOfNulls(size)
    }
}
