package ca.gainzassist.activities.start_workout.fragments

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.util.size
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.gainzassist.R
import ca.gainzassist.activities.start_workout.CurrWorkout
import ca.gainzassist.activities.start_workout.StartWorkout
import ca.gainzassist.adapters.SingleItemAdapter
import ca.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS
import ca.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS.FAIL
import ca.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS.SELECTED
import ca.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS.SUCCESS
import ca.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS.UNSELECTED
import ca.gainzassist.constants.ExerciseConst.MIN_REPS
import ca.gainzassist.constants.UIConst.PROGRESS_CODE_MAP
import ca.gainzassist.constants.UIConst.PROGRESS_STATUS_MAP
import ca.gainzassist.databinding.FragmentWorkoutScreenBinding
import ca.gainzassist.models.Exercise
import ca.gainzassist.models.Exercise.SetsType
import ca.gainzassist.models.ExerciseSet
import ca.gainzassist.models.db.WorkoutViewModel
import ca.gainzassist.util.Misc.readValue
import ca.gainzassist.util.Misc.writeValueAsString
import ca.gainzassist.util.Preferences
import ca.gainzassist.util.UI.getTextInt
import ca.gainzassist.util.UI.handleFocusLeft
import com.orhanobut.logger.Logger
import java.util.Locale

class WorkoutScreen : Fragment(), CurrWorkout.DataListener, SingleItemAdapter.ItemClickObserver {

    private val currWorkout = CurrWorkout.getInstance()
    private var act: StartWorkout? = null
    private var countDownTimer: CountDownTimer? = null

    private var exerciseAdapter: SingleItemAdapter? = null
    private var setAdapter: SingleItemAdapter? = null

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

    private var binding: FragmentWorkoutScreenBinding? = null

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
                WorkoutScreenScreen(
                    onBindingReady = { inflatedBinding ->
                        initializeLegacyBinding(inflatedBinding)
                    }
                )
            }
        }
    }

    private fun initializeLegacyBinding(inflatedBinding: FragmentWorkoutScreenBinding) {
        binding = inflatedBinding

        setNum = "%s " + getString(R.string.set_num)

        setProgressLayoutManagers(inflatedBinding.rvExerciseSet)
        setProgressLayoutManagers(inflatedBinding.rvExerciseNum)

        inflatedBinding.rvExerciseSet.setHasFixedSize(true)
        inflatedBinding.rvExerciseNum.setHasFixedSize(true)

        setupListeners(inflatedBinding)

        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            refreshFromResume()
        }
    }

    private fun setupListeners(binding: FragmentWorkoutScreenBinding) {
        binding.partEtReps.etReps.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (!binding.ibtnDecReps.isEnabled) {
                    binding.ibtnDecReps.isEnabled = true
                    binding.ibtnDecReps.visibility = View.VISIBLE
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val repStr = s.toString()
                val reps = if (repStr.isNotEmpty()) repStr.toInt() else MIN_REPS
                currWorkout.setCurrReps(reps, false)
                if (currWorkout.isMinReps()) {
                    binding.ibtnDecReps.isEnabled = false
                    binding.ibtnDecReps.visibility = View.INVISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.partEtWeight.etWeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (!binding.ibtnDecWeight.isEnabled) {
                    binding.ibtnDecWeight.isEnabled = true
                    binding.ibtnDecWeight.visibility = View.VISIBLE
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val weightVal: Float
                if (s.toString().isNotEmpty()) {
                    weightVal = s.toString().toFloat()
                    binding.equipView.setup(weightVal, currWorkout.currEquip)
                } else {
                    weightVal = currWorkout.currMinWeight
                }
                currWorkout.setWeight(weightVal)
                if (currWorkout.isMinWeight() || weightVal <= currWorkout.currMinWeight) {
                    binding.ibtnDecWeight.isEnabled = false
                    binding.ibtnDecWeight.visibility = View.INVISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        val focusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus && v is EditText) {
                val min: Number
                val res: Number
                if (v.id == R.id.et_weight) {
                    min = currWorkout.currMinWeight
                    res = currWorkout.currWeight
                } else {
                    min = MIN_REPS
                    res = currWorkout.currReps
                }
                handleFocusLeft(v, min, res)
            }
        }
        binding.partEtReps.etReps.onFocusChangeListener = focusChangeListener
        binding.partEtWeight.etWeight.onFocusChangeListener = focusChangeListener

        binding.tvTimer.setOnClickListener { changeTimerState() }
        binding.ibtnIncReps.setOnClickListener { changeReps(it as ImageButton) }
        binding.ibtnDecReps.setOnClickListener { changeReps(it as ImageButton) }
        binding.ibtnIncWeight.setOnClickListener { changeWeight(it as ImageButton) }
        binding.ibtnDecWeight.setOnClickListener { changeWeight(it as ImageButton) }
        binding.btnResumeWorkout.setOnClickListener { resumeWorkout() }
        binding.btnFinishSet.setOnClickListener { finishSet() }
    }

    private fun setProgressLayoutManagers(rv: RecyclerView): LinearLayoutManager {
        val manager = LinearLayoutManager(act, LinearLayoutManager.HORIZONTAL, false)
        rv.layoutManager = manager
        rv.setHasFixedSize(true)
        return manager
    }

    override fun onResume() {
        super.onResume()
        if (binding == null) return
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
        val binding = binding ?: return
        if (exProgress == null) {
            exProgress = setupProgress(numExs, currWorkout.currExNum, null)
        }
        exProgress?.let {
            exerciseAdapter = setupProgressAdapter(binding.rvExerciseNum, numExs, it, false)
            exerciseAdapter?.setItemClickObserver(object : SingleItemAdapter.ItemClickObserver {
                override fun onItemClick(view: View?) {
                    view?.let { v -> exerciseItemClick(v) }
                }

                override fun onItemLongClick(view: View?) {}
            })
        }
    }

    fun updateProgSets(numSets: Int) {
        val binding = binding ?: return
        if (setProgress == null) {
            setProgress = setupProgress(numSets, currWorkout.currSetNum, null)
            updateProgress = true
        } else {
            updateProgress = false
        }
        setProgress?.let {
            setAdapter = setupProgressAdapter(binding.rvExerciseSet, numSets, it, true)
        }
    }

    override fun onPause() {
        super.onPause()
        if (!workoutFinished) {
            saveProgressMap()
        }
        currWorkout.setDataListener(null as CurrWorkout.DataListener?)
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
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
                val binding = binding ?: return
                currTime = millisUntilFinished
                val seconds = currTime / 1000
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                val time =
                    "$minutes:" + String.format(Locale.getDefault(), "%02d", remainingSeconds)
                binding.tvTimer.text = time
            }

            override fun onFinish() {
                val binding = binding ?: return
                binding.tvTimer.setText(R.string.start_next_set)
                cancel()
            }
        }
    }

    override fun updateProgressSets(numSets: Int) {
        val binding = binding ?: return
        setAdapter = setupProgressAdapter(
            binding.rvExerciseSet,
            numSets,
            setupProgress(
                numSets,
                currWorkout.currSetNum,
                currWorkout.currExType
            ).also { setProgress = it },
            true
        )
    }

    private fun setupProgressAdapter(
        rv: RecyclerView,
        numItems: Int,
        progressStatus: SparseArray<PROGRESS_STATUS>,
        setClickListener: Boolean
    ): SingleItemAdapter {
        val adapter = SingleItemAdapter(
            act,
            numItems,
            R.layout.part_text_view_progress,
            R.id.tv_progress,
            progressStatus
        )
        rv.adapter = adapter
        if (setClickListener) {
            adapter.setItemClickObserver(this)
        }
        return adapter
    }

    private fun setupProgress(
        numItems: Int,
        itemInd: Int,
        setType: SetsType?
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

    fun changeReps(repsBtn: ImageButton) {
        when (repsBtn.id) {
            R.id.ibtn_inc_reps -> currWorkout.incReps()
            R.id.ibtn_dec_reps -> currWorkout.decReps()
        }
        setReps()
    }

    fun changeWeight(weightBtn: ImageButton) {
        Logger.d("changeWeight clicked id=" + weightBtn.id)
        Logger.d("before currWeight=" + currWorkout.currWeight)
        when (weightBtn.id) {
            R.id.ibtn_inc_weight -> currWorkout.incWeight()
            R.id.ibtn_dec_weight -> currWorkout.decWeight()
        }
        Logger.d("after currWeight=" + currWorkout.currWeight)
        setWeight()
    }

    fun resumeWorkout() {
        val binding = binding ?: return
        updateSetMode = false
        binding.btnFinishSet.visibility = View.VISIBLE
        binding.btnUpdateSet.visibility = View.INVISIBLE
        binding.btnResumeWorkout.visibility = View.INVISIBLE

        setProgress?.let {
            setAdapter =
                setupProgressAdapter(binding.rvExerciseSet, currWorkout.currNumSets, it, true)
        }
        exerciseAdapter?.setSelected(currWorkout.currExNum)
        binding.tvExerciseTitle.text = currWorkout.currExName
        currSet?.let {
            binding.equipView.setup(it.weight, currWorkout.currEquip)
            binding.partEtReps.etReps.setText(it.reps.toString())
            binding.partEtWeight.etWeight.setText(it.weight.toString())
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
        val binding = binding ?: return
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

        binding.tvExerciseTitle.text = currWorkout.currExName
        setNum?.let {
            binding.tvSetNum.text = String.format(it, setType)
        }

        if (updateProgress) {
            exerciseAdapter?.let {
                selectProgressAdapterPos(
                    it,
                    binding.rvExerciseNum,
                    currWorkout.currExNum,
                    currWorkout.exSuccess
                )
            }
            setAdapter?.let {
                selectProgressAdapterPos(
                    it,
                    binding.rvExerciseSet,
                    currWorkout.currSetNum,
                    currWorkout.setSuccess
                )
            }
        } else {
            updateProgress = true
        }
        Logger.d("CURR SET NUM = " + currWorkout.currSetNum)
    }

    private fun setReps() {
        val binding = binding ?: return
        binding.partEtReps.etReps.setText(currWorkout.currReps.toString())
    }

    fun setWeight() {
        val binding = binding ?: return
        weightVal = currWorkout.currWeight
        binding.partEtWeight.etWeight.setText(weightVal.toString())
        binding.equipView.post { binding.equipView.setup(weightVal, currWorkout.currEquip) }
    }

    private fun selectProgressAdapterPos(
        adapter: SingleItemAdapter,
        rv: RecyclerView,
        pos: Int,
        success: Boolean
    ) {
        adapter.setCurrItem(pos, success)
        rv.scrollToPosition(pos - 1)
    }

    private fun exerciseItemClick(view: View) {
        val binding = binding ?: return
        val ind = getTextInt(view as TextView)
        updateEx = currWorkout.getSessionExercise(ind)
        val ex = updateEx
        Logger.d("OHH $ind")
        if (ex != null) {
            val setStatus = SparseArray<PROGRESS_STATUS>()
            updateSetMode = true
            if (binding.btnFinishSet.isVisible) {
                binding.btnFinishSet.visibility = View.INVISIBLE
                binding.btnUpdateSet.visibility = View.VISIBLE
                binding.btnResumeWorkout.visibility = View.VISIBLE
            }
            if (currSet == null) {
                currSet = ExerciseSet(
                    ex,
                    currWorkout.currSetNum,
                    currWorkout.currReps,
                    currWorkout.currWeight
                )
            }
            exerciseAdapter?.setSelected(ind)
            val setsToUpdate = ex.getFinishedSetsList()
            for (set in setsToUpdate) {
                if (set.reps >= ex.reps && set.weight >= ex.weight) {
                    setStatus.put(set.setNumber, SUCCESS)
                } else {
                    setStatus.put(set.setNumber, FAIL)
                }
            }
            setAdapter =
                setupProgressAdapter(binding.rvExerciseSet, ex.getNumSets(), setStatus, true)
            setAdapter?.setSelected(1)
            updateUI(ex, 0)
        }
    }

    override fun onItemClick(view: View?) {
        binding ?: return
        val ind = getTextInt(view as TextView)
        if (updateSetMode || (!currWorkout.getIsWarmup() && ind < currWorkout.currSetNum)) {
            saveProgressMap()
            setAdapter?.setSelected(ind)
            updateEx?.let { updateUI(it, ind - 1) }
        }
    }

    private fun updateUI(updateEx: Exercise, setInd: Int) {
        val binding = binding ?: return
        val setList = updateEx.getFinishedSetsList()
        if (setInd >= 0 && setInd < setList.size) {
            val set = setList[setInd]
            binding.tvExerciseTitle.text = updateEx.name
            setNum?.let {
                binding.tvSetNum.text = String.format(it, "Main")
            }
            updateEx.equipment?.let {
                binding.equipView.setup(set.weight, it)
            }
            binding.tvTimer.setText(R.string.update_set)
            binding.partEtReps.etReps.setText(set.reps.toString())
            binding.partEtWeight.etWeight.setText(set.weight.toString())
        }
    }

    override fun onItemLongClick(view: View?) {}

    companion object {
        @JvmStatic
        fun getInstance(): WorkoutScreen {
            return WorkoutScreen()
        }
    }
}
