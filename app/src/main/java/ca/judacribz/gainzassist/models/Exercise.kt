package ca.judacribz.gainzassist.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import ca.judacribz.gainzassist.constants.ExerciseConst
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Date

@Entity(
    tableName = "exercises",
    foreignKeys = [ForeignKey(
        entity = Workout::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("workout_id"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["workout_id", "exercise_number"])]
)
@Parcelize
class Exercise(
    // Global Vars
    // --------------------------------------------------------------------------------------------
    @PrimaryKey
    var id: Long = Date().time,
    @JvmField
    @ColumnInfo(name = "workout_id")
    var workoutId: Long = -1,
    @JvmField
    @ColumnInfo(name = "exercise_number")
    var exerciseNumber: Int = 0,
    @JvmField
    var name: String = "",
    @JvmField
    var type: String = "",
    var equipment: String = "",
    @JvmField
    var sets: Int = 0,
    @JvmField
    var reps: Int = 0,
    @JvmField
    var weight: Float = 0f
) : Parcelable {

    @IgnoredOnParcel
    var weightChange = 0f

    @IgnoredOnParcel
    var minWeight = 0f

    @IgnoredOnParcel
    var setsList: ArrayList<ExerciseSet> = ArrayList()

    @IgnoredOnParcel
    var finishedSetsList = ArrayList<ExerciseSet>()

    enum class SetsType {
        WARMUP_SET,
        MAIN_SET
    }

    @IgnoredOnParcel
    var setsType: SetsType = SetsType.MAIN_SET

    // --------------------------------------------------------------------------------------------
    // ######################################################################################### //
    // Exercise Constructors                                                                     //
    // ######################################################################################### //
    // ######################################################################################### //

    @Ignore
    constructor(
        exerciseNumber: Int,
        name: String,
        type: String,
        equipment: String,
        setsList: ArrayList<ExerciseSet>,
        setsType: SetsType
    ) : this(
        workoutId = -1,
        exerciseNumber = exerciseNumber,
        name = name,
        type = type,
        equipment = equipment,
        sets = setsList.size,
        reps = setsList.firstOrNull()?.reps ?: 0,
        weight = setsList.firstOrNull()?.weight ?: 0f,
    ) {
        setExerciseBase(exerciseNumber, name, type, equipment)
        setSetsList(setsList)
        this.setsType = setsType
    }

    @Ignore
    constructor(
        exerciseNumber: Int,
        name: String,
        type: String,
        equipment: String,
        sets: Int,
        reps: Int,
        weight: Float,
        setsType: SetsType
    ) : this(
        workoutId = -1,
        exerciseNumber = exerciseNumber,
        name = name,
        type = type,
        equipment = equipment,
        sets = sets,
        reps = reps,
        weight = weight
    ) {
        setExerciseBase(exerciseNumber, name, type, equipment)
        this.sets = sets
        this.reps = reps
        this.weight = weight
        this.setsType = setsType
    }

    private fun setExerciseBase(
        exerciseNumber: Int,
        name: String,
        type: String,
        equipment: String
    ) {
        this.exerciseNumber = exerciseNumber
        this.name = name
        this.type = type
        setEquipment(equipment)
    }

    // ============================================================================================
    // Getters and setters
    // ============================================================================================

    private fun setEquipment(equipment: String) {
        this.equipment = equipment
        if (ExerciseConst.BARBELL == this.equipment) {
            minWeight = ExerciseConst.BB_MIN_WEIGHT
            weightChange = ExerciseConst.BB_WEIGHT_CHANGE
        } else if (ExerciseConst.DUMBBELL == this.equipment) {
            minWeight = ExerciseConst.DB_MIN_WEIGHT
            weightChange = ExerciseConst.DB_WEIGHT_CHANGE
        } else {
            minWeight = ExerciseConst.MIN_WEIGHT
            weightChange = ExerciseConst.WEIGHT_CHANGE
        }
    }

    private fun setSetsList(setsList: ArrayList<ExerciseSet>?) {
        if (setsList != null) {
            this.setsList = setsList.toList() as ArrayList<ExerciseSet>
        } else {
            for (i in 0 until (sets ?: 0)) {
                this.setsList.add(ExerciseSet(id, name, i, reps, weight))
            }
        }
    }

    fun updateSet(set: ExerciseSet) {
        finishedSetsList[set.setNumber] = set
    }

    fun addSet(set: ExerciseSet, genId: Boolean) {
        if (genId) {
            id = Date().time
        }
        finishedSetsList.add(set)
    }

    val avgWeight: Float
        get() {
            var weight = 0.0f
            for (exerciseSet in setsList) {
                weight += exerciseSet.weight
            }
            return weight / sets.toFloat()
        }
    val avgReps: Int
        get() {
            var reps = 0
            for (exerciseSet in setsList) {
                reps += exerciseSet.reps
            }
            return reps / sets
        }

    fun getSet(setIndex: Int): ExerciseSet {
        return setsList[setIndex]
    }

    val numSets: Int
        get() = setsList.size

    // ============================================================================================
    /* Misc function used to store Exercise information in the firebase db */
    fun toMap(): MutableMap<String, Any> {
        return object : HashMap<String, Any>() {
            init {
                id.let { put("id", it) }
                put("name", name.toString())
                put("type", type.toString())
                put("equipment", equipment.toString())
                sets.let { put("sets", it) }
                reps.let { put("reps", it) }
                weight.let { put("weight", it) }
            }
        }
    }

    fun setsToMap(): Map<String, Any> {
        val setMap: MutableMap<String, Any> = HashMap()
        val exMap = toMap()
        var success = true
        for (set in finishedSetsList) {
            setMap[set.setNumber.toString()] = set.toMap()
            if (set.reps < reps || set.weight < weight) {
                success = false
            }
        }
        exMap["setList"] = setMap
        if (finishedSetsList.size == sets) {
            exMap["success"] = success
        }
        return exMap
    }

    companion object {
        @Ignore
        val EQUIPMENT_TYPES = ArrayList(
            listOf(
                ExerciseConst.BARBELL,
                ExerciseConst.DUMBBELL,
                ExerciseConst.NA
            )
        )

        @Ignore
        val EXERCISE_TYPES = ArrayList(
            listOf(
                ExerciseConst.STRENGTH,
                ExerciseConst.CARDIOVASCULAR,
                ExerciseConst.PLYOMETRICS
            )
        )
    }
}
