package ca.judacribz.gainzassist.activities.add_workout

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.adapters.SingleItemAdapter
import ca.judacribz.gainzassist.constants.ExerciseConst.BB_MIN_WEIGHT
import ca.judacribz.gainzassist.constants.ExerciseConst.BB_WEIGHT_CHANGE
import ca.judacribz.gainzassist.constants.ExerciseConst.DB_MIN_WEIGHT
import ca.judacribz.gainzassist.constants.ExerciseConst.DB_WEIGHT_CHANGE
import ca.judacribz.gainzassist.constants.ExerciseConst.MIN_WEIGHT
import ca.judacribz.gainzassist.constants.ExerciseConst.WEIGHT_CHANGE
import ca.judacribz.gainzassist.databinding.ActivityNewWorkoutSummaryBinding
import ca.judacribz.gainzassist.models.Exercise
import ca.judacribz.gainzassist.models.Exercise.Companion.EQUIPMENT_TYPES
import ca.judacribz.gainzassist.models.Exercise.SetsType.MAIN_SET
import ca.judacribz.gainzassist.models.ExerciseSet
import ca.judacribz.gainzassist.models.Workout
import ca.judacribz.gainzassist.models.db.WorkoutViewModel
import ca.judacribz.gainzassist.util.Preferences.removeIncompleteSessionPref
import ca.judacribz.gainzassist.util.Preferences.removeIncompleteWorkoutPref
import ca.judacribz.gainzassist.util.UI.setInitTheme
import ca.judacribz.gainzassist.util.UI.setToolbar
import ca.judacribz.gainzassist.util.UI.setSpinnerWithArray
import ca.judacribz.gainzassist.util.UI.getTextString
import ca.judacribz.gainzassist.util.UI.getTextInt
import ca.judacribz.gainzassist.util.UI.getTextFloat
import ca.judacribz.gainzassist.util.UI.validateForm
import ca.judacribz.gainzassist.util.UI.clearFormEntry
import ca.judacribz.gainzassist.util.firebase.Database.addWorkoutFirebase
import com.orhanobut.logger.Logger
import org.parceler.Parcels
import java.util.*

class Summary : AppCompatActivity(), SingleItemAdapter.ItemClickObserver {

    enum class CALLING_ACTIVITY {
        WORKOUTS_LIST,
        EXERCISES_ENTRY
    }

    companion object {
        const val EXTRA_WORKOUT = "ca.judacribz.gainzassist.activities.add_workout.EXTRA_WORKOUT"
        const val EXTRA_CALLING_ACTIVITY =
            "ca.judacribz.gainzassist.activities.add_workout.EXTRA_CALLING_ACTIVITY"

        private const val MIN_INT = 1
        private const val MIN_FLOAT = 5.0f

        private const val POS_STREN = 0
        private const val POS_CARDIO = 1
        private const val POS_NA = 2
    }

    private var exerciseAdapter: SingleItemAdapter? = null
    private var exSets: ArrayList<ExerciseSet>? = null
    private var workoutId: Long = -1

    private var num_reps = 0
    private var num_sets = 0
    private var minInt = 1
    private var weight = 0f
    private var minWeight = MIN_WEIGHT
    private var weightChange = WEIGHT_CHANGE

    var workout: Workout? = null
    var exercises: ArrayList<Exercise>? = null
    val workoutViewModel by viewModels<WorkoutViewModel>()

    private lateinit var binding: ActivityNewWorkoutSummaryBinding

    private lateinit var formEntries: Array<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInitTheme(this)
        binding = ActivityNewWorkoutSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setToolbar(this, R.string.title_new_workout_summary, true)


        val sourceIntent = intent
        workout = Parcels.unwrap(sourceIntent.getParcelableExtra(EXTRA_WORKOUT))
        val currentWorkout = workout ?: return
        workoutId = currentWorkout.id

        when (sourceIntent.getSerializableExtra(EXTRA_CALLING_ACTIVITY) as? CALLING_ACTIVITY) {
            CALLING_ACTIVITY.WORKOUTS_LIST -> {
                binding.btnAddWorkout.text = getString(R.string.update_workout)
            }

            else -> {
                // Keep default
            }
        }

        formEntries = arrayOf(
            binding.partEtExercise.etExerciseName,
            binding.partEtReps.etReps,
            binding.partEtWeight.etWeight,
            binding.partEtSets.etSets
        )

        val workoutName = currentWorkout.name
        if (workoutName != null) {
            binding.partEtWorkout.etWorkoutName.setText(workoutName)
        }

        setSpinnerWithArray(this, R.array.exerciseEquipment, binding.sprEquipment)

        binding.rvExerciseBtns.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.rvExerciseBtns.setHasFixedSize(true)

        exercises = currentWorkout.exercises

        updateAdapter()

        binding.partEtWorkout.etWorkoutName.setText(currentWorkout.name)

        setupListeners()
    }

    private fun setupListeners() {
        binding.sprEquipment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                equipmentSelected(binding.sprEquipment, position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.partEtExercise.etExerciseName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onExerciseNameChanged(s ?: "", start, before, count)
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
                onWeightChanged(s ?: "", start, before, count)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.partEtReps.etReps.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                beforeNumChanged(binding.ibtnDecReps)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                num_reps =
                    onNumChanged(binding.partEtReps.etReps, binding.ibtnDecReps, s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.partEtSets.etSets.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                beforeNumChanged(binding.ibtnDecSets)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                num_sets =
                    onNumChanged(binding.partEtSets.etSets, binding.ibtnDecSets, s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.ibtnIncReps.setOnClickListener { incReps() }
        binding.ibtnDecReps.setOnClickListener { decReps() }
        binding.ibtnIncSets.setOnClickListener { incSets() }
        binding.ibtnDecSets.setOnClickListener { decSets() }
        binding.ibtnIncWeight.setOnClickListener { incWeight() }
        binding.ibtnDecWeight.setOnClickListener { decWeight() }
        binding.btnAddExercise.setOnClickListener { addExercise() }
        binding.btnUpdateExercise.setOnClickListener { updateExercise() }
        binding.btnAddWorkout.setOnClickListener { addWorkout() }
        binding.btnDiscardWorkout.setOnClickListener { discardWorkout() }
        binding.btnClearExercise.setOnClickListener { clearExercise() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    private fun updateAdapter() {
        val currentWorkout = workout ?: return
        exerciseAdapter = SingleItemAdapter(
            this,
            currentWorkout.exerciseNames,
            R.layout.part_square_button,
            R.id.sqrBtnListItem
        )
        exerciseAdapter?.setItemClickObserver(this)
        binding.rvExerciseBtns.adapter = exerciseAdapter
    }

    fun equipmentSelected(spinner: Spinner, position: Int) {
        when (position) {
            0 -> {
                minWeight = BB_MIN_WEIGHT
                weightChange = BB_WEIGHT_CHANGE
            }

            1 -> {
                minWeight = DB_MIN_WEIGHT
                weightChange = DB_WEIGHT_CHANGE
            }

            else -> {
                minWeight = MIN_WEIGHT
                weightChange = WEIGHT_CHANGE
            }
        }

        if (!binding.ibtnDecWeight.isEnabled || weight < minWeight) {
            binding.partEtWeight.etWeight.setText(minWeight.toString())
        }
    }

    fun onExerciseNameChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        val exerciseName = s.toString()

        if (exerciseName.isNotEmpty()) {
            if (workout?.containsExercise(exerciseName) == true) {
                switchExerciseBtns(binding.btnAddExercise, binding.btnUpdateExercise)
            } else {
                switchExerciseBtns(binding.btnUpdateExercise, binding.btnAddExercise)
            }
        } else {
            switchExerciseBtns(
                btnDisable = binding.btnUpdateExercise,
                btnEnable = binding.btnAddExercise
            )
        }
    }

    private fun switchExerciseBtns(btnDisable: Button, btnEnable: Button) {
        if (btnDisable.visibility == View.VISIBLE) {
            btnEnable.visibility = View.VISIBLE
            btnDisable.visibility = View.INVISIBLE
        }
    }

    fun onWeightChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        val weightStr = s.toString()
        weight = if (weightStr.isEmpty()) minWeight else weightStr.toFloat()

        if (weight <= minWeight) {
            binding.ibtnDecWeight.isEnabled = false
            binding.ibtnDecWeight.visibility = View.INVISIBLE

            if (weight < minWeight) {
                binding.partEtWeight.etWeight.setText(minWeight.toString())
            }
        }
    }

    fun beforeNumChanged(ibtnDec: ImageButton) {
        if (!ibtnDec.isEnabled) {
            ibtnDec.isEnabled = true
            ibtnDec.visibility = View.VISIBLE
        }
    }

    fun onNumChanged(etNum: EditText, ibtnDec: ImageButton, str: String): Int {
        val value = if (str.isEmpty()) minInt else str.toInt()

        if (value <= minInt) {
            ibtnDec.isEnabled = false
            ibtnDec.visibility = View.INVISIBLE

            if (value < minInt) {
                etNum.setText(minInt.toString())
            }
        }

        return value
    }

    fun incReps() {
        binding.partEtReps.etReps.setText((getTextInt(binding.partEtReps.etReps) + MIN_INT).toString())
    }

    fun decReps() {
        binding.partEtReps.etReps.setText(
            Math.max(
                getTextInt(binding.partEtReps.etReps) - MIN_INT,
                MIN_INT
            ).toString()
        )
    }

    fun incSets() {
        binding.partEtSets.etSets.setText((getTextInt(binding.partEtSets.etSets) + MIN_INT).toString())
    }

    fun decSets() {
        binding.partEtSets.etSets.setText(
            Math.max(
                getTextInt(binding.partEtSets.etSets) - MIN_INT,
                MIN_INT
            ).toString()
        )
    }

    fun incWeight() {
        binding.partEtWeight.etWeight.setText((getTextFloat(binding.partEtWeight.etWeight) + MIN_FLOAT).toString())
    }

    fun decWeight() {
        binding.partEtWeight.etWeight.setText(
            Math.max(
                getTextFloat(binding.partEtWeight.etWeight) - MIN_FLOAT,
                MIN_FLOAT
            ).toString()
        )
    }

    fun addExercise() {
        if (validateForm(this, formEntries)) {
            val exName = getTextString(binding.partEtExercise.etExerciseName)
            if (workout?.containsExercise(exName) == true) {
                binding.partEtExercise.etExerciseName.error =
                    getString(R.string.err_exercise_exists, exName)
            } else {
                exercises?.add(updateExerciseData(-1, exName))
                updateAdapter()
            }
        }
    }

    fun updateExercise() {
        if (validateForm(this, formEntries)) {
            val currentEx = ex ?: return
            val num = currentEx.exerciseNumber
            exercises?.let {
                if (num >= 0 && num < it.size) {
                    currentEx.name?.let { name ->
                        it[num] = updateExerciseData(num, name)
                    }
                }
            }
            updateAdapter()
        }
    }

    private fun updateExerciseData(exNumber: Int, exName: String): Exercise {
        var newExNumber = exNumber
        var id = -1L

        binding.partEtExercise.etExerciseName.setText("")

        if (newExNumber == -1) {
            newExNumber = workout?.numExercises ?: 0
            Logger.d(newExNumber)
        } else {
            ex?.let { id = it.id }
        }

        val exercise = Exercise(
            newExNumber,
            exName,
            "Strength",
            binding.sprEquipment.selectedItem.toString().lowercase(),
            getTextInt(binding.partEtSets.etSets),
            getTextInt(binding.partEtReps.etReps),
            getTextFloat(binding.partEtWeight.etWeight),
            MAIN_SET
        )

        exercise.workoutId = workoutId

        if (id != -1L) {
            exercise.id = id
        }

        return exercise
    }

    fun addWorkout() {
        if (validateForm(this, arrayOf(binding.partEtWorkout.etWorkoutName))) {
            val currentExercises = exercises
            if (currentExercises.isNullOrEmpty()) {
                Toast.makeText(this, "Error: No exercises added.", Toast.LENGTH_SHORT).show()
            } else {
                val btnText = getTextString(binding.btnAddWorkout).lowercase()
                val currentWorkout = workout ?: return

                currentWorkout.name = getTextString(binding.partEtWorkout.etWorkoutName)
                currentWorkout.exercises = currentExercises

                addWorkoutFirebase(currentWorkout)

                if ("add workout" == btnText) {
                    workoutViewModel.insertWorkout(currentWorkout)
                } else {
                    workoutViewModel.updateWorkout(currentWorkout)

                    currentWorkout.name?.let {
                        if (removeIncompleteWorkoutPref(this, it)) {
                            removeIncompleteSessionPref(this, it)
                        }
                    }
                }

                discardWorkout()
            }
        }
    }

    fun clearExercise() {
        clearFormEntry(binding.partEtExercise.etExerciseName)
        binding.partEtReps.etReps.setText(getString(R.string.starting_reps))
        binding.partEtSets.etSets.setText(getString(R.string.starting_sets))
        binding.partEtWeight.etWeight.setText(getString(R.string.starting_weight))
        binding.sprEquipment.setSelection(0)
    }

    fun discardWorkout() {
        setResult(RESULT_OK)
        finish()
    }

    override fun onItemClick(view: View?) {
        updateExerciseArea(getTextString(view as TextView))
    }

    var ex: Exercise? = null
    private fun updateExerciseArea(exName: String) {
        ex = workout?.getExerciseFromName(exName)
        binding.partEtExercise.etExerciseName.setText(exName)
        ex?.let {
            binding.partEtSets.etSets.setText(it.sets.toString())
            binding.partEtReps.etReps.setText(it.reps.toString())
            binding.partEtWeight.etWeight.setText(it.weight.toString())
            binding.sprEquipment.setSelection(EQUIPMENT_TYPES.indexOf(it.equipment))
        }
    }

    override fun onItemLongClick(view: View?) {
        // Keep Java behavior: no-op.
    }
}
