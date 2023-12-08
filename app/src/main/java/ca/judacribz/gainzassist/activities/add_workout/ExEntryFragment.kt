package ca.judacribz.gainzassist.activities.add_workout

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.BindViews
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnFocusChange
import butterknife.OnItemSelected
import butterknife.OnTextChanged
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.constants.ExerciseConst
import ca.judacribz.gainzassist.models.Exercise
import ca.judacribz.gainzassist.util.UI

// --------------------------------------------------------------------------------------------
// ######################################################################################### //
// ExEntry Constructor/Instance                                                        //
// ######################################################################################### //
class ExEntryFragment : Fragment() {
    // Interfaces
    // --------------------------------------------------------------------------------------------
    private var exEntryDataListener: ExEntryDataListener? = null

    interface ExEntryDataListener {
        fun exerciseDoesNotExist(fmt: ExEntryFragment, exerciseName: String, skipIndex: Int): Boolean
        fun exerciseDataReceived(exercise: Exercise, update: Boolean)
        fun deleteExercise(exercise: Exercise, index: Int)
    }

    // --------------------------------------------------------------------------------------------
    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------
    // Global Vars
    // --------------------------------------------------------------------------------------------
    lateinit var exercise: Exercise
    private lateinit var exerciseName: String
    private var num_reps = -1
    private var num_sets = -1
    private var ex_i = 0
    private var minInt: Int = ExerciseConst.MIN_INT
    var weight = -1f
    private var minWeight = 0f
    private var weightChange = 0f
    private var deleteHidden = false

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
    lateinit var formEntries: Array<EditText>

    // ######################################################################################### //
    fun setInd(index: Int) {
        ex_i = index
    }

    fun updateExFields(exercise: Exercise) {
        this.exercise = exercise
    }

    fun hideDelete() {
        btnDelete.visibility = View.INVISIBLE
    }

    // Fragment Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun onAttach(context: Context) {
        super.onAttach(context)
        exEntryDataListener = if (context is ExEntryDataListener) {
            context
        } else {
            throw RuntimeException(
                context.toString()
                        + " must implement ExEntryDataListener"
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true
        val view = inflater.inflate(R.layout.fragment_ex_entry, container, false)
        ButterKnife.bind(this, view)

        num_reps = Integer.valueOf(getString(R.string.starting_reps))
        num_sets = Integer.valueOf(getString(R.string.starting_sets))
        UI.setSpinnerWithArray(getActivity(), R.array.exerciseEquipment, sprEquipment)
        exercise.let { exercise ->
            etExerciseName?.setText(exercise.name)
            UI.setText(etWeight, exercise.weight)
            UI.setText(etNumSets, exercise.sets)
            UI.setText(etNumReps, exercise.reps)
            btnEnter.visibility = View.GONE
            btnUpdate.visibility = View.VISIBLE
            if (deleteHidden) {
                btnDelete.visibility = View.GONE
            } else {
                btnDelete.visibility = View.VISIBLE
            }
        }

        return view
    }

    override fun onDetach() {
        super.onDetach()
        exEntryDataListener = null
    }

    //Fragment//Override///////////////////////////////////////////////////////////////////////////
    // OnItemSelected Handling
    // ============================================================================================
    @OnItemSelected(R.id.spr_equipment)
    fun equipmentSelected(position: Int) {
        when (position) {
            0 -> {
                minWeight = ExerciseConst.BB_MIN_WEIGHT
                weightChange = ExerciseConst.BB_WEIGHT_CHANGE
            }

            1 -> {
                minWeight = ExerciseConst.DB_MIN_WEIGHT
                weightChange = ExerciseConst.DB_WEIGHT_CHANGE
            }

            else -> {
                minWeight = ExerciseConst.MIN_WEIGHT
                weightChange = ExerciseConst.WEIGHT_CHANGE
            }
        }
        if (weight > minWeight) {
            UI.setVisibleIfDisabled(ibtnDecWeight)
        } else {
            etWeight?.setText(minWeight.toString())
            UI.setGoneIfEnabled(ibtnDecWeight)
        }
    }

    // =OnItemSelected=Handling=====================================================================
    // OnTextChanged Handling
    // =============================================================================================
    @OnTextChanged(value = [R.id.et_weight], callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    fun beforeNumExercisesChanged() {
        UI.setVisibleIfDisabled(ibtnDecWeight)
    }

    @OnTextChanged(value = [R.id.et_weight], callback = OnTextChanged.Callback.TEXT_CHANGED)
    fun onNumExercisesChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        weight = UI.handleNumChanged(ibtnDecWeight, s.toString(), minWeight).toFloat()
    }

    @OnTextChanged(value = [R.id.et_reps], callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    fun beforeRepsChanged() {
        UI.setVisibleIfDisabled(ibtnDecReps)
    }

    @OnTextChanged(value = [R.id.et_sets], callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    fun beforeSetsChanged() {
        UI.setVisibleIfDisabled(ibtnDecSets)
    }

    @OnTextChanged(R.id.et_reps)
    fun onRepsChanged(
        s: CharSequence,
        start: Int,
        before: Int,
        count: Int
    ) {
        num_reps = UI.handleNumChanged(ibtnDecReps, s.toString(), minInt).toInt()
    }

    @OnTextChanged(R.id.et_sets)
    fun onSetsChanged(
        s: CharSequence,
        start: Int,
        before: Int,
        count: Int
    ) {
        num_sets = UI.handleNumChanged(ibtnDecSets, s.toString(), minInt).toInt()
    }

    // =OnTextChanged=Handling======================================================================
    // OnFocusChanged Handling
    // =============================================================================================
    @OnFocusChange(R.id.et_reps, R.id.et_sets, R.id.et_weight)
    fun onFocusLeft(et: EditText, hasFocus: Boolean) {
        if (!hasFocus) {
            var min: Number = 0
            var res: Number = 0
            when (et.getId()) {
                R.id.et_weight -> {
                    res = weight
                    min = minWeight
                }

                R.id.et_reps -> {
                    res = num_reps
                    min = minInt
                }

                R.id.et_sets -> {
                    res = num_sets
                    min = minInt
                }
            }
            UI.handleFocusLeft(et, min, res)
        }
    }

    // =OnFocusChanged=Handling=====================================================================
    // Click Handling
    // =============================================================================================
    /* Increase weight */
    @OnClick(R.id.ibtn_inc_weight)
    fun incNumWeight() {
        UI.setText(etWeight, weight + weightChange)
    }

    /* Decrease weight */
    @OnClick(R.id.ibtn_dec_weight)
    fun decNumWeight() {
        UI.setText(etWeight, Math.max(weight - weightChange, minWeight))
    }

    /* Increase num_reps */
    @OnClick(R.id.ibtn_inc_reps)
    fun incNumReps() {
        UI.setText(etNumReps, num_reps + 1)
    }

    /* Decrease num_reps */
    @OnClick(R.id.ibtn_dec_reps)
    fun decNumReps() {
        UI.setText(etNumReps, num_reps - 1)
    }

    /* Increase num_sets */
    @OnClick(R.id.ibtn_inc_sets)
    fun incNumSets() {
        UI.setText(etNumSets, num_sets + 1)
    }

    /* Decrease num_sets */
    @OnClick(R.id.ibtn_dec_sets)
    fun decNumSets() {
        UI.setText(etNumSets, num_sets - 1)
    }

    @OnClick(R.id.btn_enter)
    fun enterExercise() {
        if (UI.validateForm(getActivity(), formEntries)) {
            exerciseName = UI.getTextString(etExerciseName)

            // Check if exercise already added
            if (exEntryDataListener?.exerciseDoesNotExist(this, exerciseName, ex_i) == true) {
                btnEnter.visibility = View.GONE
                btnUpdate.visibility = View.VISIBLE
                num_reps = UI.getTextInt(etNumReps)
                num_sets = UI.getTextInt(etNumSets)
                exEntryDataListener?.exerciseDataReceived(
                    exercise = Exercise(
                        ex_i,
                        exerciseName,
                        "Strength",
                        UI.getTextString(sprEquipment),
                        UI.getTextInt(etNumSets),
                        UI.getTextInt(etNumReps),
                        UI.getTextFloat(etWeight), Exercise.SetsType.MAIN_SET
                    ).also { exercise = it },
                    update = false
                )
            }
        }
    }

    fun setExerciseExists() {
        etExerciseName.error = String.format(
            getString(R.string.err_exercise_exists),
            exerciseName
        )
    }

    @OnClick(R.id.btn_update)
    fun updateExercise() {
        if (UI.validateForm(getActivity(), formEntries)) {
            exerciseName = UI.getTextString(etExerciseName)

            // Check if exercise already added
            if (exEntryDataListener?.exerciseDoesNotExist(this, exerciseName, ex_i) == true) {
                num_reps = UI.getTextInt(etNumReps)
                num_sets = UI.getTextInt(etNumSets)
                exEntryDataListener?.exerciseDataReceived(
                    Exercise(
                        ex_i,
                        exerciseName.toString(),
                        "Strength",
                        UI.getTextString(sprEquipment),
                        UI.getTextInt(etNumSets),
                        UI.getTextInt(etNumReps),
                        UI.getTextFloat(etWeight), Exercise.SetsType.MAIN_SET
                    ).also { exercise = it }, true
                )
            }
        }
    }

    @OnClick(R.id.btn_delete)
    fun deleteExercise() {
        etExerciseName.setText("")
        exEntryDataListener?.deleteExercise(exercise, ex_i)
    } //=Click=Handling===============================================================================
}
