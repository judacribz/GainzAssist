package ca.gainzassist.domain.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import ca.gainzassist.core.constants.ExerciseConst.BARBELL
import ca.gainzassist.core.constants.ExerciseConst.BB_MIN_WEIGHT
import ca.gainzassist.core.constants.ExerciseConst.BB_WEIGHT_CHANGE
import ca.gainzassist.core.constants.ExerciseConst.DB_MIN_WEIGHT
import ca.gainzassist.core.constants.ExerciseConst.DB_WEIGHT_CHANGE
import ca.gainzassist.core.constants.ExerciseConst.DUMBBELL
import ca.gainzassist.core.constants.ExerciseConst.MIN_WEIGHT
import ca.gainzassist.core.constants.ExerciseConst.WEIGHT_CHANGE
import java.util.Date

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
class Exercise : Parcelable {

    @PrimaryKey
    var id: Long = -1
        set(value) {
            field = if (value == -1L) Date().time else value
        }

    @ColumnInfo(name = "workout_id")
    var workoutId: Long = -1

    @ColumnInfo(name = "exercise_number")
    var exerciseNumber: Int = 0

    var name: String? = null
    var type: String? = null
    var equipment: String? = null
        set(value) {
            field = value

            when (value) {
                BARBELL -> {
                    minWeight = BB_MIN_WEIGHT
                    weightChange = BB_WEIGHT_CHANGE
                }

                DUMBBELL -> {
                    minWeight = DB_MIN_WEIGHT
                    weightChange = DB_WEIGHT_CHANGE
                }

                else -> {
                    minWeight = MIN_WEIGHT
                    weightChange = WEIGHT_CHANGE
                }
            }
        }

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
        initSetsList(setsList)
        this.setsType = setsType
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
        initSetsList(null)
    }

    private fun setExerciseBase(
        exerciseNumber: Int,
        name: String?,
        type: String?,
        equipment: String?
    ) {
        this.id = -1
        this.exerciseNumber = exerciseNumber
        this.name = name
        this.type = type
        this.equipment = equipment
    }

    fun initSetsList(setsList: ArrayList<ExerciseSet>?) {
        if (setsList != null) {
            this.setsList = setsList
        } else if (this.setsList.isEmpty() && sets > 0) {
            this.setsList = ArrayList()
            for (i in 0 until sets) {
                this.setsList.add(ExerciseSet(id, name, i, reps, weight))
            }
        }
    }

    fun getFinishedSetsList(): ArrayList<ExerciseSet> = finSets

    fun addSet(set: ExerciseSet, genId: Boolean) {
        if (genId) {
            set.id = -1
        }
        finSets.add(set)
    }

    fun getAvgWeight(): Float {
        initSetsList(null)

        var totalWeight = 0.0f
        for (exerciseSet in setsList) {
            totalWeight += exerciseSet.weight
        }
        return if (sets > 0) totalWeight / sets.toFloat() else 0f
    }

    fun getAvgReps(): Int {
        initSetsList(null)

        var totalReps = 0
        for (exerciseSet in setsList) {
            totalReps += exerciseSet.reps
        }
        return if (sets > 0) totalReps / sets else 0
    }

    fun getSet(setIndex: Int): ExerciseSet {
        initSetsList(null)

        check(!(setIndex < 0 || setIndex >= setsList.size)) {
            "Exercise '$name' has invalid setIndex=$setIndex, sets=$sets, setsList.size=${setsList.size}, reps=$reps, weight=$weight, equipment=$equipment"
        }

        return setsList[setIndex]
    }

    fun getNumSets(): Int {
        initSetsList(null)
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

    constructor(parcel: Parcel) {
        id = parcel.readLong()
        workoutId = parcel.readLong()
        exerciseNumber = parcel.readInt()
        name = parcel.readString()
        type = parcel.readString()
        equipment = parcel.readString()
        sets = parcel.readInt()
        reps = parcel.readInt()
        weight = parcel.readFloat()
        weightChange = parcel.readFloat()
        minWeight = parcel.readFloat()
        setsList = parcel.createTypedArrayList(ExerciseSet.CREATOR) ?: ArrayList()
        finSets = parcel.createTypedArrayList(ExerciseSet.CREATOR) ?: ArrayList()
        setsType = SetsType.valueOf(parcel.readString() ?: SetsType.MAIN_SET.name)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(workoutId)
        parcel.writeInt(exerciseNumber)
        parcel.writeString(name)
        parcel.writeString(type)
        parcel.writeString(equipment)
        parcel.writeInt(sets)
        parcel.writeInt(reps)
        parcel.writeFloat(weight)
        parcel.writeFloat(weightChange)
        parcel.writeFloat(minWeight)
        parcel.writeTypedList(setsList)
        parcel.writeTypedList(finSets)
        parcel.writeString(setsType.name)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Exercise> {
        override fun createFromParcel(parcel: Parcel): Exercise = Exercise(parcel)
        override fun newArray(size: Int): Array<Exercise?> = arrayOfNulls(size)
    }
}
