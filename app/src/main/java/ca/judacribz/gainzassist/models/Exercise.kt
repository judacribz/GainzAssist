package ca.judacribz.gainzassist.models

import android.arch.persistence.room.*
import ca.judacribz.gainzassist.constants.ExerciseConst.BARBELL
import ca.judacribz.gainzassist.constants.ExerciseConst.DUMBBELL
import ca.judacribz.gainzassist.constants.ExerciseConst.BB_MIN_WEIGHT
import ca.judacribz.gainzassist.constants.ExerciseConst.BB_WEIGHT_CHANGE
import ca.judacribz.gainzassist.constants.ExerciseConst.DB_MIN_WEIGHT
import ca.judacribz.gainzassist.constants.ExerciseConst.DB_WEIGHT_CHANGE
import ca.judacribz.gainzassist.constants.ExerciseConst.MIN_WEIGHT
import ca.judacribz.gainzassist.constants.ExerciseConst.WEIGHT_CHANGE
import ca.judacribz.gainzassist.constants.ExerciseConst.NA
import ca.judacribz.gainzassist.constants.ExerciseConst.STRENGTH
import ca.judacribz.gainzassist.constants.ExerciseConst.CARDIOVASCULAR
import ca.judacribz.gainzassist.constants.ExerciseConst.PLYOMETRICS
import org.parceler.Parcel
import org.parceler.Parcel.Serialization
import org.parceler.Parcels
import java.util.*

@Parcel(Serialization.BEAN)
@Entity(
    tableName = "exercises",
    foreignKeys = [ForeignKey(
        entity = Workout::class,
        parentColumns = ["id"],
        childColumns = ["workout_id"],
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["workout_id", "exercise_number"])]
)
class Exercise {

    @PrimaryKey
    var id: Long = -1

    @ColumnInfo(name = "workout_id")
    var workoutId: Long = -1

    @ColumnInfo(name = "exercise_number")
    var exerciseNumber: Int = 0

    var name: String? = null
    var type: String? = null
    var equipment: String? = null
    var sets: Int = 0
    var reps: Int = 0
    var weight: Float = 0f

    @Ignore
    var weightChange: Float = 0f
    @Ignore
    var minWeight: Float = 0f

    @Ignore
    var setsList = ArrayList<ExerciseSet>()
    @Ignore
    var finSets = ArrayList<ExerciseSet>()

    val finishedSetsList: ArrayList<ExerciseSet>
        get() = finSets

    enum class SetsType {
        WARMUP_SET, MAIN_SET
    }

    @Ignore
    var setsType = SetsType.MAIN_SET

    constructor()

    @Ignore
    constructor(
        exerciseNumber: Int,
        name: String?,
        type: String?,
        equipment: String?,
        setsList: ArrayList<ExerciseSet>?,
        setsType: SetsType
    ) {
        setExerciseBase(exerciseNumber, name, type, equipment)
        this.setsType = setsType
        if (setsList != null) {
            this.setsList = setsList
            this.sets = setsList.size
            if (sets > 0) {
                this.reps = setsList[0].reps
                this.weight = setsList[0].weight
            }
        }
    }

    @Ignore
    constructor(
        exerciseNumber: Int,
        name: String?,
        type: String?,
        equipment: String?,
        sets: Int,
        reps: Int,
        weight: Float,
        setsType: SetsType
    ) {
        setExerciseBase(exerciseNumber, name, type, equipment)
        this.sets = sets
        this.reps = reps
        this.weight = weight
        this.setsType = setsType
        initializeSetsList()
    }

    fun initializeSetsList() {
        if (setsList.isEmpty()) {
            for (i in 0 until sets) {
                setsList.add(ExerciseSet(id, name, i, reps, weight))
            }
        }
    }

    private fun setExerciseBase(
        exerciseNumber: Int,
        name: String?,
        type: String?,
        equipment: String?
    ) {
        id = -1
        this.exerciseNumber = exerciseNumber
        this.name = name
        this.type = type
        this.equipment = equipment
    }

    fun updateSet(set: ExerciseSet) {
        finSets[set.setNumber] = set
    }

    fun addSet(set: ExerciseSet, genId: Boolean) {
        if (genId) {
            set.id = -1
        }
        finSets.add(set)
    }

    val avgWeight: Float
        get() {
            initializeSetsList()
            var weight = 0.0f
            for (exerciseSet in setsList) {
                weight += exerciseSet.weight
            }
            return if (sets > 0) weight / sets.toFloat() else 0f
        }

    val avgReps: Int
        get() {
            initializeSetsList()
            var reps = 0
            for (exerciseSet in setsList) {
                reps += exerciseSet.reps
            }
            return if (sets > 0) reps / sets else 0
        }

    fun getSet(setIndex: Int): ExerciseSet {
        initializeSetsList()
        return setsList[setIndex]
    }

    val numSets: Int
        get() {
            initializeSetsList()
            return setsList.size
        }

    fun toMap(): Map<String, Any?> {
        val map = HashMap<String, Any?>()
        map["id"] = id
        map["name"] = name
        map["type"] = type
        map["equipment"] = equipment
        map["sets"] = sets
        map["reps"] = reps
        map["weight"] = weight
        return map
    }

    fun setsToMap(): Map<String, Any?> {
        val setMap = HashMap<String, Any?>()
        val exMap = toMap() as HashMap<String, Any?>
        var success = true

        for (set in finSets) {
            setMap[set.setNumber.toString()] = set.toMap()
            if (set.reps < reps || set.weight < weight) {
                success = false
            }
        }

        exMap["setList"] = setMap
        if (finSets.size == sets) {
            exMap["success"] = success
        }

        return exMap
    }

    companion object {
        @Ignore
        @JvmField
        val EQUIPMENT_TYPES = arrayListOf(BARBELL, DUMBBELL, NA)

        @Ignore
        @JvmField
        val EXERCISE_TYPES = arrayListOf(STRENGTH, CARDIOVASCULAR, PLYOMETRICS)
    }
}
