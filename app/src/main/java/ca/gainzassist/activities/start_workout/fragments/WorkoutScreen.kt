package ca.gainzassist.activities.start_workout.fragments

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.util.size
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ca.gainzassist.R
import ca.gainzassist.activities.start_workout.CurrWorkout
import ca.gainzassist.activities.start_workout.StartWorkout
import ca.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS
import ca.gainzassist.constants.ExerciseConst.MIN_REPS
import ca.gainzassist.constants.UIConst.PROGRESS_CODE_MAP
import ca.gainzassist.constants.UIConst.PROGRESS_STATUS_MAP
import ca.gainzassist.models.Exercise
import ca.gainzassist.models.ExerciseSet
import ca.gainzassist.models.db.WorkoutViewModel
import ca.gainzassist.util.Misc.readValue
import ca.gainzassist.util.Misc.writeValueAsString
import ca.gainzassist.util.Preferences
import com.orhanobut.logger.Logger
import java.util.Locale

class WorkoutScreen : Fragment(), CurrWorkout.DataListener {

    private val currWorkout = CurrWorkout.getInstance()
    private var act: StartWorkout? = null
    private var countDownTimer: CountDownTimer? = null
    private var finExercises = ArrayList<Exercise>()
    private var updateEx: Exercise? = null
    private var currSet: ExerciseSet? = null
    private var updateSetMode = false
    private var currTime: Long = 0
    private var weightVal = 0f
    private var exProgress: SparseArray<PROGRESS_STATUS>? = null
    private var setProgress: SparseArray<PROGRESS_STATUS>? = null
    private var setNum: String? = null
    private var updateProgress = true
    private var workoutFinished = false

    // Compose State values
    private val exerciseTitle = mutableStateOf("")
    private val setNumText = mutableStateOf("")
    private val exerciseProgressList = mutableStateOf<List<PROGRESS_STATUS>>(emptyList())
    private val setProgressList = mutableStateOf<List<PROGRESS_STATUS>>(emptyList())
    private val timerText = mutableStateOf("")
    private val repsText = mutableStateOf("")
    private val weightText = mutableStateOf("")
    private val equipmentWeight = mutableFloatStateOf(0f)
    private val equipmentType = mutableStateOf("")
    private val isUpdateMode = mutableStateOf(false)
    private val isDecRepsVisible = mutableStateOf(true)
    private val isDecWeightVisible = mutableStateOf(true)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        act = context as StartWorkout?
        finExercises = ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setNum = "%s " + getString(R.string.set_num)
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                WorkoutScreenComposeContent(
                    exerciseTitle = exerciseTitle.value,
                    setNumText = setNumText.value,
                    exerciseProgress = exerciseProgressList.value,
                    setProgress = setProgressList.value,
                    timerText = timerText.value,
                    repsText = repsText.value,
                    weightText = weightText.value,
                    equipmentWeight = equipmentWeight.floatValue,
                    equipmentType = equipmentType.value,
                    isUpdateMode = isUpdateMode.value,
                    isDecRepsVisible = isDecRepsVisible.value,
                    isDecWeightVisible = isDecWeightVisible.value,
                    onExerciseDotClick = { index -> onExerciseDotClick(index) },
                    onSetDotClick = { index -> onSetDotClick(index) },
                    onTimerClick = { changeTimerState() },
                    onRepsChanged = { newVal -> handleRepsChanged(newVal) },
                    onWeightChanged = { newVal -> handleWeightChanged(newVal) },
                    onIncReps = { changeRepsCompose(inc = true) },
                    onDecReps = { changeRepsCompose(inc = false) },
                    onIncWeight = { changeWeightCompose(inc = true) },
                    onDecWeight = { changeWeightCompose(inc = false) },
                    onFinishSet = { finishSet() },
                    onResumeWorkout = { resumeWorkout() },
                    onUpdateSet = { /* Click is not wired in legacy screen */ }
                )
            }
        }
    }

    private fun getProgressList(sparseArray: SparseArray<PROGRESS_STATUS>?, size: Int): List<PROGRESS_STATUS> {
        if (sparseArray == null) return emptyList()
        val list = ArrayList<PROGRESS_STATUS>()
        for (i in 0 until size) {
            list.add(sparseArray.get(i) ?: PROGRESS_STATUS.UNSELECTED)
        }
        return list
    }

    private fun handleRepsChanged(newVal: String) {
        repsText.value = newVal
        val reps = if (newVal.isNotEmpty()) newVal.toIntOrNull() ?: MIN_REPS else MIN_REPS
        currWorkout.setCurrReps(reps, false)
        isDecRepsVisible.value = !currWorkout.isMinReps()
    }

    private fun handleWeightChanged(newVal: String) {
        weightText.value = newVal
        val weight = if (newVal.isNotEmpty()) newVal.toFloatOrNull() ?: currWorkout.currMinWeight else currWorkout.currMinWeight
        currWorkout.setWeight(weight)
        equipmentWeight.floatValue = weight
        equipmentType.value = currWorkout.currEquip
        isDecWeightVisible.value = !(currWorkout.isMinWeight() || weight <= currWorkout.currMinWeight)
    }

    private fun changeRepsCompose(inc: Boolean) {
        if (inc) {
            currWorkout.incReps()
        } else {
            currWorkout.decReps()
        }
        setReps()
    }

    private fun changeWeightCompose(inc: Boolean) {
        if (inc) {
            currWorkout.incWeight()
        } else {
            currWorkout.decWeight()
        }
        setWeight()
    }

    fun updateProgressExs(numExs: Int) {
        if (exProgress == null) {
            exProgress = setupProgress(numExs, currWorkout.currExNum)
        }
        exProgress?.let {
            exerciseProgressList.value = getProgressList(it, numExs)
        }
    }

    fun updateProgSets(numSets: Int) {
        if (setProgress == null) {
            setProgress = setupProgress(numSets, currWorkout.currSetNum)
            updateProgress = true
        } else {
            updateProgress = false
        }
        setProgress?.let {
            setProgressList.value = getProgressList(it, numSets)
        }
    }

    override fun updateProgressSets(numSets: Int) {
        setProgress = setupProgress(numSets, currWorkout.currSetNum)
        setProgress?.let {
            setProgressList.value = getProgressList(it, numSets)
        }
    }

    private fun setupProgress(
        numItems: Int,
        itemInd: Int
    ): SparseArray<PROGRESS_STATUS> {
        val progressStatus = SparseArray<PROGRESS_STATUS>()
        for (i in 0 until numItems) {
            progressStatus.put(i, PROGRESS_STATUS.UNSELECTED)
        }
        progressStatus.put(itemInd - 1, PROGRESS_STATUS.SELECTED)
        return progressStatus
    }

    private fun setSelectedProgress(progStatus: SparseArray<PROGRESS_STATUS>?, currSetNum: Int) {
        if (progStatus == null) return
        for (i in 0 until progStatus.size) {
            val status = progStatus.valueAt(i)
            if (status != null) {
                val key = progStatus.keyAt(i)
                when (status) {
                    PROGRESS_STATUS.SELECTED -> progStatus.put(key, PROGRESS_STATUS.UNSELECTED)
                    PROGRESS_STATUS.SUCCESS_SELECTED -> progStatus.put(key, PROGRESS_STATUS.SUCCESS)
                    PROGRESS_STATUS.FAIL_SELECTED -> progStatus.put(key, PROGRESS_STATUS.FAIL)
                    else -> {}
                }
            }
        }

        val newIndex = currSetNum - 1
        val status = progStatus.get(newIndex)
        if (status != null) {
            when (status) {
                PROGRESS_STATUS.SUCCESS -> progStatus.put(newIndex, PROGRESS_STATUS.SUCCESS_SELECTED)
                PROGRESS_STATUS.FAIL -> progStatus.put(newIndex, PROGRESS_STATUS.FAIL_SELECTED)
                else -> progStatus.put(newIndex, PROGRESS_STATUS.SELECTED)
            }
        }
    }

    private fun setCurrItemProgress(progStatus: SparseArray<PROGRESS_STATUS>?, pos: Int, success: Boolean) {
        if (progStatus == null) return
        setSelectedProgress(progStatus, pos)
        if (pos > 1) {
            val prevIndex = pos - 2
            val currentStatus = progStatus.get(prevIndex)
            if (currentStatus != PROGRESS_STATUS.SUCCESS && currentStatus != PROGRESS_STATUS.FAIL) {
                if (success) {
                    progStatus.put(prevIndex, PROGRESS_STATUS.SUCCESS)
                } else {
                    progStatus.put(prevIndex, PROGRESS_STATUS.FAIL)
                }
            }
        }
    }

    override fun startTimer(timeInMillis: Long) {
        countDownTimer?.cancel()
        countDownTimer = getCountDownTimer(timeInMillis)
        countDownTimer?.start()
    }

    private fun getCountDownTimer(milliseconds: Long): CountDownTimer {
        return object : CountDownTimer(milliseconds, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                currTime = millisUntilFinished
                val seconds = currTime / 1000
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                val time =
                    "$minutes:" + String.format(Locale.getDefault(), "%02d", remainingSeconds)
                timerText.value = time
            }

            override fun onFinish() {
                timerText.value = getString(R.string.start_next_set)
                cancel()
            }
        }
    }

    fun changeTimerState() {
        if (currTime / 1000 != 0L) {
            if (countDownTimer == null) {
                startTimer(currTime)
            } else {
                countDownTimer?.cancel()
                countDownTimer = null
            }
        }
    }

    fun resumeWorkout() {
        updateSetMode = false
        isUpdateMode.value = false
        setProgress?.let {
            setProgressList.value = getProgressList(it, currWorkout.currNumSets)
        }
        exProgress?.let {
            setSelectedProgress(it, currWorkout.currExNum)
            exerciseProgressList.value = getProgressList(it, currWorkout.currNumExs)
        }
        exerciseTitle.value = currWorkout.currExName
        currSet?.let {
            equipmentWeight.floatValue = it.weight
            equipmentType.value = currWorkout.currEquip
            repsText.value = it.reps.toString()
            weightText.value = it.weight.toString()
        }
        updateUI()
        currSet = null
    }

    fun finishSet() {
        if (currWorkout.finishCurrSet()) {
            updateUI()
        } else {
            workoutFinished = true
            countDownTimer?.cancel()
            countDownTimer = null
            val a = act
            if (a != null) {
                val session = currWorkout.currSession
                if (session != null) {
                    ViewModelProvider(a)[WorkoutViewModel::class.java]
                        .insertSession(session)
                }
                if (Preferences.removeIncompleteWorkoutPref(a, currWorkout.workoutName)) {
                    Preferences.removeIncompleteSessionPref(a, currWorkout.workoutName)
                }
                Preferences.removeSessionProgressPref(a, currWorkout.workoutName)
                a.finish()
            }
        }
    }

    fun updateUI() {
        val setType = if (currWorkout.getIsWarmup()) {
            countDownTimer?.onFinish()
            countDownTimer = null
            "Warmup"
        } else {
            "Main"
        }
        if (!currWorkout.lockReps) {
            setReps()
        }
        if (!currWorkout.lockWeight) {
            setWeight()
        }
        exerciseTitle.value = currWorkout.currExName
        setNum?.let {
            setNumText.value = String.format(it, setType)
        }
        if (updateProgress) {
            exProgress?.let {
                setCurrItemProgress(it, currWorkout.currExNum, currWorkout.exSuccess)
                exerciseProgressList.value = getProgressList(it, currWorkout.currNumExs)
            }
            setProgress?.let {
                setCurrItemProgress(it, currWorkout.currSetNum, currWorkout.setSuccess)
                setProgressList.value = getProgressList(it, currWorkout.currNumSets)
            }
        } else {
            updateProgress = true
        }
        Logger.d("CURR SET NUM = " + currWorkout.currSetNum)
    }

    private fun setReps() {
        repsText.value = currWorkout.currReps.toString()
        isDecRepsVisible.value = !currWorkout.isMinReps()
    }

    fun setWeight() {
        weightVal = currWorkout.currWeight
        weightText.value = weightVal.toString()
        equipmentWeight.floatValue = weightVal
        equipmentType.value = currWorkout.currEquip
        isDecWeightVisible.value = !(currWorkout.isMinWeight() || weightVal <= currWorkout.currMinWeight)
    }

    private fun onExerciseDotClick(index: Int) {
        val ind = index + 1
        updateEx = currWorkout.getSessionExercise(ind)
        val ex = updateEx
        Logger.d("OHH $ind")
        if (ex != null) {
            val setStatus = SparseArray<PROGRESS_STATUS>()
            updateSetMode = true
            isUpdateMode.value = true
            if (currSet == null) {
                currSet = ExerciseSet(
                    ex,
                    currWorkout.currSetNum,
                    currWorkout.currReps,
                    currWorkout.currWeight
                )
            }
            exProgress?.let {
                setSelectedProgress(it, ind)
                exerciseProgressList.value = getProgressList(it, currWorkout.currNumExs)
            }
            val setsToUpdate = ex.getFinishedSetsList()
            for (set in setsToUpdate) {
                if (set.reps >= ex.reps && set.weight >= ex.weight) {
                    setStatus.put(set.setNumber, PROGRESS_STATUS.SUCCESS)
                } else {
                    setStatus.put(set.setNumber, PROGRESS_STATUS.FAIL)
                }
            }
            val numSets = ex.getNumSets()
            val fullSetStatus = setupProgress(numSets, 1)
            for (i in 0 until numSets) {
                val status = setStatus.get(i)
                if (status != null) {
                    fullSetStatus.put(i, status)
                }
            }
            setSelectedProgress(fullSetStatus, 1)
            setProgress = fullSetStatus
            setProgressList.value = getProgressList(fullSetStatus, numSets)
            updateUI(ex, 0)
        }
    }

    private fun onSetDotClick(index: Int) {
        val ind = index + 1
        if (updateSetMode || (!currWorkout.getIsWarmup() && ind < currWorkout.currSetNum)) {
            saveProgressMap()
            setProgress?.let {
                setSelectedProgress(it, ind)
                setProgressList.value = getProgressList(it, currWorkout.currNumSets)
            }
            updateEx?.let { updateUI(it, ind - 1) }
        }
    }

    private fun updateUI(updateEx: Exercise, setInd: Int) {
        val setList = updateEx.getFinishedSetsList()
        if (setInd >= 0 && setInd < setList.size) {
            val set = setList[setInd]
            exerciseTitle.value = updateEx.name ?: ""
            setNum?.let {
                setNumText.value = String.format(it, "Main")
            }
            updateEx.equipment?.let {
                equipmentWeight.floatValue = set.weight
                equipmentType.value = it
            }
            timerText.value = getString(R.string.update_set)
            repsText.value = set.reps.toString()
            weightText.value = set.weight.toString()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshFromResume()
    }

    private fun refreshFromResume() {
        currWorkout.setDataListener(this)
        val progressJson = Preferences.getSessionProgressPref(act, currWorkout.workoutName)
        if (progressJson != null && setProgress == null) {
            val map = readValue(progressJson)
            val exMap = readValue(map["exercise progress"])
            val setMap = readValue(map["set progress"])
            val newExProgress = SparseArray<PROGRESS_STATUS>()
            for ((key, value) in exMap) {
                newExProgress.put(
                    key.toInt(),
                    PROGRESS_STATUS_MAP[value.toString().toInt()]
                )
            }
            exProgress = newExProgress

            val newSetProgress = SparseArray<PROGRESS_STATUS>()
            for ((key, value) in setMap) {
                newSetProgress.put(
                    key.toInt(),
                    PROGRESS_STATUS_MAP[value.toString().toInt()]
                )
            }
            setProgress = newSetProgress
        }
        updateProgressExs(currWorkout.currNumExs)
        updateProgSets(currWorkout.currNumSets)
        updateUI()
    }

    override fun onPause() {
        super.onPause()
        if (!workoutFinished) {
            saveProgressMap()
        }
        currWorkout.setDataListener(null as CurrWorkout.DataListener?)
    }

    fun saveProgressMap() {
        val progressMap = HashMap<String, Any>()
        val exMap = HashMap<String, Int?>()
        val setMap = HashMap<String, Int?>()
        exProgress?.let {
            for (i in 0 until it.size) {
                val key = it.keyAt(i)
                exMap[key.toString()] = PROGRESS_CODE_MAP[it.get(key)]
            }
        }
        setProgress?.let {
            for (i in 0 until it.size) {
                val key = it.keyAt(i)
                setMap[key.toString()] = PROGRESS_CODE_MAP[it.get(key)]
            }
        }
        progressMap["exercise progress"] = exMap
        progressMap["set progress"] = setMap
        Preferences.addSessionProgressPref(
            act,
            currWorkout.workoutName,
            writeValueAsString(progressMap)
        )
    }

    companion object {
        @JvmStatic
        fun getInstance(): WorkoutScreen {
            return WorkoutScreen()
        }
    }
}