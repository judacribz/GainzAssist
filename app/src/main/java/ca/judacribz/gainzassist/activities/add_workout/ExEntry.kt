package ca.judacribz.gainzassist.activities.add_workout

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import butterknife.*
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.models.Exercise
import ca.judacribz.gainzassist.models.Exercise.SetsType.MAIN_SET
import ca.judacribz.gainzassist.util.UI.setSpinnerWithArray
import ca.judacribz.gainzassist.util.UI.setText
import ca.judacribz.gainzassist.util.UI.setVisibleIfDisabled
import ca.judacribz.gainzassist.util.UI.setGoneIfEnabled
import ca.judacribz.gainzassist.util.UI.handleNumChanged
import ca.judacribz.gainzassist.util.UI.handleFocusLeft
import ca.judacribz.gainzassist.util.UI.validateForm
import ca.judacribz.gainzassist.util.UI.getTextString
import ca.judacribz.gainzassist.util.UI.getTextInt
import ca.judacribz.gainzassist.util.UI.getTextFloat
import java.util.*

class ExEntry : Fragment() {

    interface ExEntryDataListener {
        fun exerciseDoesNotExist(fmt: ExEntry, exerciseName: String, skipIndex: Int): Boolean
        fun exerciseDataReceived(exercise: Exercise, update: Boolean)
        fun deleteExercise(exercise: Exercise?, index: Int)
    }

    private var exEntryDataListener: ExEntryDataListener? = null

    var fragmentView: View? = null
    var exercise: Exercise? = null
    var exerciseName: String? = null
    var num_reps = -1
    var num_sets = -1
    var ex_i = 0
    var minInt = 1
    var weight = -1f
    var minWeight = 0f
    var weightChange = 0f

    var deleteHidden = false

    @BindView(R.id.et_exercise_name)
    lateinit var etExerciseName: EditText

    @BindView(R.id.et_weight)
    lateinit var etWeight: EditText

    @BindView(R.id.et_reps)
    lateinit var etNumReps: EditText

    @BindView(R.id.et_sets)
    lateinit var etNumSets: EditText

    @BindView(R.id.ibtn_dec_weight)
    lateinit var ibtnDecWeight: ImageButton

    @BindView(R.id.ibtn_dec_reps)
    lateinit var ibtnDecReps: ImageButton

    @BindView(R.id.ibtn_dec_sets)
    lateinit var ibtnDecSets: ImageButton

    @BindView(R.id.spr_equipment)
    lateinit var sprEquipment: Spinner

    @BindView(R.id.btn_enter)
    lateinit var btnEnter: Button

    @BindView(R.id.btn_update)
    lateinit var btnUpdate: Button

    @BindView(R.id.btn_delete)
    lateinit var btnDelete: Button

    @BindViews(R.id.et_exercise_name, R.id.et_weight, R.id.et_reps, R.id.et_sets)
    lateinit var formEntries: List<@JvmSuppressWildcards EditText>

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is ExEntryDataListener) {
            exEntryDataListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement ExEntryDataListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (fragmentView != null) {
            return fragmentView
        }
        retainInstance = true

        val v = inflater.inflate(R.layout.fragment_ex_entry, container, false)
        fragmentView = v
        ButterKnife.bind(this, v)

        num_reps = getString(R.string.starting_reps).toInt()
        num_sets = getString(R.string.starting_sets).toInt()

        setSpinnerWithArray(activity, R.array.exerciseEquipment, sprEquipment)

        if (exercise != null) {
            etExerciseName.setText(exercise!!.name)
            setText(etWeight, exercise!!.weight)
            setText(etNumSets, exercise!!.sets)
            setText(etNumReps, exercise!!.reps)
            btnEnter.visibility = View.GONE
            btnUpdate.visibility = View.VISIBLE

            if (deleteHidden) {
                btnDelete.visibility = View.INVISIBLE
            } else {
                btnDelete.visibility = View.VISIBLE
            }
        }

        return v
    }

    override fun onDetach() {
        super.onDetach()
        exEntryDataListener = null
    }

    fun setInd(index: Int) {
        this.ex_i = index
    }

    fun updateExFields(exercise: Exercise) {
        this.exercise = exercise
    }

    fun hideDelete() {
        deleteHidden = true
        if (::btnDelete.isInitialized) {
            btnDelete.visibility = View.INVISIBLE
        }
    }

    fun setExerciseExists() {
        etExerciseName.error = String.format(
            getString(R.string.err_exercise_exists),
            exerciseName
        )
    }

    @OnItemSelected(R.id.spr_equipment)
    fun equipmentSelected(position: Int) {
        when (position) {
            0 -> {
                minWeight = ca.judacribz.gainzassist.constants.ExerciseConst.BB_MIN_WEIGHT
                weightChange = ca.judacribz.gainzassist.constants.ExerciseConst.BB_WEIGHT_CHANGE
            }
            1 -> {
                minWeight = ca.judacribz.gainzassist.constants.ExerciseConst.DB_MIN_WEIGHT
                weightChange = ca.judacribz.gainzassist.constants.ExerciseConst.DB_WEIGHT_CHANGE
            }
            else -> {
                minWeight = ca.judacribz.gainzassist.constants.ExerciseConst.MIN_WEIGHT
                weightChange = ca.judacribz.gainzassist.constants.ExerciseConst.WEIGHT_CHANGE
            }
        }

        if (weight > minWeight) {
            setVisibleIfDisabled(ibtnDecWeight)
        } else {
            etWeight.setText(minWeight.toString())
            setGoneIfEnabled(ibtnDecWeight)
        }
    }

    @OnTextChanged(value = [R.id.et_weight], callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    fun beforeWeightChanged() {
        setVisibleIfDisabled(ibtnDecWeight)
    }

    @OnTextChanged(value = [R.id.et_weight], callback = OnTextChanged.Callback.TEXT_CHANGED)
    fun onWeightChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        weight = handleNumChanged(ibtnDecWeight, s.toString(), minWeight).toFloat()
    }

    @OnTextChanged(value = [R.id.et_reps], callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    fun beforeRepsChanged() {
        setVisibleIfDisabled(ibtnDecReps)
    }

    @OnTextChanged(value = [R.id.et_sets], callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    fun beforeSetsChanged() {
        setVisibleIfDisabled(ibtnDecSets)
    }

    @OnTextChanged(R.id.et_reps)
    fun onRepsChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        num_reps = handleNumChanged(ibtnDecReps, s.toString(), minInt).toInt()
    }

    @OnTextChanged(R.id.et_sets)
    fun onSetsChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        num_sets = handleNumChanged(ibtnDecSets, s.toString(), minInt).toInt()
    }

    @OnFocusChange(value = [R.id.et_reps, R.id.et_sets, R.id.et_weight])
    fun onFocusLeft(et: EditText, hasFocus: Boolean) {
        if (!hasFocus) {
            var min = 0f
            var res = 0f
            when (et.id) {
                R.id.et_weight -> {
                    res = weight
                    min = minWeight
                }
                R.id.et_reps -> {
                    res = num_reps.toFloat()
                    min = minInt.toFloat()
                }
                R.id.et_sets -> {
                    res = num_sets.toFloat()
                    min = minInt.toFloat()
                }
            }
            handleFocusLeft(et, min, res)
        }
    }

    @OnClick(R.id.ibtn_inc_weight)
    fun incNumWeight() {
        setText(etWeight, weight + weightChange)
    }

    @OnClick(R.id.ibtn_dec_weight)
    fun decNumWeight() {
        setText(etWeight, Math.max(weight - weightChange, minWeight))
    }

    @OnClick(R.id.ibtn_inc_reps)
    fun incNumReps() {
        setText(etNumReps, num_reps + 1)
    }

    @OnClick(R.id.ibtn_dec_reps)
    fun decNumReps() {
        setText(etNumReps, num_reps - 1)
    }

    @OnClick(R.id.ibtn_inc_sets)
    fun incNumSets() {
        setText(etNumSets, num_sets + 1)
    }

    @OnClick(R.id.ibtn_dec_sets)
    fun decNumSets() {
        setText(etNumSets, num_sets - 1)
    }

    @OnClick(R.id.btn_enter)
    fun enterExercise() {
        if (validateForm(activity!!, formEntries.toTypedArray())) {
            exerciseName = getTextString(etExerciseName)
            if (exEntryDataListener!!.exerciseDoesNotExist(this, exerciseName!!, ex_i)) {
                btnEnter.visibility = View.GONE
                btnUpdate.visibility = View.VISIBLE
                num_reps = getTextInt(etNumReps)
                num_sets = getTextInt(etNumSets)
                exercise = Exercise(
                    ex_i,
                    exerciseName,
                    "Strength",
                    getTextString(sprEquipment),
                    getTextInt(etNumSets),
                    getTextInt(etNumReps),
                    getTextFloat(etWeight),
                    MAIN_SET
                )
                exEntryDataListener!!.exerciseDataReceived(exercise!!, false)
            }
        }
    }

    @OnClick(R.id.btn_update)
    fun updateExercise() {
        if (validateForm(activity!!, formEntries.toTypedArray())) {
            exerciseName = getTextString(etExerciseName)
            if (exEntryDataListener!!.exerciseDoesNotExist(this, exerciseName!!, ex_i)) {
                num_reps = getTextInt(etNumReps)
                num_sets = getTextInt(etNumSets)
                exercise = Exercise(
                    ex_i,
                    exerciseName,
                    "Strength",
                    getTextString(sprEquipment),
                    getTextInt(etNumSets),
                    getTextInt(etNumReps),
                    getTextFloat(etWeight),
                    MAIN_SET
                )
                exEntryDataListener!!.exerciseDataReceived(exercise!!, true)
            }
        }
    }

    @OnClick(R.id.btn_delete)
    fun deleteExercise() {
        etExerciseName.setText("")
        exEntryDataListener!!.deleteExercise(exercise, ex_i)
    }
}
