package ca.judacribz.gainzassist.activities.add_workout

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.databinding.FragmentExEntryBinding
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

    private lateinit var binding: FragmentExEntryBinding

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
        if (::binding.isInitialized) {
            return binding.root
        }
        retainInstance = true

        binding = FragmentExEntryBinding.inflate(inflater, container, false)
        val v = binding.root

        formEntries = listOf(
            binding.partEtExercise.etExerciseName,
            binding.partEtWeight.etWeight,
            binding.partEtReps.etReps,
            binding.partEtSets.etSets
        )

        num_reps = getString(R.string.starting_reps).toInt()
        num_sets = getString(R.string.starting_sets).toInt()

        setSpinnerWithArray(activity, R.array.exerciseEquipment, binding.sprEquipment)

        if (exercise != null) {
            binding.partEtExercise.etExerciseName.setText(exercise!!.name)
            setText(binding.partEtWeight.etWeight, exercise!!.weight)
            setText(binding.partEtSets.etSets, exercise!!.sets)
            setText(binding.partEtReps.etReps, exercise!!.reps)
            binding.btnEnter.visibility = View.GONE
            binding.btnUpdate.visibility = View.VISIBLE

            if (deleteHidden) {
                binding.btnDelete.visibility = View.INVISIBLE
            } else {
                binding.btnDelete.visibility = View.VISIBLE
            }
        }

        setupListeners()

        return v
    }

    private fun setupListeners() {
        binding.sprEquipment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                equipmentSelected(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.partEtWeight.etWeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                setVisibleIfDisabled(binding.ibtnDecWeight)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                weight = handleNumChanged(binding.ibtnDecWeight, s.toString(), minWeight).toFloat()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.partEtReps.etReps.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                setVisibleIfDisabled(binding.ibtnDecReps)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                num_reps = handleNumChanged(binding.ibtnDecReps, s.toString(), minInt).toInt()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.partEtSets.etSets.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                setVisibleIfDisabled(binding.ibtnDecSets)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                num_sets = handleNumChanged(binding.ibtnDecSets, s.toString(), minInt).toInt()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        val focusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus && v is EditText) {
                var min = 0f
                var res = 0f
                when (v.id) {
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
                handleFocusLeft(v, min, res)
            }
        }
        binding.partEtReps.etReps.onFocusChangeListener = focusChangeListener
        binding.partEtSets.etSets.onFocusChangeListener = focusChangeListener
        binding.partEtWeight.etWeight.onFocusChangeListener = focusChangeListener

        binding.ibtnIncWeight.setOnClickListener { incNumWeight() }
        binding.ibtnDecWeight.setOnClickListener { decNumWeight() }
        binding.ibtnIncReps.setOnClickListener { incNumReps() }
        binding.ibtnDecReps.setOnClickListener { decNumReps() }
        binding.ibtnIncSets.setOnClickListener { incNumSets() }
        binding.ibtnDecSets.setOnClickListener { decNumSets() }
        binding.btnEnter.setOnClickListener { enterExercise() }
        binding.btnUpdate.setOnClickListener { updateExercise() }
        binding.btnDelete.setOnClickListener { deleteExercise() }
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
        if (::binding.isInitialized) {
            binding.btnDelete.visibility = View.INVISIBLE
        }
    }

    fun setExerciseExists() {
        binding.partEtExercise.etExerciseName.error = String.format(
            getString(R.string.err_exercise_exists),
            exerciseName
        )
    }

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
            setVisibleIfDisabled(binding.ibtnDecWeight)
        } else {
            binding.partEtWeight.etWeight.setText(minWeight.toString())
            setGoneIfEnabled(binding.ibtnDecWeight)
        }
    }

    fun incNumWeight() {
        setText(binding.partEtWeight.etWeight, weight + weightChange)
    }

    fun decNumWeight() {
        setText(binding.partEtWeight.etWeight, Math.max(weight - weightChange, minWeight))
    }

    fun incNumReps() {
        setText(binding.partEtReps.etReps, num_reps + 1)
    }

    fun decNumReps() {
        setText(binding.partEtReps.etReps, num_reps - 1)
    }

    fun incNumSets() {
        setText(binding.partEtSets.etSets, num_sets + 1)
    }

    fun decNumSets() {
        setText(binding.partEtSets.etSets, num_sets - 1)
    }

    fun enterExercise() {
        if (validateForm(activity!!, formEntries.toTypedArray())) {
            exerciseName = getTextString(binding.partEtExercise.etExerciseName)
            if (exEntryDataListener!!.exerciseDoesNotExist(this, exerciseName!!, ex_i)) {
                binding.btnEnter.visibility = View.GONE
                binding.btnUpdate.visibility = View.VISIBLE
                num_reps = getTextInt(binding.partEtReps.etReps)
                num_sets = getTextInt(binding.partEtSets.etSets)
                exercise = Exercise(
                    ex_i,
                    exerciseName,
                    "Strength",
                    getTextString(binding.sprEquipment),
                    getTextInt(binding.partEtSets.etSets),
                    getTextInt(binding.partEtReps.etReps),
                    getTextFloat(binding.partEtWeight.etWeight),
                    MAIN_SET
                )
                exEntryDataListener!!.exerciseDataReceived(exercise!!, false)
            }
        }
    }

    fun updateExercise() {
        if (validateForm(activity!!, formEntries.toTypedArray())) {
            exerciseName = getTextString(binding.partEtExercise.etExerciseName)
            if (exEntryDataListener!!.exerciseDoesNotExist(this, exerciseName!!, ex_i)) {
                num_reps = getTextInt(binding.partEtReps.etReps)
                num_sets = getTextInt(binding.partEtSets.etSets)
                exercise = Exercise(
                    ex_i,
                    exerciseName,
                    "Strength",
                    getTextString(binding.sprEquipment),
                    getTextInt(binding.partEtSets.etSets),
                    getTextInt(binding.partEtReps.etReps),
                    getTextFloat(binding.partEtWeight.etWeight),
                    MAIN_SET
                )
                exEntryDataListener!!.exerciseDataReceived(exercise!!, true)
            }
        }
    }

    fun deleteExercise() {
        binding.partEtExercise.etExerciseName.setText("")
        exEntryDataListener!!.deleteExercise(exercise, ex_i)
    }
}
