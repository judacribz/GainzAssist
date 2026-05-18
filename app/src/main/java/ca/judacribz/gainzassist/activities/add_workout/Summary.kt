package ca.judacribz.gainzassist.activities.add_workout

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*
import butterknife.*
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.adapters.SingleItemAdapter
import ca.judacribz.gainzassist.constants.ExerciseConst.BB_MIN_WEIGHT
import ca.judacribz.gainzassist.constants.ExerciseConst.BB_WEIGHT_CHANGE
import ca.judacribz.gainzassist.constants.ExerciseConst.DB_MIN_WEIGHT
import ca.judacribz.gainzassist.constants.ExerciseConst.DB_WEIGHT_CHANGE
import ca.judacribz.gainzassist.constants.ExerciseConst.MIN_WEIGHT
import ca.judacribz.gainzassist.constants.ExerciseConst.WEIGHT_CHANGE
import ca.judacribz.gainzassist.models.Exercise
import ca.judacribz.gainzassist.models.Exercise.Companion.EQUIPMENT_TYPES
import ca.judacribz.gainzassist.models.Exercise.SetsType.MAIN_SET
import ca.judacribz.gainzassist.models.ExerciseSet
import ca.judacribz.gainzassist.models.Workout
import ca.judacribz.gainzassist.models.db.WorkoutViewModel
import ca.judacribz.gainzassist.util.Preferences.removeIncompleteSessionPref
import ca.judacribz.gainzassist.util.Preferences.removeIncompleteWorkoutPref
import ca.judacribz.gainzassist.util.UI.setInitView
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
        const val EXTRA_CALLING_ACTIVITY = "ca.judacribz.gainzassist.activities.add_workout.EXTRA_CALLING_ACTIVITY"

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
    var workoutViewModel: WorkoutViewModel? = null

    @BindView(R.id.et_workout_name)
    lateinit var etWorkoutName: EditText

    @BindView(R.id.rv_exercise_btns)
    lateinit var rvSummary: RecyclerView

    @BindView(R.id.et_exercise_name)
    lateinit var etExerciseName: EditText

    @BindView(R.id.spr_equipment)
    lateinit var sprEquipment: Spinner

    @BindView(R.id.et_sets)
    lateinit var etNumSets: EditText

    @BindView(R.id.et_reps)
    lateinit var etNumReps: EditText

    @BindView(R.id.et_weight)
    lateinit var etWeight: EditText

    @BindView(R.id.btn_add_exercise)
    lateinit var btnAddExercise: Button

    @BindView(R.id.btn_update_exercise)
    lateinit var btnUpdateExercise: Button

    @BindView(R.id.btn_add_workout)
    lateinit var btnAddWorkout: Button

    @BindView(R.id.btn_discard_workout)
    lateinit var btnDiscardWorkout: Button

    @BindView(R.id.btn_clear_exercise)
    lateinit var btnClearExercise: Button

    @BindView(R.id.ibtn_dec_reps)
    lateinit var ibtnDecReps: ImageButton

    @BindView(R.id.ibtn_dec_weight)
    lateinit var ibtnDecWeight: ImageButton

    @BindView(R.id.ibtn_dec_sets)
    lateinit var ibtnDecSets: ImageButton

    private lateinit var formEntries: Array<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInitView(this, R.layout.activity_new_workout_summary, R.string.title_new_workout_summary, true)

        workoutViewModel = ViewModelProviders.of(this).get(WorkoutViewModel::class.java)

        val sourceIntent = intent
        workout = Parcels.unwrap(sourceIntent.getParcelableExtra(EXTRA_WORKOUT))
        workoutId = workout!!.id

        when (sourceIntent.getSerializableExtra(EXTRA_CALLING_ACTIVITY) as? CALLING_ACTIVITY) {
            CALLING_ACTIVITY.WORKOUTS_LIST -> {
                btnAddWorkout.text = getString(R.string.update_workout)
            }
            else -> {
                // Keep default
            }
        }

        formEntries = arrayOf(etExerciseName, etNumReps, etWeight, etNumSets)

        val workoutName = workout!!.name
        if (workoutName != null) {
            etWorkoutName.setText(workoutName)
        }

        setSpinnerWithArray(this, R.array.exerciseEquipment, sprEquipment)

        rvSummary.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        rvSummary.setHasFixedSize(true)

        exercises = workout!!.exercises

        updateAdapter()

        etWorkoutName.setText(workout!!.name)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    private fun updateAdapter() {
        exerciseAdapter = SingleItemAdapter(
            this,
            workout!!.exerciseNames,
            R.layout.part_square_button,
            R.id.sqrBtnListItem
        )
        exerciseAdapter!!.setItemClickObserver(this)
        rvSummary.adapter = exerciseAdapter
    }

    @OnItemSelected(R.id.spr_equipment)
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

        if (!ibtnDecWeight.isEnabled || weight < minWeight) {
            etWeight.setText(minWeight.toString())
        }
    }

    @OnTextChanged(value = [R.id.et_exercise_name], callback = OnTextChanged.Callback.TEXT_CHANGED)
    fun onExerciseNameChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        val exerciseName = s.toString()

        if (exerciseName.isNotEmpty()) {
            if (workout!!.containsExercise(exerciseName)) {
                switchExerciseBtns(btnAddExercise, btnUpdateExercise)
            } else {
                switchExerciseBtns(btnUpdateExercise, btnAddExercise)
            }
        } else {
            switchExerciseBtns(btnUpdateExercise, btnAddExercise)
        }
    }

    private fun switchExerciseBtns(btnDisable: Button, btnEnable: Button) {
        if (btnDisable.visibility == View.VISIBLE) {
            btnEnable.visibility = View.VISIBLE
            btnDisable.visibility = View.INVISIBLE
        }
    }

    @OnTextChanged(value = [R.id.et_weight], callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    fun beforeWeightChanged() {
        if (!ibtnDecWeight.isEnabled) {
            ibtnDecWeight.isEnabled = true
            ibtnDecWeight.visibility = View.VISIBLE
        }
    }

    @OnTextChanged(value = [R.id.et_weight], callback = OnTextChanged.Callback.TEXT_CHANGED)
    fun onWeightChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        val weightStr = s.toString()
        weight = if (weightStr.isEmpty()) minWeight else weightStr.toFloat()

        if (weight <= minWeight) {
            ibtnDecWeight.isEnabled = false
            ibtnDecWeight.visibility = View.INVISIBLE

            if (weight < minWeight) {
                etWeight.setText(minWeight.toString())
            }
        }
    }

    @OnTextChanged(value = [R.id.et_reps], callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    fun beforeRepsChanged() {
        beforeNumChanged(ibtnDecReps)
    }

    @OnTextChanged(value = [R.id.et_sets], callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    fun beforeSetsChanged() {
        beforeNumChanged(ibtnDecSets)
    }

    fun beforeNumChanged(ibtnDec: ImageButton) {
        if (!ibtnDec.isEnabled) {
            ibtnDec.isEnabled = true
            ibtnDec.visibility = View.VISIBLE
        }
    }

    @OnTextChanged(R.id.et_reps)
    fun onRepsChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        num_reps = onNumChanged(etNumReps, ibtnDecReps, s.toString())
    }

    @OnTextChanged(R.id.et_sets)
    fun onSetsChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        num_sets = onNumChanged(etNumSets, ibtnDecSets, s.toString())
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

    @OnClick(R.id.ibtn_inc_reps)
    fun incReps() {
        etNumReps.setText((getTextInt(etNumReps) + MIN_INT).toString())
    }

    @OnClick(R.id.ibtn_dec_reps)
    fun decReps() {
        etNumReps.setText(Math.max(getTextInt(etNumReps) - MIN_INT, MIN_INT).toString())
    }

    @OnClick(R.id.ibtn_inc_sets)
    fun incSets() {
        etNumSets.setText((getTextInt(etNumSets) + MIN_INT).toString())
    }

    @OnClick(R.id.ibtn_dec_sets)
    fun decSets() {
        etNumSets.setText(Math.max(getTextInt(etNumSets) - MIN_INT, MIN_INT).toString())
    }

    @OnClick(R.id.ibtn_inc_weight)
    fun incWeight() {
        etWeight.setText((getTextFloat(etWeight) + MIN_FLOAT).toString())
    }

    @OnClick(R.id.ibtn_dec_weight)
    fun decWeight() {
        etWeight.setText(Math.max(getTextFloat(etWeight) - MIN_FLOAT, MIN_FLOAT).toString())
    }

    @OnClick(R.id.btn_add_exercise)
    fun addExercise() {
        if (validateForm(this, formEntries)) {
            val exName = getTextString(etExerciseName)
            if (workout!!.containsExercise(exName)) {
                etExerciseName.error = getString(R.string.err_exercise_exists, exName)
            } else {
                exercises!!.add(updateExerciseData(-1, exName))
                updateAdapter()
            }
        }
    }

    @OnClick(R.id.btn_update_exercise)
    fun updateExercise() {
        if (validateForm(this, formEntries)) {
            val num = ex!!.exerciseNumber
            exercises!![num] = updateExerciseData(num, ex!!.name!!)
            updateAdapter()
        }
    }

    private fun updateExerciseData(exNumber: Int, exName: String): Exercise {
        var newExNumber = exNumber
        var id = -1L

        etExerciseName.setText("")

        if (newExNumber == -1) {
            newExNumber = workout!!.numExercises
            Logger.d(newExNumber)
        } else {
            id = ex!!.id
        }

        val exercise = Exercise(
            newExNumber,
            exName,
            "Strength",
            sprEquipment.selectedItem.toString().toLowerCase(),
            getTextInt(etNumSets),
            getTextInt(etNumReps),
            getTextFloat(etWeight),
            MAIN_SET
        )

        exercise.workoutId = workoutId

        if (id != -1L) {
            exercise.id = id
        }

        return exercise
    }

    @OnClick(R.id.btn_add_workout)
    fun addWorkout() {
        if (validateForm(this, arrayOf(etWorkoutName))) {
            if (exercises!!.isEmpty()) {
                Toast.makeText(this, "Error: No exercises added.", Toast.LENGTH_SHORT).show()
            } else {
                val btnText = getTextString(btnAddWorkout).toLowerCase()

                workout!!.name = getTextString(etWorkoutName)
                workout!!.exercises = exercises!!

                addWorkoutFirebase(workout!!)

                if ("add workout" == btnText) {
                    workoutViewModel!!.insertWorkout(workout!!)
                } else {
                    workoutViewModel!!.updateWorkout(workout!!)

                    if (removeIncompleteWorkoutPref(this, workout!!.name!!)) {
                        removeIncompleteSessionPref(this, workout!!.name!!)
                    }
                }

                discardWorkout()
            }
        }
    }

    @OnClick(R.id.btn_clear_exercise)
    fun clearExercise() {
        clearFormEntry(etExerciseName)
        etNumReps.setText(getString(R.string.starting_reps))
        etNumSets.setText(getString(R.string.starting_sets))
        etWeight.setText(getString(R.string.starting_weight))
        sprEquipment.setSelection(0)
    }

    @OnClick(R.id.btn_discard_workout)
    fun discardWorkout() {
        setResult(RESULT_OK)
        finish()
    }

    override fun onItemClick(view: View?) {
        updateExerciseArea(getTextString(view as TextView))
    }

    var ex: Exercise? = null
    private fun updateExerciseArea(exName: String) {
        ex = workout!!.getExerciseFromName(exName)
        etExerciseName.setText(exName)
        etNumSets.setText(ex!!.sets.toString())
        etNumReps.setText(ex!!.reps.toString())
        etWeight.setText(ex!!.weight.toString())
        sprEquipment.setSelection(EQUIPMENT_TYPES.indexOf(ex!!.equipment))
    }

    override fun onItemLongClick(view: View?) {
        // Keep Java behavior: no-op.
    }
}
