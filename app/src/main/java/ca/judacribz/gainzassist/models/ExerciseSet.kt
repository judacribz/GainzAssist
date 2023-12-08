package ca.judacribz.gainzassist.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@Entity(tableName = "exercise_sets")
class ExerciseSet( // Global Vars
    // --------------------------------------------------------------------------------------------
    @PrimaryKey
    var id: Long = Date().time,
    @JvmField
    @ColumnInfo(name = "session_id")
    var sessionId: Long = -1,
    @JvmField
    @ColumnInfo(name = "exercise_id")
    var exerciseId: Long,
    @JvmField
    @ColumnInfo(name = "set_number")
    var setNumber: Int,
    @JvmField
    var reps: Int,
    @JvmField
    var weight: Float,
    @JvmField
    @ColumnInfo(name = "exercise_name")
    var exerciseName: String
) : Parcelable {


    // --------------------------------------------------------------------------------------------
    // ######################################################################################### //
    // ExerciseSet Constructors                                                                          //
    // ######################################################################################### //
    @Ignore
    constructor(
        exercise: Exercise,
        setNumber: Int,
        reps: Int,
        weight: Float
    ) : this(
        exerciseId = exercise.id,
        exerciseName = exercise.name,
        setNumber = setNumber,
        reps = reps,
        weight = weight
    )

    @Ignore
    constructor(
        exerciseId: Long,
        exerciseName: String,
        setNumber: Int,
        reps: Int,
        weight: Float
    ) : this(
        sessionId = -1,
        exerciseId = exerciseId,
        setNumber = setNumber,
        reps = reps,
        weight = weight,
        exerciseName = exerciseName
    )

    // ============================================================================================
    // Misc function used to store ExerciseSet information in the FireBase db
    fun toMap() = hashMapOf(
        "id" to id,
        "reps" to reps,
        "weight" to weight
    )
}