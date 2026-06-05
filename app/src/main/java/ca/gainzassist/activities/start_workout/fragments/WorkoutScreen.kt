package ca.gainzassist.activities.start_workout.fragments

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.util.size
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import ca.gainzassist.R
import ca.gainzassist.activities.start_workout.CurrWorkout
import ca.gainzassist.activities.start_workout.StartWorkout
import ca.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS
import ca.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS.FAIL
import ca.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS.FAIL_SELECTED
import ca.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS.SELECTED
import ca.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS.SUCCESS
import ca.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS.SUCCESS_SELECTED
import ca.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS.UNSELECTED
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

    private var uiState by mutableStateOf(WorkoutUiState())

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
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                WorkoutComposeScreen(
                    uiState = uiState,
                    actions = WorkoutUiActions(
                        onTimerClick = ::changeTimerState,
                        onRepsChanged = ::onRepsTextChanged,
                        onWeightChanged = ::onWeightTextChanged,
                        onRepsFocusLost = ::onRepsFocusLost,
                        onWeightFocusLost = ::onWeightFocusLost,
                        onIncreaseReps = ::increaseReps,
                        onDecreaseReps = ::decreaseReps,
                        onIncreaseWeight = ::increaseWeight,
                        onDecreaseWeight = ::decreaseWeight,
                        onFinishSet = ::finishSet,
                        onResumeWorkout = ::resumeWorkout,
                        onExerciseProgressClick = ::exerciseItemClick,
                        onSetProgressClick = ::setProgressItemClick
                    )
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setNum = "%s " + getString(R.string.set_num)

        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            refreshFromResume()
        }
    }

    override fun onResume() {
        super.onResume()
        if (view == null) return
        refreshFromResume()
    }

    private fun refreshFromResume() {
        currWorkout.setDataListener(this)
        val progressJson = Preferences.getSessionProgressPref(act, currWorkout.workoutName)

        if (progressJson != null && setProgress == null) {
            val map = readValue(progressJson)
            val exMap = readValue(map["exercise progress"])
            val setMap = readValue(map["set progress"])

            exProgress = SparseArray()
            for ((key, value) in exMap) {
                exProgress?.put(
                    key.toInt(),
                    PROGRESS_STATUS_MAP[value.toString().toInt()]
                )
            }
            setProgress = SparseArray()
            for ((key, value) in setMap) {
                setProgress?.put(
                    key.toInt(),
                    PROGRESS_STATUS_MAP[value.toString().toInt()]
                )
            }
        }

        updateProgressExs(currWorkout.currNumExs)
        updateProgSets(currWorkout.currNumSets)
        updateUI()
    }

    fun updateProgressExs(numExs: Int) {
        if (exProgress == null) {
            exProgress = setupProgress(numExs, currWorkout.currExNum)
        }
        uiState = uiState.copy(exerciseProgress = exProgress?.toProgressUiItems(numExs) ?: emptyList())
    }

    fun updateProgSets(numSets: Int) {
        if (setProgress == null) {
            setProgress = setupProgress(numSets, currWorkout.currSetNum)
            updateProgress = true
        } else {
            updateProgress = false
        }
        uiState = uiState.copy(setProgress = setProgress?.toProgressUiItems(numSets) ?: emptyList())
    }

    override fun onPause() {
        super.onPause()
        if (!workoutFinished) {
            saveProgressMap()
        }
        currWorkout.setDataListener(null as CurrWorkout.DataListener?)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        countDownTimer = null
    }

    fun saveProgressMap() {
        val progressMap = HashMap<String, Any>()
        val exMap = HashMap<String, Int?>()
        val setMap = HashMap<String, Int?>()

        exProgress?.let {
            for (i in 0 until it.size) {
                exMap[i.toString()] = PROGRESS_CODE_MAP[it.get(i)]
            }
        }

        setProgress?.let {
            for (i in 0 until it.size) {
                setMap[i.toString()] = PROGRESS_CODE_MAP[it.get(i)]
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
                val time = "$minutes:" + String.format(Locale.getDefault(), "%02d", remainingSeconds)
                uiState = uiState.copy(timerText = time)
            }

            override fun onFinish() {
                if (isAdded) {
                    uiState = uiState.copy(timerText = getString(R.string.start_next_set))
                }
                cancel()
            }
        }
    }

    override fun updateProgressSets(numSets: Int) {
        setProgress = setupProgress(numSets, currWorkout.currSetNum)
        uiState = uiState.copy(setProgress = setProgress?.toProgressUiItems(numSets) ?: emptyList())
    }

    private fun setupProgress(
        numItems: Int,
        itemInd: Int
    ): SparseArray<PROGRESS_STATUS> {
        val progressStatus = SparseArray<PROGRESS_STATUS>()
        for (i in 0 until numItems) {
            progressStatus.put(i, UNSELECTED)
        }
        progressStatus.put(itemInd - 1, SELECTED)
        return progressStatus
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

    private fun increaseReps() {
        currWorkout.incReps()
        setReps()
    }

    private fun decreaseReps() {
        currWorkout.decReps()
        setReps()
    }

    private fun increaseWeight() {
        currWorkout.incWeight()
        setWeight()
    }

    private fun decreaseWeight() {
        currWorkout.decWeight()
        setWeight()
    }

    private fun onRepsTextChanged(repStr: String) {
        val reps = repStr.toIntOrNull() ?: MIN_REPS
        currWorkout.setCurrReps(reps, false)
        uiState = uiState.copy(
            repsText = repStr,
            isMinReps = currWorkout.isMinReps()
        )
    }

    private fun onWeightTextChanged(weightStr: String) {
        val w = weightStr.toFloatOrNull() ?: currWorkout.currMinWeight
        currWorkout.setWeight(w)
        uiState = uiState.copy(
            weightText = weightStr,
            currentWeight = w,
            currentEquipment = currWorkout.currEquip,
            isMinWeight = currWorkout.isMinWeight() || w <= currWorkout.currMinWeight
        )
    }

    private fun onRepsFocusLost() {
        if (uiState.repsText.isEmpty() || uiState.repsText.toIntOrNull() == null) {
            currWorkout.setCurrReps(currWorkout.currReps, false)
            setReps()
        }
    }

    private fun onWeightFocusLost() {
        if (uiState.weightText.isEmpty() || uiState.weightText.toFloatOrNull() == null) {
            currWorkout.setWeight(currWorkout.currWeight)
            setWeight()
        }
    }

    fun resumeWorkout() {
        updateSetMode = false
        uiState = uiState.copy(
            isFinishSetVisible = true,
            isUpdateSetVisible = false,
            isResumeWorkoutVisible = false
        )

        setProgress?.let {
            uiState = uiState.copy(setProgress = it.toProgressUiItems(currWorkout.currNumSets))
        }

        exProgress?.selectOneBased(currWorkout.currExNum)
        uiState = uiState.copy(
            exerciseProgress = exProgress?.toProgressUiItems(currWorkout.currNumExs) ?: emptyList(),
            exerciseTitle = currWorkout.currExName
        )

        currSet?.let {
            uiState = uiState.copy(
                currentWeight = it.weight,
                currentEquipment = currWorkout.currEquip,
                repsText = it.reps.toString(),
                weightText = it.weight.toString()
            )
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

        val title = currWorkout.currExName
        val setLabel = setNum?.let { String.format(it, setType) } ?: ""
        
        uiState = uiState.copy(
            exerciseTitle = title,
            setNumText = setLabel
        )

        if (updateProgress) {
            exProgress?.setCurrentOneBased(currWorkout.currExNum, currWorkout.lastExSuccess)
            setProgress?.setCurrentOneBased(currWorkout.currSetNum, currWorkout.setSuccess)
            
            uiState = uiState.copy(
                exerciseProgress = exProgress?.toProgressUiItems(currWorkout.currNumExs) ?: emptyList(),
                setProgress = setProgress?.toProgressUiItems(currWorkout.currNumSets) ?: emptyList()
            )
        } else {
            updateProgress = true
        }
        Logger.d("CURR SET NUM = " + currWorkout.currSetNum)
    }

    private fun setReps() {
        uiState = uiState.copy(
            repsText = currWorkout.currReps.toString(),
            isMinReps = currWorkout.isMinReps()
        )
    }

    fun setWeight() {
        weightVal = currWorkout.currWeight
        uiState = uiState.copy(
            weightText = weightVal.toString(),
            currentWeight = weightVal,
            currentEquipment = currWorkout.currEquip,
            isMinWeight = currWorkout.isMinWeight() || weightVal <= currWorkout.currMinWeight
        )
    }

    private fun exerciseItemClick(index: Int) {
        val ind = index + 1
        updateEx = currWorkout.getSessionExercise(ind)
        val ex = updateEx
        Logger.d("OHH $ind")
        if (ex != null) {
            val setStatus = SparseArray<PROGRESS_STATUS>()
            updateSetMode = true
            
            uiState = uiState.copy(
                isFinishSetVisible = false,
                isUpdateSetVisible = true,
                isResumeWorkoutVisible = true
            )
            
            if (currSet == null) {
                currSet = ExerciseSet(
                    ex,
                    currWorkout.currSetNum,
                    currWorkout.currReps,
                    currWorkout.currWeight
                )
            }
            
            exProgress?.selectOneBased(ind)
            uiState = uiState.copy(
                exerciseProgress = exProgress?.toProgressUiItems(currWorkout.currNumExs) ?: emptyList()
            )
            
            val setsToUpdate = ex.getFinishedSetsList()
            for (set in setsToUpdate) {
                if (set.reps >= ex.reps && set.weight >= ex.weight) {
                    setStatus.put(set.setNumber, SUCCESS)
                } else {
                    setStatus.put(set.setNumber, FAIL)
                }
            }
            
            setProgress = setStatus
            setProgress?.selectOneBased(1)
            uiState = uiState.copy(
                setProgress = setProgress?.toProgressUiItems(ex.getNumSets()) ?: emptyList()
            )
            
            updateUI(ex, 0)
        }
    }

    private fun setProgressItemClick(index: Int) {
        val ind = index + 1
        if (updateSetMode || (!currWorkout.getIsWarmup() && ind < currWorkout.currSetNum)) {
            saveProgressMap()
            setProgress?.selectOneBased(ind)
            uiState = uiState.copy(
                setProgress = setProgress?.toProgressUiItems(updateEx?.getNumSets() ?: currWorkout.currNumSets) ?: emptyList()
            )
            updateEx?.let { updateUI(it, ind - 1) }
        }
    }

    private fun updateUI(updateEx: Exercise, setInd: Int) {
        val setList = updateEx.getFinishedSetsList()
        if (setInd >= 0 && setInd < setList.size) {
            val set = setList[setInd]
            val setLabel = setNum?.let { String.format(it, "Main") } ?: ""
            
            uiState = uiState.copy(
                exerciseTitle = updateEx.name ?: "",
                setNumText = setLabel,
                currentEquipment = updateEx.equipment.orEmpty(),
                currentWeight = set.weight,
                timerText = getString(R.string.update_set),
                repsText = set.reps.toString(),
                weightText = set.weight.toString()
            )
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(): WorkoutScreen {
            return WorkoutScreen()
        }
    }
}

private fun SparseArray<PROGRESS_STATUS>.deselectCurrent() {
    for (i in 0 until size) {
        val key = keyAt(i)
        when (get(key)) {
            SELECTED -> put(key, UNSELECTED)
            SUCCESS_SELECTED -> put(key, SUCCESS)
            FAIL_SELECTED -> put(key, FAIL)
            else -> Unit
        }
    }
}

private fun SparseArray<PROGRESS_STATUS>.selectOneBased(index: Int) {
    deselectCurrent()
    val zeroBased = index - 1
    val status = get(zeroBased)
    when (status) {
        SUCCESS -> put(zeroBased, SUCCESS_SELECTED)
        FAIL -> put(zeroBased, FAIL_SELECTED)
        else -> put(zeroBased, SELECTED)
    }
}

private fun SparseArray<PROGRESS_STATUS>.setCurrentOneBased(index: Int, success: Boolean) {
    selectOneBased(index)
    if (index > 1) {
        put(index - 2, if (success) SUCCESS else FAIL)
    }
}

private fun SparseArray<PROGRESS_STATUS>.toProgressUiItems(count: Int): List<WorkoutProgressUiItem> {
    return (0 until count).map { zeroBased ->
        WorkoutProgressUiItem(
            number = zeroBased + 1,
            status = get(zeroBased) ?: UNSELECTED
        )
    }
}
