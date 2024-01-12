package ca.judacribz.gainzassist.activities.start_workout

import ca.judacribz.gainzassist.constants.ExerciseConst
import ca.judacribz.gainzassist.models.Exercise
import ca.judacribz.gainzassist.models.Exercise.SetsType
import ca.judacribz.gainzassist.models.ExerciseSet
import ca.judacribz.gainzassist.models.Session
import ca.judacribz.gainzassist.models.Workout
import ca.judacribz.gainzassist.util.Misc
import com.orhanobut.logger.Logger



object CurrWorkout  {// --------------------------------------------------------------------------------------------
// ######################################################################################### //
// CurrWorkout Constructor/Instance                                                        //
// ##################################
    // --------------------------------------------------------------------------------------------
    // Global Vars
    // --------------------------------------------------------------------------------------------
    private var currWorkout: Workout? = null
    private var currExercise: Exercise? = null
    private var currExerciseSet: ExerciseSet? = null
    /* Reps-end--------------------------------------------------------------------------------- */ /*
    Weight----------------------------------------------------------------------------------- */
    var currWeight = 0f
        private set
    var currMinWeight = ExerciseConst.MIN_WEIGHT
        private set
    private var currWeightChange = ExerciseConst.WEIGHT_CHANGE
    private var set_i = -1
    var exInd = -1

    /* Reps------------------------------------------------------------------------------------- */
    var currReps = 0
        private set
    private var numWarmups = 0

    var currNumExs = 0
        private set
    var warmups: ArrayList<Exercise>? = null
        private set
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
    var retrievedWorkout: Map<String, Any>? = null

    @JvmField
    var warmupsListener: WarmupsListener? = null

    // --------------------------------------------------------------------------------------------
    // Interfaces
    // --------------------------------------------------------------------------------------------
    private var dataListener: DataListener? = null

    interface DataListener {
        fun startTimer(timeInMillis: Long)
        fun updateProgressSets(numSets: Int)
    }

    fun setDataListener(dataListener: DataListener?) {
        this.dataListener = dataListener
        //TODO make deterministic
        if (!timerSet) {
            setTimer()
        }
    }

    interface WarmupsListener {
        fun warmupsGenerated(warmups: ArrayList<Exercise>)
    }

    fun setWarmupsListener(warmupsListener: WarmupsListener?) {
        CurrWorkout.warmupsListener = warmupsListener
    }

    // ######################################################################################### //
    fun setRetrievedWorkout(map: Map<String, Any>, workout: Workout) {
        val exsMap: Map<String?, Any?>
        var exMap: Map<String?, Any?>
        var setsMap: Map<String?, Any?>
        var setMap: Map<String?, Any>
        resetIndices()
        retrievedWorkout = map
        warmups = ArrayList()
        currWorkout = workout
        currSession = Session(workout)
        exInd = Integer.valueOf(map[ExerciseConst.EXERCISE_INDEX].toString())
        set_i = Integer.valueOf(map[ExerciseConst.SET_INDEX].toString())
        exsMap = Misc.readValue(Misc.readValue(map[ExerciseConst.SESSION])[ExerciseConst.EXERCISES])
        var exNum = "0"
        var exercise: Exercise? = null
        for ((key, value) in exsMap) {
            exNum = key
            exMap = Misc.readValue(value)

            //TODO test without using map
            exercise = workout.getExerciseFromIndex(Integer.valueOf(exNum))
            exercise?.setsType = SetsType.MAIN_SET
            val succ = exMap["success"]
            setsMap = Misc.readValue(Misc.readValue(exMap)[ExerciseConst.SET_LIST])
            for ((key1, value1) in setsMap) {
                setMap = Misc.readValue(value1)
                exercise?.addSet(
                    ExerciseSet(
                        exercise,
                        Integer.valueOf(key1),
                        Integer.valueOf(setMap[ExerciseConst.REPS].toString()),
                        java.lang.Float.valueOf(setMap[ExerciseConst.WEIGHT].toString())
                    ), false
                )
            }
            exercise?.let { currSession?.addExercise(it) }
        }
        genWarmups(workout.exercises)
        Misc.enablePrettyMapper()
        Logger.d(
            Misc.writeValueAsString(
                currSession!!.sessionStateMap(
                    exInd,
                    set_i
                )
            )
        )
    }

    fun setCurrWorkout(workout: Workout) {
        resetIndices()
        currWorkout = workout
        currSession = Session(workout)
        genWarmups(workout.exercises)
    }

    private fun genWarmups(exercises: ArrayList<Exercise>) {
        val allExs = ArrayList<Exercise>()
        val warmups = ArrayList<Exercise>()
        var exerciseSets: ArrayList<ExerciseSet>
        var warmup: Exercise
        var oneRepMax: Float
        var minWeight: Float
        var weight: Float
        var equip: String
        setCurrMainExercises(exercises)
        for (ex in exercises) {
            equip = ex?.equipment.toString()
            minWeight = ex?.minWeight ?: 0f
            weight = ex?.avgWeight ?: 0f
            if (weight == minWeight) {
                allExs.add(ex)
                continue
            }

            //Todo: use onerepmax
//            oneRepMax = getOneRepMax(ex.getAvgReps(), ex.getAvgWeight());
            exerciseSets = if (ExerciseConst.BARBELL == equip) {
                genBBWarmups(ex)
            } else {
                genWarmups(ex)
            }
            warmup = Exercise(
                ex.exerciseNumber,
                ex.name,
                ex.type,
                ex.equipment,
                exerciseSets,
                SetsType.WARMUP_SET
            )
            warmups.add(warmup)
            allExs.add(warmup)
            allExs.add(ex)
        }
        setCurrWarmupExercises(warmups)
        setAllCurrExercises(allExs)
        currMainInd = allExs[exInd]!!.exerciseNumber
    }

    private fun genBBWarmups(ex: Exercise?): ArrayList<ExerciseSet> {
        val exerciseSets = ArrayList<ExerciseSet>()
        var setNum = 0
        var reps = ex!!.avgReps
        val minWeight = ex.minWeight
        val weightChange = ex.weightChange
        val weight = ex.avgWeight
        var weightInc: Float
        var newWeight = minWeight
        exerciseSets.add(ExerciseSet(ex, setNum++, reps, newWeight))
        exerciseSets.add(ExerciseSet(ex, setNum++, reps, newWeight))
        if (weight >= 405f) {
            weightInc = 90f
            newWeight += 90f
            while (newWeight <= 0.85f * weight) {
                if (newWeight >= 0.75f * weight) {
                    reps = (ex.reps?.div(4) ?: 0) + 1
                } else if (newWeight > 0.65f * weight) {
                    reps = (ex.reps?.div(2) ?: 0) + 1
                }
                exerciseSets.add(ExerciseSet(ex, setNum++, reps, newWeight))
                newWeight += weightInc
            }
        } else {
            newWeight += 50f
            weightInc = 40f
            while (newWeight <= 0.85f * weight) {
                if (newWeight >= 0.75f * weight) {
                    reps = (ex.reps?.div(4) ?: 0) + 1
                } else if (newWeight > 0.65f * weight) {
                    reps = (ex.reps?.div(2) ?: 0) + 1
                }
                exerciseSets.add(ExerciseSet(ex, setNum++, reps, newWeight))
                newWeight += weightInc
            }
        }
        reps = reps / 2 + 1
        do {
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
        } while (true)
    }

    private fun genWarmups(ex: Exercise): ArrayList<ExerciseSet> {
        val exerciseSets = ArrayList<ExerciseSet>()
        var setNum = 0
        var reps = ex!!.avgReps
        val minWeight = ex.minWeight
        val weightChange = ex.weightChange
        val weight = ex.avgWeight
        var newWeight = minWeight
        val diff = weight - minWeight
        if (diff == 0f) {
            return exerciseSets
        }
        var sets = Math.min(5, diff.toInt() / (weightChange * 2).toInt())
        sets += weight.toInt() / 100
        val percInc = 0.91f / sets.toFloat()
        var perc = percInc
        for (i in 0 until sets) {
            newWeight = perc * weight
            newWeight -= newWeight % (weightChange * 2)
            if (newWeight >= 0.65f * weight) {
                reps = reps / 2 + 1
            }
            exerciseSets.add(
                ExerciseSet(
                    ex,
                    setNum++,
                    reps,
                    newWeight
                )
            )
            perc += percInc
        }
        return exerciseSets
    }

    private fun setCurrMainExercises(exercises: ArrayList<Exercise>) {
        currMains = exercises
        currNumExs = exercises.size
    }

    private fun setCurrWarmupExercises(warmups: ArrayList<Exercise>) {
        this.warmups = warmups
        numWarmups = warmups.size
        warmupsListener!!.warmupsGenerated(warmups)
    }

    private fun setAllCurrExercises(allExercises: ArrayList<Exercise>) {
        currWorkout?.exercises = allExercises
        if (exInd == -1) {
            exInd = 0
        }
        currWorkout?.getExerciseFromIndex(exInd)?.let { setCurrExercise(it) }
    }

    @JvmField
    var setSuccess = true

    @JvmField
    var exSuccess = true
    var lastExSuccess = true
    fun finishCurrSet(): Boolean {
        set_i++
        if (!isWarmup) {
            addCurrSet()
        }

        // End of sets for an exercise
        if (atEndOfSets()) {
            resetLocks()
            set_i = 0
            exInd++
            if (!isWarmup) {
                currSession!!.addExercise(currExercise!!)
            }

            // End of all exercises for this workout session
            if (atEndOfExercises()) {
                currSession!!.setTimestamp1(-1)
                resetIndices()
                return false

                // Not end of all exercises, set next exercise
            } else {
                lastExSuccess = exSuccess
                if (isWarmup) {
                    exSuccess = true
                }
                currWorkout!!.getExerciseFromIndex(exInd)?.let { setCurrExercise(it) }
            }

            // End of set, set next set from current exercise
        } else {
            if (!isWarmup) {
                if (currReps != currExercise!!.reps) {
                    lockReps = true
                }
                if (currWeight != currExercise!!.weight) {
                    lockWeight = true
                }
            }
            if (currReps >= currExercise!!.reps!! && currWeight >= currExercise!!.weight!!) {
                setSuccess = true
            } else {
                setSuccess = false
                exSuccess = false
            }
            setCurrExerciseSet(currExercise!!.getSet(set_i))
        }
        return true
    }

    fun getExSuccess(): Boolean {
        return lastExSuccess
    }

    fun resetLocks() {
        lockReps = false
        lockWeight = false
    }

    fun addCurrSet() {
        currExerciseSet!!.reps = currReps
        currExerciseSet!!.weight = currWeight
        currExercise!!.addSet(currExerciseSet!!, true)
    }

    fun atEndOfSets(): Boolean {
        return set_i >= currExercise!!.numSets
    }

    private fun atEndOfExercises(): Boolean {
        return exInd >= currWorkout!!.numExercises
    }

    private fun setCurrExercise(exercise: Exercise) {
        if (set_i == -1) {
            set_i = 0
        }
        currExercise = exercise
        if (dataListener != null) {
            dataListener!!.updateProgressSets(exercise.numSets)
        }
        currMinWeight = exercise.minWeight
        currWeightChange = exercise.weightChange
        setCurrExerciseSet(currExercise!!.getSet(set_i))
    }

    private fun setCurrExerciseSet(exerciseSet: ExerciseSet) {
        currExerciseSet = exerciseSet
        currExerciseSet!!.reps?.let { setCurrReps(it, true) }
        if (!lockWeight) currWeight = currExerciseSet!!.weight!!
    }

    val workoutName: String?
        get() = currWorkout!!.name
    private var currMainInd = 0
    val currExNum: Int
        get() = if (isWarmup) {
            currMainInd + 1
        } else currMains!!.indexOf(currExercise) + 1.also { currMainInd = it }
    val currExName: String?
        get() = currExercise!!.name
    val currEquip: String?
        get() = currExercise!!.equipment
    val currNumSets: Int
        get() = currExercise!!.numSets
    val currSetNum: Int
        get() = set_i + 1

    fun incReps() {
        setCurrReps(++currReps, false)
    }

    fun decReps() {
        setCurrReps(--currReps, false)
    }

    fun setCurrReps(reps: Int, setTimer: Boolean) {
        if (!lockReps) currReps = reps
        if (setTimer) {
            if (currExercise!!.setsType == SetsType.MAIN_SET) {
                setCurrRestTime()
            }
        }
    }

    val isMinReps: Boolean
        get() = currReps == ExerciseConst.MIN_REPS

    private fun setCurrRestTime() {
        currRestTime =
            if (currExerciseSet!!.reps!! <= 6) ExerciseConst.HEAVY_REST_TIME else ExerciseConst.LIGHT_REST_TIME
        setTimer()
    }

    private fun setTimer() {
        timerSet = if (dataListener != null) {
            dataListener!!.startTimer(currRestTime)
            true
        } else {
            false
        }
    }

    fun incWeight() {
        setWeight(currWeight + currWeightChange)
    }

    fun decWeight() {
        setWeight(Math.max(currMinWeight, currWeight - currWeightChange))
    }

    fun setWeight(weight: Float) {
        currWeight = weight
    }

    val isMinWeight: Boolean
        get() = currWeight <= currMinWeight
    val isWarmup: Boolean
        /* Weight-end------------------------------------------------------------------------------- */
        get() = SetsType.WARMUP_SET == currExercise!!.setsType

    fun resetIndices() {
        exInd = -1
        set_i = -1
        currMainInd = 0
        retrievedWorkout = null
    }

    fun saveSessionState(): String {
        if (!isWarmup) {
            val ex = currExercise
            currSession!!.addExercise(ex!!)
        }
        Misc.enablePrettyMapper()
        val jsonStr = Misc.writeValueAsString(
            currSession!!.sessionStateMap(
                exInd,
                set_i
            )
        )
        Logger.d("leave$jsonStr")
        return jsonStr
    }

    fun unsetTimer() {
        timerSet = false
    }

    fun getSessionExercise(ex_i: Int): Exercise? {
        var exercise: Exercise? = null
        if (ex_i < currExNum) {
            exercise = currSession!!.sessionExs[ex_i - 1]
        }
        return exercise
    }

    val currExType: SetsType?
        get() = currExercise!!.setsType
}
