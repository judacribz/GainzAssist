package ca.judacribz.gainzassist.activities.start_workout.fragments

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnFocusChange
import butterknife.OnTextChanged
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.start_workout.CurrWorkout
import ca.judacribz.gainzassist.activities.start_workout.CurrWorkout.DataListener
import ca.judacribz.gainzassist.activities.start_workout.EquipmentView
import ca.judacribz.gainzassist.activities.start_workout.StartWorkoutActivity
import ca.judacribz.gainzassist.adapters.SingleItemAdapter
import ca.judacribz.gainzassist.adapters.SingleItemAdapter.ItemClickObserver
import ca.judacribz.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS
import ca.judacribz.gainzassist.constants.ExerciseConst
import ca.judacribz.gainzassist.constants.UIConst
import ca.judacribz.gainzassist.models.Exercise
import ca.judacribz.gainzassist.models.Exercise.SetsType
import ca.judacribz.gainzassist.models.ExerciseSet
import ca.judacribz.gainzassist.models.db.WorkoutViewModel
import ca.judacribz.gainzassist.util.Misc
import ca.judacribz.gainzassist.util.Preferences
import ca.judacribz.gainzassist.util.UI
import com.orhanobut.logger.Logger
import java.util.Locale

class WorkoutScreen  // --------------------------------------------------------------------------------------------
// ######################################################################################### //
// WorkoutScreen Constructor/Instance                                                        //
// ######################################################################################### //
    : Fragment(), DataListener, ItemClickObserver {
    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------
    // Global Vars
    // --------------------------------------------------------------------------------------------
    var currWorkout = CurrWorkout.instance
    var act: StartWorkoutActivity? = null
    var bundle: Bundle? = null
    var countDownTimer: CountDownTimer? = null
    var exerciseAdapter: SingleItemAdapter? = null
    var setAdapter: SingleItemAdapter? = null
    var setManager: LinearLayoutManager? = null
    var exerciseManager: LinearLayoutManager? = null
    var finExercises: ArrayList<Exercise>? = null
    var updateEx: Exercise? = null
    var currSet: ExerciseSet? = null
    var updateSetMode = false
    var currTime: Long = 0
    var weight = 0f
    var exProgress: SparseArray<PROGRESS_STATUS?>? = null
    var setProgress: SparseArray<PROGRESS_STATUS?>? = null
    var setNum: String? = null

    // --------------------------------------------------------------------------------------------
    // UI Elements
    @JvmField
    @BindView(R.id.equip_view)
    var equipmentView: EquipmentView? = null

    @JvmField
    @BindView(R.id.tv_timer)
    var tvTimer: TextView? = null

    @JvmField
    @BindView(R.id.tv_exercise_title)
    var tvExerciseTitle: TextView? = null

    @JvmField
    @BindView(R.id.tv_set_num)
    var tvSetNum: TextView? = null

    @JvmField
    @BindView(R.id.ibtn_dec_reps)
    var btnDecReps: ImageButton? = null

    @JvmField
    @BindView(R.id.ibtn_dec_weight)
    var btnDecWeight: ImageButton? = null

    @JvmField
    @BindView(R.id.btn_finish_set)
    var btnFinishSet: Button? = null

    @JvmField
    @BindView(R.id.btn_update_set)
    var btnUpdateSet: Button? = null

    @JvmField
    @BindView(R.id.btn_resume_workout)
    var btnResumeWorkout: Button? = null

    @JvmField
    @BindView(R.id.et_reps)
    var etCurrReps: EditText? = null

    @JvmField
    @BindView(R.id.et_weight)
    var etCurrWeight: EditText? = null

    @JvmField
    @BindView(R.id.rv_exercise_num)
    var rvExercise: RecyclerView? = null

    @JvmField
    @BindView(R.id.rv_exercise_set)
    var rvSet: RecyclerView? = null

    // ######################################################################################### //
    // Fragment Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun onAttach(context: Context) {
        super.onAttach(context)
        act = context as StartWorkoutActivity



        // init finished workout variables
        finExercises = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_workout_screen, container, false)
        ButterKnife.bind(this, view)
        setNum = "%s " + getString(R.string.set_num)
        setManager = setProgressLayoutManagers(rvSet)
        exerciseManager = setProgressLayoutManagers(rvExercise)
        rvSet!!.setHasFixedSize(true)
        rvExercise!!.setHasFixedSize(true)
        return view
    }

    private fun setProgressLayoutManagers(rv: RecyclerView?): LinearLayoutManager {
        val manager = LinearLayoutManager(
            act,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        rv!!.layoutManager = manager
        rv.setHasFixedSize(true)
        return manager
    }

    override fun onResume() {
        super.onResume()
        currWorkout.setDataListener(this)
        val progressJson = Preferences.getSessionProgressPref(act, currWorkout.workoutName)
        if (progressJson != null && setProgress == null) {
            val map = Misc.readValue(progressJson)
            val exMap: Map<String?, Any>
            val setMap: Map<String?, Any>
            exMap = Misc.readValue(map["exercise progress"])
            setMap = Misc.readValue(map["set progress"])
            exProgress = SparseArray()
            for ((key, value) in exMap) {
                exProgress!!.put(
                    Integer.valueOf(key),
                    UIConst.PROGRESS_STATUS_MAP[Integer.valueOf(value.toString())]
                )
            }
            setProgress = SparseArray()
            for ((key, value) in setMap) {
                setProgress!!.put(
                    Integer.valueOf(key),
                    UIConst.PROGRESS_STATUS_MAP[Integer.valueOf(value.toString())]
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
        exerciseAdapter = setupProgressAdapter(
            rvExercise,
            numExs,
            exProgress,
            false
        )
        exerciseAdapter!!.setItemClickObserver(object : ItemClickObserver {
            override fun onItemClick(view: View) {
                exerciseItemClick(view)
            }

            override fun onItemLongClick(view: View) {}
        })
    }

    var updateProgress = true
    fun updateProgSets(numSets: Int) {
        if (setProgress == null) {
            setProgress = setupProgress(numSets, currWorkout.currSetNum, null)
            updateProgress = true
        } else {
            updateProgress = false
        }
        setAdapter = setupProgressAdapter(
            rvSet,
            numSets,
            setProgress,
            true
        )
    }

    override fun onPause() {
        super.onPause()
        if (!workoutFinished) {
            saveProgressMap()
        }
        currWorkout.setDataListener(null)
    }

    fun saveProgressMap() {
        val progressMap: MutableMap<String, Any> = HashMap()
        val exMap: MutableMap<String, Any?> = HashMap()
        val setMap: MutableMap<String, Any?> = HashMap()
        for (i in 0 until exProgress!!.size()) {
            exMap[i.toString()] = UIConst.PROGRESS_CODE_MAP[exProgress!![i]]
        }
        for (i in 0 until setProgress!!.size()) {
            setMap[i.toString()] = UIConst.PROGRESS_CODE_MAP[setProgress!![i]]
        }
        progressMap["exercise progress"] = exMap
        progressMap["set progress"] = setMap
        Preferences.addSessionProgressPref(
            act,
            currWorkout.workoutName,
            Misc.writeValueAsString(progressMap)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    //Fragment//Override///////////////////////////////////////////////////////////////////////////
    // CurrWorkout.DataListener Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun startTimer(timeInMillis: Long) {
        if (countDownTimer != null) {
            countDownTimer!!.cancel()
        }
        countDownTimer = getCountDownTimer(timeInMillis)
        countDownTimer!!.start()
    }

    /* Creates and returns a new CountDownTimer with the rest time to count down from. Has the
     * following format: 0:00
     *********************************************************************************************/
    fun getCountDownTimer(milliseconds: Long): CountDownTimer {
        return object : CountDownTimer(milliseconds, 1000) {
            var seconds: Long = 0
            var minutes: Long = 0

            // Calculates the minutes and seconds to display in 0:00 format
            override fun onTick(millisUntilFinished: Long) {
                currTime = millisUntilFinished
                seconds = currTime / 1000
                minutes = seconds / 60
                seconds = seconds % 60
                val time =
                    minutes.toString() + ":" + String.format(Locale.getDefault(), "%02d", seconds)
                tvTimer!!.text = time
            }

            // Changes the timer text, when it gets to 0:00, to "Start the next set"
            override fun onFinish() {
                tvTimer!!.setText(R.string.start_next_set)
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
            ).also {
                setProgress = it
            },
            true
        )
    }

    //CurrWorkout.DataListener//Override///////////////////////////////////////////////////////////
    private fun setupProgressAdapter(
        rv: RecyclerView?,
        numItems: Int,
        progressStatus: SparseArray<PROGRESS_STATUS?>?,
        setClickListener: Boolean
    ): SingleItemAdapter {
        val adapter = SingleItemAdapter(
            act,
            numItems,
            R.layout.part_text_view_progress,
            R.id.tv_progress,
            progressStatus
        )
        rv!!.adapter = adapter
        if (setClickListener) {
            adapter.setItemClickObserver(this)
        }
        return adapter
    }

    private fun setupProgress(
        numItems: Int,
        itemInd: Int,
        setType: SetsType?
    ): SparseArray<PROGRESS_STATUS?> {
        val progressStatus = SparseArray<PROGRESS_STATUS?>()
        for (i in 0 until numItems) {
            progressStatus.put(i, PROGRESS_STATUS.UNSELECTED)
        }
        progressStatus.put(itemInd - 1, PROGRESS_STATUS.SELECTED)
        return progressStatus
    }

    // TextWatcher Handling
    // =============================================================================================
    @OnTextChanged(value = [R.id.et_reps], callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    fun beforeRepsChanged() {
        if (!btnDecReps!!.isEnabled) {
            btnDecReps!!.isEnabled = true
            btnDecReps!!.visibility = View.VISIBLE
        }
    }

    @OnTextChanged(value = [R.id.et_reps], callback = OnTextChanged.Callback.TEXT_CHANGED)
    fun onRepsChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        val repStr = s.toString()
        val reps: Int
        reps = if (!repStr.isEmpty()) Integer.valueOf(repStr) else ExerciseConst.MIN_REPS
        currWorkout.setCurrReps(reps, false)
        if (currWorkout.isMinReps) {
            btnDecReps!!.isEnabled = false
            btnDecReps!!.visibility = View.INVISIBLE
        }
    }

    // TextWatcher for weight ET
    @OnTextChanged(value = [R.id.et_weight], callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    fun beforeWeightChanged() {
        if (!btnDecWeight!!.isEnabled) {
            btnDecWeight!!.isEnabled = true
            btnDecWeight!!.visibility = View.VISIBLE
        }
    }

    @OnTextChanged(value = [R.id.et_weight], callback = OnTextChanged.Callback.TEXT_CHANGED)
    fun onWeightChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        val weight: Float
        if (!s.toString().isEmpty()) {
            weight = java.lang.Float.valueOf(s.toString())
            if (equipmentView != null) equipmentView!!.setup(weight, currWorkout.currEquip)
        } else weight = currWorkout.currMinWeight
        currWorkout.setWeight(weight)
        if (currWorkout.isMinWeight || weight <= currWorkout.currMinWeight) {
            btnDecWeight!!.isEnabled = false
            btnDecWeight!!.visibility = View.INVISIBLE
        }
    }

    // =TextWatcher=Handling========================================================================
    // OnFocusChanged Handling
    // =============================================================================================
    @OnFocusChange(R.id.et_reps, R.id.et_weight)
    fun onFocusLeft(et: EditText, hasFocus: Boolean) {
        if (!hasFocus) {
            val min: Number
            val res: Number
            when (et.id) {
                R.id.et_weight -> {
                    min = currWorkout.currMinWeight
                    res = currWorkout.currWeight
                }

                else -> {
                    min = ExerciseConst.MIN_REPS
                    res = currWorkout.currReps
                }
            }
            UI.handleFocusLeft(et, min, res)
        }
    }

    // =OnFocusChanged=Handling=====================================================================
    // Click Handling
    // =============================================================================================
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

    // Reps change
    @OnClick(R.id.ibtn_inc_reps, R.id.ibtn_dec_reps)
    fun changeReps(repsBtn: ImageButton) {
        when (repsBtn.id) {
            R.id.ibtn_inc_reps -> currWorkout.incReps()
            R.id.ibtn_dec_reps -> currWorkout.decReps()
        }
        setReps()
    }

    // Weight change
    @OnClick(R.id.ibtn_inc_weight, R.id.ibtn_dec_weight)
    fun changeWeight(weightBtn: ImageButton) {
        when (weightBtn.id) {
            R.id.ibtn_inc_weight -> currWorkout.incWeight()
            R.id.ibtn_dec_weight -> currWorkout.decWeight()
        }
        setWeight()
    }

    @OnClick(R.id.btn_resume_workout)
    fun resumeWorkout() {
        updateSetMode = false
        btnFinishSet!!.visibility = View.VISIBLE
        btnUpdateSet!!.visibility = View.INVISIBLE
        btnResumeWorkout!!.visibility = View.INVISIBLE
        setAdapter = setupProgressAdapter(
            rvSet,
            currWorkout.currNumSets,
            setProgress,
            true
        )
        exerciseAdapter!!.setSelected(currWorkout.currExNum)
        tvExerciseTitle!!.text = currWorkout.currExName
        equipmentView!!.setup(currSet!!.weight!!, currWorkout.currEquip)
        etCurrReps!!.setText(currSet!!.reps.toString())
        etCurrWeight!!.setText(currSet!!.weight.toString())
        updateUI()
        currSet = null
    }

    var workoutFinished = false

    // Finish set
    @OnClick(R.id.btn_finish_set)
    fun finishSet() {
        if (currWorkout.finishCurrSet()) {
            updateUI()

            // End of workout
        } else {
            workoutFinished = true
            val workoutViewModel : WorkoutViewModel by viewModels()
            workoutViewModel.insertSession(currWorkout.currSession)
            if (Preferences.removeIncompleteWorkoutPref(act, currWorkout.workoutName)) {
                Preferences.removeIncompleteSessionPref(act, currWorkout.workoutName)
            }
            Preferences.removeSessionProgressPref(act, currWorkout.workoutName)
            act!!.finish()
        }
    }

    fun updateUI() {
        val setType: String
        if (currWorkout.isWarmup) {
            if (countDownTimer != null) {
                countDownTimer!!.onFinish()
                countDownTimer = null
            }
            setType = "Warmup"
        } else {
            setType = "Main"
        }
        if (!currWorkout.lockReps) {
            setReps()
        }
        if (!currWorkout.lockWeight) {
            setWeight()
        }
        tvExerciseTitle!!.text = currWorkout.currExName
        tvSetNum!!.text = String.format(setNum!!, setType)
        if (updateProgress) {
            selectProgressAdapterPos(
                exerciseAdapter,
                rvExercise,
                currWorkout.currExNum,
                currWorkout.getExSuccess()
            )
            selectProgressAdapterPos(
                setAdapter,
                rvSet,
                currWorkout.currSetNum,
                currWorkout.setSuccess
            )
        } else {
            updateProgress = true
        }
        Logger.d("CURR SET NUM = " + currWorkout.currSetNum)
    }

    var setsToUpdate: ArrayList<ExerciseSet>? = null
    private fun setReps() {
        etCurrReps!!.setText(currWorkout.currReps.toString())
    }

    fun setWeight() {
        weight = currWorkout.currWeight
        etCurrWeight!!.setText(weight.toString())
        equipmentView!!.post { equipmentView!!.setup(weight, currWorkout.currEquip) }
    }

    private fun selectProgressAdapterPos(
        adapter: SingleItemAdapter?,
        rv: RecyclerView?,
        pos: Int,
        success: Boolean
    ) {
        adapter!!.setCurrItem(pos, success)
        rv!!.scrollToPosition(pos - 1)
    }

    // SingleItemAdapter.ItemClickObserver Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private fun exerciseItemClick(view: View) {
        val ind = UI.getTextInt(view as TextView)
        updateEx = currWorkout.getSessionExercise(ind)
        Logger.d("OHH $ind")
        if (updateEx != null) {
            val setStatus = SparseArray<PROGRESS_STATUS?>()
            updateSetMode = true
            if (btnFinishSet!!.visibility == View.VISIBLE) {
                btnFinishSet!!.visibility = View.INVISIBLE
                btnUpdateSet!!.visibility = View.VISIBLE
                btnResumeWorkout!!.visibility = View.VISIBLE
            }
            if (currSet == null) {
                currSet = ExerciseSet(
                    updateEx!!,
                    currWorkout.currSetNum,
                    currWorkout.currReps,
                    currWorkout.currWeight
                )
            }
            exerciseAdapter!!.setSelected(ind)
            setsToUpdate = updateEx!!.finishedSetsList
            for (set in setsToUpdate!!) {
                if (set.reps!! >= updateEx!!.reps!! && set.weight!! >= updateEx!!.weight!!) {
                    setStatus.put(set.setNumber!!, PROGRESS_STATUS.SUCCESS)
                } else {
                    setStatus.put(set.setNumber!!, PROGRESS_STATUS.FAIL)
                }
            }
            setAdapter = setupProgressAdapter(
                rvSet,
                updateEx!!.numSets,
                setStatus,
                true
            )
            setAdapter!!.setSelected(1)
            updateUI(updateEx, 0)
        }
    }

    override fun onItemClick(view: View) {
        val ind = UI.getTextInt(view as TextView)
        if (updateSetMode || !currWorkout.isWarmup && ind < currWorkout.currSetNum) {
            saveProgressMap()
            setAdapter!!.setSelected(ind)
            updateUI(updateEx, ind - 1)
        }
    }

    private fun updateUI(updateEx: Exercise?, setInd: Int) {
        val set = updateEx!!.finishedSetsList[setInd]
        tvExerciseTitle!!.text = updateEx.name
        tvSetNum!!.text = String.format(setNum!!, "Main")
        equipmentView!!.setup(set.weight!!, updateEx.getEquipment())
        tvTimer!!.setText(R.string.update_set)
        etCurrReps!!.setText(set.reps.toString())
        etCurrWeight!!.setText(set.weight.toString())
    }

    override fun onItemLongClick(view: View) {} //SingleItemAdapter.ItemClickObserver//Override////////////////////////////////////////////////

    //=Click=Handling===============================================================================
    companion object {
        val instance: WorkoutScreen
            get() = WorkoutScreen()
    }
}
