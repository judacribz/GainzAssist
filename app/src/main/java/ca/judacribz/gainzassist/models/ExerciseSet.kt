package ca.judacribz.gainzassist.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.parceler.Parcel
import org.parceler.Parcel.Serialization
import java.util.*

@Parcel(Serialization.BEAN)
@Entity(tableName = "exercise_sets")
class ExerciseSet {

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

    fun initId(id: Long) { this.id = if (id == -1L) Date().time else id }

    fun toMap(): Map<String, Any?> {
        val exerciseSetMap = HashMap<String, Any?>()
        exerciseSetMap["id"] = id
        exerciseSetMap["reps"] = reps
        exerciseSetMap["weight"] = weight
        return exerciseSetMap
    }
}
