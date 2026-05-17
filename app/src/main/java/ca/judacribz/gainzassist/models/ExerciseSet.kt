package ca.judacribz.gainzassist.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import org.parceler.Parcel
import org.parceler.Parcel.Serialization
import java.util.*

@Parcel(Serialization.BEAN)
@Entity(tableName = "exercise_sets")
class ExerciseSet {

    @PrimaryKey
    var id: Long = -1

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
        id = -1
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
}
