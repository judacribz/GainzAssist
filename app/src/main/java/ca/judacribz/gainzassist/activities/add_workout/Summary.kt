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
import ca.judacribz.gainzassist.constants.ExerciseConst.*
import ca.judacribz.gainzassist.models.Exercise
import ca.judacribz.gainzassist.models.Exercise.EQUIPMENT_TYPES
import ca.judacribz.gainzassist.models.Exercise.SetsType.MAIN_SET
import ca.judacribz.gainzassist.models.ExerciseSet
import ca.judacribz.gainzassist.models.Workout
import ca.judacribz.gainzassist.models.db.WorkoutViewModel
import ca.judacribz.gainzassist.util.Preferences.removeIncompleteSessionPref
import ca.judacribz.gainzassist.util.Preferences.removeIncompleteWorkoutPref
import ca.judacribz.gainzassist.util.UI.*
import ca.judacribz.gainzassist.util.firebase.Database.addWorkoutFirebase
import com.orhanobut.logger.Logger
import org.parceler.Parcels
import java.util.*

class Summary : AppCompatActivity(), SingleItemAdapter.ItemClickObserver {

    companion object {
        const val EXTRA_WORKOUT = "ca.judacribz.gainzassist.activities.add_workout.EXTRA_WORKOUT"
        const val EXTRA_CALLING_ACTIVITY = "ca.judacribz.gainzassist.activities.add_workout.EXTRA_CALLING_ACTIVITY"

        private const val MIN_INT_SUMMARY = 1
        private const val MIN_FLOAT_SUMMARY = 5.0f
        private const val POS_STREN = 0
        private const val POS_CARDIO = 1
        private const val POS_NA = 2
    }

    enum class CALLING_ACTIVITY {
        WORKOUTS_LIST,
        EXERCISES_ENTRY
    }

    var exerciseAdapter: SingleItemAdapter? = null
    var exercises: ArrayList<Exercise>? = null
    var exSets: ArrayList<ExerciseSet>? = null
    var workout: Workout? = null
    var workoutId: Long = 0

    var num_reps: Int = 0
    var num_sets: Int = 0
    var minInt = 1
    var weight: Float = 0.toFloat()
    var minWeight: Float = 0.toFloat()
    var weightChange: Float = 0.toFloat()

    var workoutViewModel: WorkoutViewModel? = null

    @BindView(R.id.et_workout_name)
    lateinit var etWorkoutName: EditText

    @BindView(R.id.et_exercise_name)
    lateinit var etExerciseName: EditText

    @BindView(R.id.spr_equipment)
    lateinit var sprEquipment: Spinner

    @BindView(R.id.et_reps)
    lateinit var etNumReps: EditText

    @BindView(R.id.et_weight)
    lateinit var etWeight: EditText

    @BindView(R.id.et_sets)
    lateinit var etNumSets: EditText

    @BindView(R.id.ibtn_dec_reps)
    lateinit var ibtnDecReps: ImageButton

    @BindView(R.id.ibtn_dec_weight)
    lateinit var ibtnDecWeight: ImageButton

    @BindView(R.id.ibtn_dec_sets)
    lateinit var ibtnDecSets: ImageButton

    @BindView(R.id.btn_add_exercise)
    lateinit var btnAddExercise: Button

    @BindView(R.id.btn_update_exercise)
    lateinit var btnUpdateExercise: Button

    @BindView(R.id.btn_add_workout)
    lateinit var btnAddWorkout: Button

    @BindView(R.id.rv_exercise_btns)
    lateinit var rvExerciseList: RecyclerView

    lateinit var formEntries: Array<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInitView(this, R.layout.activity_new_workout_summary, R.string.title_new_workout_summary, true)

        workoutViewModel = ViewModelProviders.of(this).get(WorkoutViewModel::class.java)

        val intent = intent
        workout = Parcels.unwrap<Workout>(getIntent().getParcelableExtra(EXTRA_WORKOUT))
        workoutId = workout!!.id

        if (intent.getSerializableExtra(EXTRA_CALLING_ACTIVITY) == CALLING_ACTIVITY.WORKOUTS_LIST) {
            btnAddWorkout.setText(getString(R.string.update_workout))
        }

        formEntries = arrayOf(etExerciseName, etNumReps, etWeight, etNumSets)

        val workoutName = workout!!.name
        if (workoutName != null) {
            etWorkoutName.setText(workoutName)
        }

        setSpinnerWithArray(this, R.array.exerciseEquipment, sprEquipment)

        rvExerciseList.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        rvExerciseList.setHasFixedSize(true)

        exercises = workout!!.exercises
        updateAdapter()
        etWorkoutName.setText(workout!!.name)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
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

            if (weight < minWeight)
                etWeight.setText(minWeight.toString())
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
        val value = if (str.isEmpty()) minInt else Integer.valueOf(str)

        if (value <= minInt) {
            ibtnDec.isEnabled = false
            ibtnDec.visibility = View.INVISIBLE

            if (value < minInt)
                etNum.setText(minInt.toString())
        }

        return value
    }

    override fun onItemClick(view: View) {
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

    override fun onItemLongClick(view: View) {}

    @OnClick(R.id.ibtn_inc_reps)
    fun incReps() {
        etNumReps.setText((getTextInt(etNumReps) + minInt).toString())
    }

    @OnClick(R.id.ibtn_dec_reps)
    fun decReps() {
        etNumReps.setText(Math.max(getTextInt(etNumReps) - minInt, minInt).toString())
    }

    @OnClick(R.id.ibtn_inc_sets)
    fun incSets() {
        etNumSets.setText((getTextInt(etNumSets) + minInt).toString())
    }

    @OnClick(R.id.ibtn_dec_sets)
    fun decSets() {
        etNumSets.setText(Math.max(getTextInt(etNumSets) - minInt, minInt).toString())
    }

    @OnClick(R.id.ibtn_inc_weight)
    fun incWeight() {
        etWeight.setText((getTextFloat(etWeight) + MIN_FLOAT_SUMMARY).toString())
    }

    @OnClick(R.id.ibtn_dec_weight)
    fun decWeight() {
        etWeight.setText(Math.max(getTextFloat(etWeight) - MIN_FLOAT_SUMMARY, MIN_FLOAT_SUMMARY).toString())
    }

    @OnClick(R.id.btn_add_exercise)
    fun addExercise() {
        if (validateForm(this, formEntries)) {
            val exName = getTextString(etExerciseName)
            if (workout!!.containsExercise(exName)) {
                etExerciseName.error = getString(R.string.err_exercise_exists)
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
            exercises!![num] = updateExerciseData(num, ex!!.name)
            updateAdapter()
        }
    }

    private fun updateExerciseData(exNumber: Int, exName: String): Exercise {
        var newExNumber = exNumber
        val exercise: Exercise
        var id: Long = -1
        etExerciseName.setText("")
        if (newExNumber == -1) {
            newExNumber = workout!!.numExercises
            Logger.d(newExNumber)
        } else {
            id = ex!!.id
        }

        exercise = Exercise(
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

    fun updateAdapter() {
        exerciseAdapter = SingleItemAdapter(
            this,
            workout!!.exerciseNames,
            R.layout.part_square_button,
            R.id.sqrBtnListItem
        )
        exerciseAdapter!!.setItemClickObserver(this)
        rvExerciseList.adapter = exerciseAdapter
    }

    @OnClick(R.id.btn_add_workout)
    fun addWorkout() {
        if (validateForm(this, arrayOf(etWorkoutName))) {
            if (exercises!!.isEmpty()) {
                Toast.makeText(this, "Error: No exercises added.", Toast.LENGTH_SHORT).show()
            } else {
                val btnText = getTextString(btnAddWorkout).toLowerCase()
                workout!!.name = getTextString(etWorkoutName)
                workout!!.exercises = exercises
                addWorkoutFirebase(workout)
                if ("add workout" == btnText) {
                    workoutViewModel!!.insertWorkout(workout)
                } else {
                    workoutViewModel!!.updateWorkout(workout)
                    if (removeIncompleteWorkoutPref(this, workout!!.name)) {
                        removeIncompleteSessionPref(this, workout!!.name)
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

    @OnItemSelected(value = [R.id.spr_equipment], callback = OnItemSelected.Callback.NOTHING_SELECTED)
    fun changeSprType(spr: Spinner) {
        Toast.makeText(this, spr.getItemAtPosition(POS_NA).toString(), Toast.LENGTH_SHORT).show()
    }
}
