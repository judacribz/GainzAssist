@file:Suppress("kotlin:S107", "kotlin:S109", "kotlin:S1192", "kotlin:S138", "kotlin:S3776", "kotlin:S112", "kotlin:S1874", "DEPRECATION", "HardCodedStringLiteral")
package ca.gainzassist.activities.start_workout.workout_screen.view

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
import androidx.lifecycle.lifecycleScope
import ca.gainzassist.R
import ca.gainzassist.activities.start_workout.WorkoutController
import ca.gainzassist.activities.start_workout.view.StartWorkoutActivity
import ca.gainzassist.activities.start_workout.workout_screen.WorkoutProgressMapper
import ca.gainzassist.activities.start_workout.workout_screen.view.WorkoutProgressUiItem
import ca.gainzassist.activities.start_workout.workout_screen.WorkoutScreenViewModel
import ca.gainzassist.core.constants.ExerciseConst.MIN_REPS
import ca.gainzassist.core.constants.UIConst.PROGRESS_CODE_MAP
import ca.gainzassist.core.constants.UIConst.PROGRESS_STATUS_MAP
import ca.gainzassist.domain.model.Exercise
import ca.gainzassist.domain.model.ExerciseSet
import ca.gainzassist.domain.session.SessionProgressSnapshot
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus.FAIL
import ca.gainzassist.ui.adapters.SingleItemAdapter.ProgressStatus.SUCCESS
import com.orhanobut.logger.Logger
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class WorkoutFragment : Fragment(), WorkoutController.DataListener {

    private val viewModel: WorkoutScreenViewModel by viewModel()

    private val workoutController = WorkoutController
    private var act: StartWorkoutActivity? = null
    private var countDownTimer: CountDownTimer? = null

    private var finExercises = ArrayList<Exercise>()
    private var updateEx: Exercise? = null
    private var currSet: ExerciseSet? = null
    private var updateSetMode = false
    private var currTime: Long = 0
    private var weightVal = 0f

    private var exProgress: SparseArray<ProgressStatus>? = null
    private var setProgress: SparseArray<ProgressStatus>? = null

    private var setNum: String? = null
    private var updateProgress = true
    private var workoutFinished = false

    private var uiState by mutableStateOf(WorkoutUiState())

    override fun onAttach(context: Context) {
        super.onAttach(context)
        act = context as StartWorkoutActivity?
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
        workoutController.setDataListener(this)

        lifecycleScope.launch {
            if (setProgress == null) {
                val snapshot = viewModel.getSessionProgress(workoutController.workoutName)
                if (snapshot != null) {
                    exProgress = SparseArray()
                    for ((key, value) in snapshot.exerciseProgress) {
                        if (value != null) {
                            exProgress?.put(key, PROGRESS_STATUS_MAP[value])
                        }
                    }
                    setProgress = SparseArray()
                    for ((key, value) in snapshot.setProgress) {
                        if (value != null) {
                            setProgress?.put(key, PROGRESS_STATUS_MAP[value])
                        }
                    }
                }
            }

            updateProgressExs(workoutController.currNumExs)
            updateProgSets(workoutController.currNumSets)
            updateUI()
        }
    }

    fun updateProgressExs(numExs: Int) {
        if (exProgress == null) {
            exProgress = setupProgress(numExs, workoutController.currExNum)
        }
        uiState = uiState.copy(exerciseProgress = exProgress?.toProgressUiItems(numExs) ?: emptyList())
    }

    fun updateProgSets(numSets: Int) {
        if (setProgress == null) {
            setProgress = setupProgress(numSets, workoutController.currSetNum)
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
        workoutController.setDataListener(null as WorkoutController.DataListener?)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        countDownTimer = null
    }

    fun saveProgressMap() {
        val exMap = mutableMapOf<Int, Int?>()
        val setMap = mutableMapOf<Int, Int?>()

        exProgress?.let {
            for (i in 0 until it.size) {
                exMap[i] = PROGRESS_CODE_MAP[it[i]]
            }
        }

        setProgress?.let {
            for (i in 0 until it.size) {
                setMap[i] = PROGRESS_CODE_MAP[it[i]]
            }
        }

        val snapshot = SessionProgressSnapshot(exMap, setMap)

        lifecycleScope.launch {
            viewModel.saveSessionProgress(workoutController.workoutName, snapshot)
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
        setProgress = setupProgress(numSets, workoutController.currSetNum)
        uiState = uiState.copy(setProgress = setProgress?.toProgressUiItems(numSets) ?: emptyList())
    }

    private fun setupProgress(
        numItems: Int,
        itemInd: Int
    ): SparseArray<ProgressStatus> {
        return WorkoutProgressMapper.setupProgress(numItems, itemInd)
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
        workoutController.incReps()
        setReps()
    }

    private fun decreaseReps() {
        workoutController.decReps()
        setReps()
    }

    private fun increaseWeight() {
        workoutController.incWeight()
        setWeight()
    }

    private fun decreaseWeight() {
        workoutController.decWeight()
        setWeight()
    }

    private fun onRepsTextChanged(repStr: String) {
        val reps = repStr.toIntOrNull() ?: MIN_REPS
        workoutController.setCurrReps(reps, false)
        uiState = uiState.copy(
            repsText = repStr,
            isMinReps = workoutController.isMinReps()
        )
    }

    private fun onWeightTextChanged(weightStr: String) {
        val w = weightStr.toFloatOrNull() ?: workoutController.currMinWeight
        workoutController.setWeight(w)
        uiState = uiState.copy(
            weightText = weightStr,
            currentWeight = w,
            currentEquipment = workoutController.currEquip,
            isMinWeight = workoutController.isMinWeight() || w <= workoutController.currMinWeight
        )
    }

    private fun onRepsFocusLost() {
        if (uiState.repsText.isEmpty() || uiState.repsText.toIntOrNull() == null) {
            workoutController.setCurrReps(workoutController.currReps, false)
            setReps()
        }
    }

    private fun onWeightFocusLost() {
        if (uiState.weightText.isEmpty() || uiState.weightText.toFloatOrNull() == null) {
            workoutController.setWeight(workoutController.currWeight)
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
            uiState = uiState.copy(setProgress = it.toProgressUiItems(workoutController.currNumSets))
        }

        exProgress?.selectOneBased(workoutController.currExNum)
        uiState = uiState.copy(
            exerciseProgress = exProgress?.toProgressUiItems(workoutController.currNumExs) ?: emptyList(),
            exerciseTitle = workoutController.currExName
        )

        currSet?.let {
            uiState = uiState.copy(
                currentWeight = it.weight,
                currentEquipment = workoutController.currEquip,
                repsText = it.reps.toString(),
                weightText = it.weight.toString()
            )
        }
        updateUI()
        currSet = null
    }

    fun finishSet() {
        if (workoutController.finishCurrSet()) {
            updateUI()
        } else {
            workoutFinished = true

            countDownTimer?.cancel()
            countDownTimer = null

            val a = act
            if (a != null) {
                lifecycleScope.launch {
                    val session = workoutController.currSession
                    if (session != null) {
                        viewModel.insertCompletedSession(session)
                    }
                    viewModel.clearFinishedWorkoutState(workoutController.workoutName)
                    a.finish()
                }
            }
        }
    }

    fun updateUI() {
        val setType = if (workoutController.getIsWarmup()) {
            countDownTimer?.onFinish()
            countDownTimer = null
            "Warmup"
        } else {
            "Main"
        }

        if (!workoutController.lockReps) {
            setReps()
        }
        if (!workoutController.lockWeight) {
            setWeight()
        }

        val title = workoutController.currExName
        val setLabel = setNum?.let { String.format(it, setType) } ?: ""
        
        uiState = uiState.copy(
            exerciseTitle = title,
            setNumText = setLabel
        )

        if (updateProgress) {
            exProgress?.setCurrentOneBased(workoutController.currExNum, workoutController.lastExSuccess)
            setProgress?.setCurrentOneBased(workoutController.currSetNum, workoutController.setSuccess)
            
            uiState = uiState.copy(
                exerciseProgress = exProgress?.toProgressUiItems(workoutController.currNumExs) ?: emptyList(),
                setProgress = setProgress?.toProgressUiItems(workoutController.currNumSets) ?: emptyList()
            )
        } else {
            updateProgress = true
        }
        Logger.d("CURR SET NUM = " + workoutController.currSetNum)
    }

    private fun setReps() {
        uiState = uiState.copy(
            repsText = workoutController.currReps.toString(),
            isMinReps = workoutController.isMinReps()
        )
    }

    fun setWeight() {
        weightVal = workoutController.currWeight
        uiState = uiState.copy(
            weightText = weightVal.toString(),
            currentWeight = weightVal,
            currentEquipment = workoutController.currEquip,
            isMinWeight = workoutController.isMinWeight() || weightVal <= workoutController.currMinWeight
        )
    }

    private fun exerciseItemClick(index: Int) {
        val ind = index + 1
        updateEx = workoutController.getSessionExercise(ind)
        val ex = updateEx
        Logger.d("OHH $ind")
        if (ex != null) {
            val setStatus = SparseArray<ProgressStatus>()
            updateSetMode = true
            
            uiState = uiState.copy(
                isFinishSetVisible = false,
                isUpdateSetVisible = true,
                isResumeWorkoutVisible = true
            )
            
            if (currSet == null) {
                currSet = ExerciseSet(
                    ex,
                    workoutController.currSetNum,
                    workoutController.currReps,
                    workoutController.currWeight
                )
            }
            
            exProgress?.selectOneBased(ind)
            uiState = uiState.copy(
                exerciseProgress = exProgress?.toProgressUiItems(workoutController.currNumExs) ?: emptyList()
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
        if (updateSetMode || (!workoutController.getIsWarmup() && ind < workoutController.currSetNum)) {
            saveProgressMap()
            setProgress?.selectOneBased(ind)
            uiState = uiState.copy(
                setProgress = setProgress?.toProgressUiItems(updateEx?.getNumSets() ?: workoutController.currNumSets) ?: emptyList()
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
        fun getInstance(): WorkoutFragment {
            return WorkoutFragment()
        }
    }
}

private fun SparseArray<ProgressStatus>.selectOneBased(index: Int) {
    WorkoutProgressMapper.selectOneBased(this, index)
}

private fun SparseArray<ProgressStatus>.setCurrentOneBased(index: Int, success: Boolean) {
    WorkoutProgressMapper.setCurrentOneBased(this, index, success)
}

private fun SparseArray<ProgressStatus>.toProgressUiItems(count: Int): List<WorkoutProgressUiItem> {
    return WorkoutProgressMapper.toProgressUiItems(this, count)
}
