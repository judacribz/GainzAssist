package ca.judacribz.gainzassist.activities.start_workout.fragments

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnFocusChange
import butterknife.OnTextChanged
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.start_workout.CurrWorkout
import ca.judacribz.gainzassist.activities.start_workout.EquipmentView
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout
import ca.judacribz.gainzassist.adapters.SingleItemAdapter
import ca.judacribz.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS
import ca.judacribz.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS.*
import ca.judacribz.gainzassist.constants.ExerciseConst.MIN_REPS
import ca.judacribz.gainzassist.constants.ExerciseConst.SESSION
import ca.judacribz.gainzassist.constants.ExerciseConst.EXERCISES
import ca.judacribz.gainzassist.constants.ExerciseConst.SET_LIST
import ca.judacribz.gainzassist.constants.ExerciseConst.REPS
import ca.judacribz.gainzassist.constants.ExerciseConst.WEIGHT
import ca.judacribz.gainzassist.constants.UIConst.PROGRESS_CODE_MAP
import ca.judacribz.gainzassist.constants.UIConst.PROGRESS_STATUS_MAP
import ca.judacribz.gainzassist.models.Exercise
import ca.judacribz.gainzassist.models.Exercise.SetsType
import ca.judacribz.gainzassist.models.ExerciseSet
import ca.judacribz.gainzassist.models.db.WorkoutViewModel
import ca.judacribz.gainzassist.util.Misc.readValue
import ca.judacribz.gainzassist.util.Misc.writeValueAsString
import ca.judacribz.gainzassist.util.Preferences.*
import ca.judacribz.gainzassist.util.UI.getTextInt
import ca.judacribz.gainzassist.util.UI.handleFocusLeft
import com.orhanobut.logger.Logger
import java.util.*

class WorkoutScreen : Fragment(), CurrWorkout.DataListener, SingleItemAdapter.ItemClickObserver {

    private val currWorkout = CurrWorkout.getInstance()
    private var act: StartWorkout? = null
    private var countDownTimer: CountDownTimer? = null

    private var exerciseAdapter: SingleItemAdapter? = null
    private var setAdapter: SingleItemAdapter? = null
    private var setManager: LinearLayoutManager? = null
    private var exerciseManager: LinearLayoutManager? = null

    private var finExercises = ArrayList<Exercise>()
    private var updateEx: Exercise? = null
    private var currSet: ExerciseSet? = null
    private var updateSetMode = false
    private var currTime: Long = 0
    private var weight = 0f

    private var exProgress: SparseArray<PROGRESS_STATUS>? = null
    private var setProgress: SparseArray<PROGRESS_STATUS>? = null

    private var setNum: String? = null
    private var updateProgress = true
    private var workoutFinished = false

    @BindView(R.id.equip_view)
    lateinit var equipmentView: EquipmentView

    @BindView(R.id.tv_timer)
    lateinit var tvTimer: TextView

    @BindView(R.id.tv_exercise_title)
    lateinit var tvExerciseTitle: TextView

    @BindView(R.id.tv_set_num)
    lateinit var tvSetNum: TextView

    @BindView(R.id.ibtn_dec_reps)
    lateinit var btnDecReps: ImageButton

    @BindView(R.id.ibtn_dec_weight)
    lateinit var btnDecWeight: ImageButton

    @BindView(R.id.btn_finish_set)
    lateinit var btnFinishSet: Button

    @BindView(R.id.btn_update_set)
    lateinit var btnUpdateSet: Button

    @BindView(R.id.btn_resume_workout)
    lateinit var btnResumeWorkout: Button

    @BindView(R.id.et_reps)
    lateinit var etCurrReps: EditText

    @BindView(R.id.et_weight)
    lateinit var etCurrWeight: EditText

    @BindView(R.id.rv_exercise_num)
    lateinit var rvExercise: RecyclerView

    @BindView(R.id.rv_exercise_set)
    lateinit var rvSet: RecyclerView

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        act = context as StartWorkout?
        finExercises = ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_workout_screen, container, false)
        ButterKnife.bind(this, view)

        setNum = "%s " + getString(R.string.set_num)
        setManager = setProgressLayoutManagers(rvSet)
        exerciseManager = setProgressLayoutManagers(rvExercise)

        rvSet.setHasFixedSize(true)
        rvExercise.setHasFixedSize(true)

        return view
    }

    private fun setProgressLayoutManagers(rv: RecyclerView): LinearLayoutManager {
        val manager = LinearLayoutManager(act, LinearLayoutManager.HORIZONTAL, false)
        rv.layoutManager = manager
        rv.setHasFixedSize(true)
        return manager
    }

    override fun onResume() {
        super.onResume()
        currWorkout.setDataListener(this)
        val progressJson = getSessionProgressPref(act, currWorkout.workoutName)

        if (progressJson != null && setProgress == null) {
            val map = readValue(progressJson)
            val exMap = readValue(map["exercise progress"])
            val setMap = readValue(map["set progress"])

            exProgress = SparseArray()
            for ((key, value) in exMap) {
                exProgress!!.put(
                    key.toInt(),
                    PROGRESS_STATUS_MAP[value.toString().toInt()]
                )
            }
            setProgress = SparseArray()
            for ((key, value) in setMap) {
                setProgress!!.put(
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
            exProgress = setupProgress(numExs, currWorkout.currExNum, null)
        }
        exerciseAdapter = setupProgressAdapter(rvExercise, numExs, exProgress!!, false)
        exerciseAdapter!!.setItemClickObserver(object : SingleItemAdapter.ItemClickObserver {
            override fun onItemClick(view: View?) {
                exerciseItemClick(view!!)
            }

            override fun onItemLongClick(view: View?) {}
        })
    }

    fun updateProgSets(numSets: Int) {
        if (setProgress == null) {
            setProgress = setupProgress(numSets, currWorkout.currSetNum, null)
            updateProgress = true
        } else {
            updateProgress = false
        }
        setAdapter = setupProgressAdapter(rvSet, numSets, setProgress!!, true)
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

        for (i in 0 until exProgress!!.size()) {
            exMap[i.toString()] = PROGRESS_CODE_MAP[exProgress!!.get(i)]
        }

        for (i in 0 until setProgress!!.size()) {
            setMap[i.toString()] = PROGRESS_CODE_MAP[setProgress!!.get(i)]
        }

        progressMap["exercise progress"] = exMap
        progressMap["set progress"] = setMap
        addSessionProgressPref(
            act,
            currWorkout.workoutName,
            writeValueAsString(progressMap)
        )
    }

    override fun startTimer(timeInMillis: Long) {
        if (countDownTimer != null) {
            countDownTimer!!.cancel()
        }
        countDownTimer = getCountDownTimer(timeInMillis)
        countDownTimer!!.start()
    }

    private fun getCountDownTimer(milliseconds: Long): CountDownTimer {
        return object : CountDownTimer(milliseconds, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                currTime = millisUntilFinished
                val seconds = currTime / 1000
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                val time = "$minutes:" + String.format(Locale.getDefault(), "%02d", remainingSeconds)
                tvTimer.text = time
            }

            override fun onFinish() {
                tvTimer.setText(R.string.start_next_set)
                cancel()
            }
        }
    }

    override fun updateProgressSets(numSets: Int) {
        setAdapter = setupProgressAdapter(
            rvSet,
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
        val adapter = SingleItemAdapter(act, numItems, R.layout.part_text_view_progress, R.id.tv_progress, progressStatus)
        rv.adapter = adapter
        if (setClickListener) {
            adapter.setItemClickObserver(this)
        }
        return adapter
    }

    private fun setupProgress(numItems: Int, itemInd: Int, setType: SetsType?): SparseArray<PROGRESS_STATUS> {
        val progressStatus = SparseArray<PROGRESS_STATUS>()
        for (i in 0 until numItems) {
            progressStatus.put(i, UNSELECTED)
        }
        progressStatus.put(itemInd - 1, SELECTED)
        return progressStatus
    }

    @OnTextChanged(value = [R.id.et_reps], callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    fun beforeRepsChanged() {
        if (!btnDecReps.isEnabled) {
            btnDecReps.isEnabled = true
            btnDecReps.visibility = View.VISIBLE
        }
    }

    @OnTextChanged(value = [R.id.et_reps], callback = OnTextChanged.Callback.TEXT_CHANGED)
    fun onRepsChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        val repStr = s.toString()
        val reps = if (repStr.isNotEmpty()) repStr.toInt() else MIN_REPS
        currWorkout.setCurrReps(reps, false)
        if (currWorkout.isMinReps()) {
            btnDecReps.isEnabled = false
            btnDecReps.visibility = View.INVISIBLE
        }
    }

    @OnTextChanged(value = [R.id.et_weight], callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    fun beforeWeightChanged() {
        if (!btnDecWeight.isEnabled) {
            btnDecWeight.isEnabled = true
            btnDecWeight.visibility = View.VISIBLE
        }
    }

    @OnTextChanged(value = [R.id.et_weight], callback = OnTextChanged.Callback.TEXT_CHANGED)
    fun onWeightChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        val weightVal: Float
        if (s.toString().isNotEmpty()) {
            weightVal = s.toString().toFloat()
            equipmentView.setup(weightVal, currWorkout.currEquip)
        } else {
            weightVal = currWorkout.currMinWeight
        }
        currWorkout.setWeight(weightVal)
        if (currWorkout.isMinWeight() || weightVal <= currWorkout.currMinWeight) {
            btnDecWeight.isEnabled = false
            btnDecWeight.visibility = View.INVISIBLE
        }
    }

    @OnFocusChange(value = [R.id.et_reps, R.id.et_weight])
    fun onFocusLeft(et: EditText, hasFocus: Boolean) {
        if (!hasFocus) {
            val min: Number
            val res: Number
            if (et.id == R.id.et_weight) {
                min = currWorkout.currMinWeight
                res = currWorkout.currWeight
            } else {
                min = MIN_REPS
                res = currWorkout.currReps
            }
            handleFocusLeft(et, min, res)
        }
    }

    @OnClick(R.id.tv_timer)
    fun changeTimerState() {
        if (currTime / 1000 != 0L) {
            if (countDownTimer == null) {
                startTimer(currTime)
            } else {
                countDownTimer!!.cancel()
                countDownTimer = null
            }
        }
    }

    @OnClick(value = [R.id.ibtn_inc_reps, R.id.ibtn_dec_reps])
    fun changeReps(repsBtn: ImageButton) {
        when (repsBtn.id) {
            R.id.ibtn_inc_reps -> currWorkout.incReps()
            R.id.ibtn_dec_reps -> currWorkout.decReps()
        }
        setReps()
    }

    @OnClick(value = [R.id.ibtn_inc_weight, R.id.ibtn_dec_weight])
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

    @OnClick(R.id.btn_resume_workout)
    fun resumeWorkout() {
        updateSetMode = false
        btnFinishSet.visibility = View.VISIBLE
        btnUpdateSet.visibility = View.INVISIBLE
        btnResumeWorkout.visibility = View.INVISIBLE

        setAdapter = setupProgressAdapter(rvSet, currWorkout.currNumSets, setProgress!!, true)
        exerciseAdapter!!.setSelected(currWorkout.currExNum)
        tvExerciseTitle.text = currWorkout.currExName
        equipmentView.setup(currSet!!.weight, currWorkout.currEquip)
        etCurrReps.setText(currSet!!.reps.toString())
        etCurrWeight.setText(currSet!!.weight.toString())
        updateUI()
        currSet = null
    }

    @OnClick(R.id.btn_finish_set)
    fun finishSet() {
        if (currWorkout.finishCurrSet()) {
            updateUI()
        } else {
            workoutFinished = true
            val a = act
            if (a != null) {
                ViewModelProviders.of(a).get(WorkoutViewModel::class.java).insertSession(currWorkout.currSession!!)
                if (removeIncompleteWorkoutPref(a, currWorkout.workoutName)) {
                    removeIncompleteSessionPref(a, currWorkout.workoutName)
                }
                removeSessionProgressPref(a, currWorkout.workoutName)
                a.finish()
            }
        }
    }

    fun updateUI() {
        val setType = if (currWorkout.isWarmup) {
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

        tvExerciseTitle.text = currWorkout.currExName
        tvSetNum.text = String.format(setNum!!, setType)

        if (updateProgress) {
            selectProgressAdapterPos(exerciseAdapter!!, rvExercise, currWorkout.currExNum, currWorkout.exSuccess)
            selectProgressAdapterPos(setAdapter!!, rvSet, currWorkout.currSetNum, currWorkout.setSuccess)
        } else {
            updateProgress = true
        }
        Logger.d("CURR SET NUM = " + currWorkout.currSetNum)
    }

    private fun setReps() {
        etCurrReps.setText(currWorkout.currReps.toString())
    }

    fun setWeight() {
        weight = currWorkout.currWeight
        etCurrWeight.setText(weight.toString())
        equipmentView.post { equipmentView.setup(weight, currWorkout.currEquip) }
    }

    private fun selectProgressAdapterPos(adapter: SingleItemAdapter, rv: RecyclerView, pos: Int, success: Boolean) {
        adapter.setCurrItem(pos, success)
        rv.scrollToPosition(pos - 1)
    }

    private fun exerciseItemClick(view: View) {
        val ind = getTextInt(view as TextView)
        updateEx = currWorkout.getSessionExercise(ind)
        val ex = updateEx
        Logger.d("OHH $ind")
        if (ex != null) {
            val setStatus = SparseArray<PROGRESS_STATUS>()
            updateSetMode = true
            if (btnFinishSet.visibility == View.VISIBLE) {
                btnFinishSet.visibility = View.INVISIBLE
                btnUpdateSet.visibility = View.VISIBLE
                btnResumeWorkout.visibility = View.VISIBLE
            }
            if (currSet == null) {
                currSet = ExerciseSet(ex, currWorkout.currSetNum, currWorkout.currReps, currWorkout.currWeight)
            }
            exerciseAdapter!!.setSelected(ind)
            val setsToUpdate = ex.finishedSetsList
            for (set in setsToUpdate) {
                if (set.reps >= ex.reps && set.weight >= ex.weight) {
                    setStatus.put(set.setNumber, SUCCESS)
                } else {
                    setStatus.put(set.setNumber, FAIL)
                }
            }
            setAdapter = setupProgressAdapter(rvSet, ex.numSets, setStatus, true)
            setAdapter!!.setSelected(1)
            updateUI(ex, 0)
        }
    }

    override fun onItemClick(view: View?) {
        val ind = getTextInt(view as TextView)
        if (updateSetMode || (!currWorkout.isWarmup && ind < currWorkout.currSetNum)) {
            saveProgressMap()
            setAdapter!!.setSelected(ind)
            updateUI(updateEx!!, ind - 1)
        }
    }

    private fun updateUI(updateEx: Exercise, setInd: Int) {
        val set = updateEx.finishedSetsList[setInd]
        tvExerciseTitle.text = updateEx.name
        tvSetNum.text = String.format(setNum!!, "Main")
        equipmentView.setup(set.weight, updateEx.equipment!!)
        tvTimer.setText(R.string.update_set)
        etCurrReps.setText(set.reps.toString())
        etCurrWeight.setText(set.weight.toString())
    }

    override fun onItemLongClick(view: View?) {}

    companion object {
        @JvmStatic
        fun getInstance(): WorkoutScreen {
            return WorkoutScreen()
        }
    }
}
