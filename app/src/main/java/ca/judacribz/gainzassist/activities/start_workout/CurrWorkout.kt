package ca.judacribz.gainzassist.activities.start_workout

import android.util.SparseArray
import ca.judacribz.gainzassist.models.Exercise
import ca.judacribz.gainzassist.models.Exercise.SetsType
import ca.judacribz.gainzassist.models.Exercise.SetsType.MAIN_SET
import ca.judacribz.gainzassist.models.Exercise.SetsType.WARMUP_SET
import ca.judacribz.gainzassist.models.ExerciseSet
import ca.judacribz.gainzassist.models.Session
import ca.judacribz.gainzassist.models.Workout
import ca.judacribz.gainzassist.util.Misc.enablePrettyMapper
import ca.judacribz.gainzassist.util.Misc.readValue
import ca.judacribz.gainzassist.util.Misc.writeValueAsString
import ca.judacribz.gainzassist.constants.ExerciseConst.MIN_WEIGHT
import ca.judacribz.gainzassist.constants.ExerciseConst.WEIGHT_CHANGE
import ca.judacribz.gainzassist.constants.ExerciseConst.EXERCISE_INDEX
import ca.judacribz.gainzassist.constants.ExerciseConst.SET_INDEX
import ca.judacribz.gainzassist.constants.ExerciseConst.SESSION
import ca.judacribz.gainzassist.constants.ExerciseConst.EXERCISES
import ca.judacribz.gainzassist.constants.ExerciseConst.SET_LIST
import ca.judacribz.gainzassist.constants.ExerciseConst.REPS
import ca.judacribz.gainzassist.constants.ExerciseConst.WEIGHT
import ca.judacribz.gainzassist.constants.ExerciseConst.BARBELL
import ca.judacribz.gainzassist.constants.ExerciseConst.MIN_REPS
import ca.judacribz.gainzassist.constants.ExerciseConst.HEAVY_REST_TIME
import ca.judacribz.gainzassist.constants.ExerciseConst.LIGHT_REST_TIME
import com.orhanobut.logger.Logger
import java.util.*

class CurrWorkout private constructor() {

    private var currWorkout: Workout? = null
    private var currExercise: Exercise? = null
    private var currExerciseSet: ExerciseSet? = null
    
    var currWeight = 0f
        private set
    var currMinWeight = MIN_WEIGHT
        private set
    private var currWeightChange = WEIGHT_CHANGE

    private var set_i = -1
    private var ex_i = -1
    
    var currReps = 0
        private set
        
    private var numWarmups = 0
    private var numMains = 0
    private var currWarmups: ArrayList<Exercise>? = null
    private var currMains: ArrayList<Exercise>? = null

    var currRestTime: Long = 0
        private set
        
    private var timerSet = false
    
    var lockReps = false
        private set
    var lockWeight = false
        private set
        
    var currSession: Session? = null
        private set

    var retrievedWorkout: Map<String, Any?>? = null

    interface DataListener {
        fun startTimer(timeInMillis: Long)
        fun updateProgressSets(numSets: Int)
    }

    private var dataListener: DataListener? = null

    fun setDataListener(dataListener: DataListener?) {
        this.dataListener = dataListener
        if (!timerSet) {
            setTimer()
        }
    }

    interface WarmupsListener {
        fun warmupsGenerated(warmups: ArrayList<Exercise>)
    }

    fun setDataListener(warmupsListener: WarmupsListener) {
        Companion.warmupsListener = warmupsListener
    }

    fun setRetrievedWorkout(map: Map<String, Any?>, workout: Workout) {
        resetIndices()
        this.retrievedWorkout = map
        this.currWarmups = ArrayList()
        this.currWorkout = workout
        this.currSession = Session(workout)

        this.ex_i = map[EXERCISE_INDEX]?.toString()?.toInt() ?: 0
        this.set_i = map[SET_INDEX]?.toString()?.toInt() ?: 0

        val sessionRestored = readValue(map[SESSION])
        val exsMap = readValue(sessionRestored[EXERCISES])
        
        for ((exNum, value) in exsMap) {
            val exMap = readValue(value)
            val exercise = workout.getExerciseFromIndex(exNum.toInt())
            exercise.setsType = MAIN_SET

            val setsMap = readValue(exMap[SET_LIST])
            for ((setKey, setValue) in setsMap) {
                val setMap = readValue(setValue)
                exercise.addSet(
                    ExerciseSet(
                        exercise,
                        setKey.toInt(),
                        setMap[REPS]?.toString()?.toInt() ?: 0,
                        setMap[WEIGHT]?.toString()?.toFloat() ?: 0f
                    ), false
                )
            }
            this.currSession!!.addExercise(exercise)
        }
        genWarmups(workout.exercises)
        enablePrettyMapper()
        Logger.d(writeValueAsString(this.currSession!!.sessionStateMap(ex_i, set_i)))
    }

    fun setCurrWorkout(workout: Workout) {
        resetIndices()
        this.currWorkout = workout
        this.currSession = Session(workout)
        genWarmups(workout.exercises)
    }

    private fun genWarmups(exercises: ArrayList<Exercise>) {
        val allExs = ArrayList<Exercise>()
        val warmups = ArrayList<Exercise>()
        var exerciseSets: ArrayList<ExerciseSet>
        var warmup: Exercise

        setCurrMainExercises(exercises)

        for (ex in exercises) {
            val equip = ex.equipment
            val minWeight = ex.minWeight
            val weight = ex.getAvgWeight()

            if (weight == minWeight) {
                allExs.add(ex)
                continue
            }

            exerciseSets = if (BARBELL == equip) {
                genBBWarmups(ex)
            } else {
                generateWarmups(ex)
            }

            warmup = Exercise(
                ex.exerciseNumber,
                ex.name,
                ex.type,
                ex.equipment,
                exerciseSets,
                WARMUP_SET
            )
            warmups.add(warmup)
            allExs.add(warmup)
            allExs.add(ex)
        }
        setCurrWarmupExercises(warmups)
        setAllCurrExercises(allExs)
        currMainInd = allExs[ex_i].exerciseNumber
    }

    private fun genBBWarmups(ex: Exercise): ArrayList<ExerciseSet> {
        val exerciseSets = ArrayList<ExerciseSet>()
        var setNum = 0
        var reps = ex.getAvgReps()
        val minWeight = ex.minWeight
        val weightChange = ex.weightChange
        val weight = ex.getAvgWeight()
        var weightInc: Float
        var newWeight = minWeight

        exerciseSets.add(ExerciseSet(ex, setNum++, reps, newWeight))
        exerciseSets.add(ExerciseSet(ex, setNum++, reps, newWeight))

        if (weight >= 405f) {
            weightInc = 90f
            newWeight += 90f
            while (newWeight <= 0.85f * weight) {
                if (newWeight >= 0.75f * weight) {
                    reps = ex.reps / 4 + 1
                } else if (newWeight > 0.65f * weight) {
                    reps = ex.reps / 2 + 1
                }
                exerciseSets.add(ExerciseSet(ex, setNum++, reps, newWeight))
                newWeight += weightInc
            }
        } else {
            newWeight += 50f
            weightInc = 40f
            while (newWeight <= 0.85f * weight) {
                if (newWeight >= 0.75f * weight) {
                    reps = ex.reps / 4 + 1
                } else if (newWeight > 0.65f * weight) {
                    reps = ex.reps / 2 + 1
                }
                exerciseSets.add(ExerciseSet(ex, setNum++, reps, newWeight))
                newWeight += weightInc
            }
        }
        reps = reps / 2 + 1
        while (true) {
            if (weightInc <= weightChange * 2) {
                return exerciseSets
            }
            if (newWeight < 0.91f * weight) {
                newWeight -= newWeight % weightChange
                exerciseSets.add(ExerciseSet(ex, setNum++, reps, newWeight))
                newWeight += weightInc
                reps = Math.max(reps / 2, 1)
            } else {
                newWeight -= weightInc
                weightInc /= 2f
                newWeight += weightInc
            }
        }
    }

    private fun generateWarmups(ex: Exercise): ArrayList<ExerciseSet> {
        val exerciseSets = ArrayList<ExerciseSet>()
        var setNum = 0
        var reps = ex.getAvgReps()
        val minWeight = ex.minWeight
        val weightChange = ex.weightChange
        val weight = ex.getAvgWeight()
        var newWeight = minWeight
        val diff = weight - minWeight

        if (diff == 0f) {
            return exerciseSets
        }

        var setsCount = Math.min(5, (diff / (weightChange * 2)).toInt())
        setsCount += (weight / 100).toInt()
        val percInc = 0.91f / setsCount.toFloat()
        var perc = percInc

        for (i in 0 until setsCount) {
            newWeight = perc * weight
            newWeight -= newWeight % (weightChange * 2)
            if (newWeight >= 0.65f * weight) {
                reps = reps / 2 + 1
            }
            exerciseSets.add(ExerciseSet(ex, setNum++, reps, newWeight))
            perc += percInc
        }
        return exerciseSets
    }

    private fun setCurrMainExercises(exercises: ArrayList<Exercise>) {
        this.currMains = exercises
        this.numMains = exercises.size
    }

    private fun setCurrWarmupExercises(warmups: ArrayList<Exercise>) {
        this.currWarmups = warmups
        this.numWarmups = warmups.size
        warmupsListener?.warmupsGenerated(warmups)
    }

    private fun setAllCurrExercises(allExercises: ArrayList<Exercise>) {
        this.currWorkout!!.exercises = allExercises
        if (this.ex_i == -1) {
            this.ex_i = 0
        }
        setCurrExercise(this.currWorkout!!.getExerciseFromIndex(this.ex_i))
    }

    var setSuccess = true
    var exSuccess = true
    var lastExSuccess = true

    fun finishCurrSet(): Boolean {
        this.set_i++
        if (!isWarmup) {
            addCurrSet()
        }
        if (atEndOfSets()) {
            resetLocks()
            this.set_i = 0
            this.ex_i++
            if (!isWarmup) {
                this.currSession!!.addExercise(this.currExercise!!)
            }
            if (atEndOfExercises()) {
                resetIndices()
                return false
            } else {
                lastExSuccess = exSuccess
                if (isWarmup) {
                    exSuccess = true
                }
                setCurrExercise(this.currWorkout!!.getExerciseFromIndex(this.ex_i))
            }
        } else {
            if (!isWarmup) {
                if (this.currReps != this.currExercise!!.reps) {
                    lockReps = true
                }
                if (this.currWeight != this.currExercise!!.weight) {
                    lockWeight = true
                }
            }
            if (this.currReps >= this.currExercise!!.reps && this.currWeight >= this.currExercise!!.weight) {
                setSuccess = true
            } else {
                setSuccess = false
                exSuccess = false
            }
            setCurrExerciseSet(this.currExercise!!.getSet(this.set_i))
        }
        return true
    }

    val exerciseSuccess: Boolean
        get() = lastExSuccess

    fun resetLocks() {
        lockReps = false
        lockWeight = false
    }

    fun addCurrSet() {
        this.currExerciseSet!!.reps = this.currReps
        this.currExerciseSet!!.weight = this.currWeight
        this.currExercise!!.addSet(this.currExerciseSet!!, true)
    }

    fun atEndOfSets(): Boolean {
        return this.set_i >= this.currExercise!!.getNumSets()
    }

    private fun atEndOfExercises(): Boolean {
        return this.ex_i >= this.currWorkout!!.numExercises
    }

    var exInd: Int
        get() = ex_i
        set(ex_i) {
            this.ex_i = ex_i
        }

    private fun setCurrExercise(exercise: Exercise) {
        if (this.set_i == -1) {
            this.set_i = 0
        }
        this.currExercise = exercise
        dataListener?.updateProgressSets(exercise.getNumSets())
        this.currMinWeight = exercise.minWeight
        this.currWeightChange = exercise.weightChange
        setCurrExerciseSet(this.currExercise!!.getSet(this.set_i))
    }

    private fun setCurrExerciseSet(exerciseSet: ExerciseSet) {
        this.currExerciseSet = exerciseSet
        setCurrReps(this.currExerciseSet!!.reps, true)
        if (!lockWeight) this.currWeight = this.currExerciseSet!!.weight
    }

    fun getIsWarmup(): Boolean = isWarmup

    val warmups: ArrayList<Exercise>?
        get() = currWarmups

    val workoutName: String
        get() = currWorkout!!.name!!

    val currNumExs: Int
        get() = numMains

    private var currMainInd = 0

    val currExNum: Int
        get() {
            if (isWarmup) {
                return currMainInd + 1
            }
            return (currMains!!.indexOf(currExercise) + 1).also { currMainInd = it - 1; /* Match Java side effect logic */ }
        }

    val currExName: String
        get() = currExercise!!.name!!

    val currEquip: String
        get() = currExercise!!.equipment!!

    val currNumSets: Int
        get() = currExercise!!.getNumSets()

    val currSetNum: Int
        get() = set_i + 1

    fun incReps() {
        setCurrReps(++this.currReps, false)
    }

    fun decReps() {
        setCurrReps(--this.currReps, false)
    }

    fun setCurrReps(reps: Int, setTimer: Boolean) {
        if (!lockReps) this.currReps = reps
        if (setTimer) {
            if (this.currExercise!!.setsType == MAIN_SET) {
                setCurrRestTime()
            }
        }
    }

    fun isMinReps(): Boolean {
        return this.currReps == MIN_REPS
    }

    private fun setCurrRestTime() {
        this.currRestTime = if (this.currExerciseSet!!.reps <= 6) HEAVY_REST_TIME else LIGHT_REST_TIME
        setTimer()
    }

    private fun setTimer() {
        if (dataListener != null) {
            dataListener!!.startTimer(this.currRestTime)
            timerSet = true
        } else {
            timerSet = false
        }
    }

    fun incWeight() {
        setWeight(this.currWeight + this.currWeightChange)
    }

    fun decWeight() {
        setWeight(Math.max(this.currMinWeight, this.currWeight - this.currWeightChange))
    }

    fun setWeight(weight: Float) {
        this.currWeight = weight
    }

    fun isMinWeight(): Boolean {
        return this.currWeight <= this.currMinWeight
    }

    val isWarmup: Boolean
        get() = WARMUP_SET == currExercise!!.setsType

    fun resetIndices() {
        this.ex_i = -1
        this.set_i = -1
        this.currMainInd = 0
        this.retrievedWorkout = null
    }

    fun saveSessionState(): String {
        if (!isWarmup) {
            val ex = this.currExercise
            this.currSession!!.addExercise(ex!!)
        }
        enablePrettyMapper()
        val jsonStr = writeValueAsString(
            this.currSession!!.sessionStateMap(
                ex_i,
                set_i
            )
        )
        Logger.d("leave$jsonStr")
        return jsonStr
    }

    fun getSessionExercise(ex_i: Int): Exercise? {
        var exercise: Exercise? = null
        if (ex_i < currExNum) {
            exercise = this.currSession!!.sessionExs[ex_i - 1]
        }
        return exercise
    }

    val currExType: SetsType
        get() = currExercise!!.setsType

    fun unsetTimer() {
        timerSet = false
    }

    companion object {
        private val INST = CurrWorkout()
        private var warmupsListener: WarmupsListener? = null

        @JvmStatic
        fun getInstance(): CurrWorkout {
            return INST
        }
    }
}
