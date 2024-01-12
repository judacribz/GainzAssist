package ca.judacribz.gainzassist.activities.add_workout

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.OnClick
import butterknife.OnItemSelected
import butterknife.OnTextChanged
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.adapters.SingleItemAdapter
import ca.judacribz.gainzassist.adapters.SingleItemAdapter.ItemClickObserver
import ca.judacribz.gainzassist.constants.ExerciseConst.BB_MIN_WEIGHT
import ca.judacribz.gainzassist.constants.ExerciseConst.BB_WEIGHT_CHANGE
import ca.judacribz.gainzassist.constants.ExerciseConst.DB_MIN_WEIGHT
import ca.judacribz.gainzassist.constants.ExerciseConst.DB_WEIGHT_CHANGE
import ca.judacribz.gainzassist.constants.ExerciseConst.MIN_WEIGHT
import ca.judacribz.gainzassist.constants.ExerciseConst.WEIGHT_CHANGE
import ca.judacribz.gainzassist.models.Exercise
import ca.judacribz.gainzassist.models.Exercise.Companion.EQUIPMENT_TYPES
import ca.judacribz.gainzassist.models.ExerciseSet
import ca.judacribz.gainzassist.models.Workout
import ca.judacribz.gainzassist.models.db.WorkoutViewModel
import ca.judacribz.gainzassist.util.Preferences.removeIncompleteSessionPref
import ca.judacribz.gainzassist.util.Preferences.removeIncompleteWorkoutPref
import ca.judacribz.gainzassist.util.UI.clearFormEntry
import ca.judacribz.gainzassist.util.UI.getTextFloat
import ca.judacribz.gainzassist.util.UI.getTextInt
import ca.judacribz.gainzassist.util.UI.getTextString
import ca.judacribz.gainzassist.util.UI.setInitView
import ca.judacribz.gainzassist.util.UI.setSpinnerWithArray
import ca.judacribz.gainzassist.util.UI.validateForm
import ca.judacribz.gainzassist.util.firebase.Database.addWorkoutFirebase
import com.orhanobut.logger.Logger
import java.util.Locale

class Summary : AppCompatActivity(), ItemClickObserver {
    enum class CALLING_ACTIVITY {
        WORKOUTS_LIST,
        EXERCISES_ENTRY
    }

    // --------------------------------------------------------------------------------------------
    // Global Vars
    // --------------------------------------------------------------------------------------------
    var exerciseAdapter: SingleItemAdapter? = null
    var exercises: ArrayList<Exercise>? = null
    var exSets: ArrayList<ExerciseSet>? = null
    var workout: Workout? = null
    var workoutId: Long = 0
    var num_reps = 0
    var num_sets = 0
    var minInt = 1 // for min num_reps/num_sets
    var weight = 0f
    var minWeight = 0f
    var weightChange = 0f
    val workoutViewModel: WorkoutViewModel by viewModels()

    // UI Elements
    @BindView(R.id.et_workout_name)
    lateinit var etWorkoutName: EditText

    @BindView(R.id.et_exercise_name)
    lateinit var etExerciseName: EditText

    //    @BindView(R.id.spr_type) Spinner sprType;
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

    // --------------------------------------------------------------------------------------------
    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInitView(
            this,
            R.layout.activity_new_workout_summary,
            R.string.title_new_workout_summary,
            true
        )
        val intent = intent
        workout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getIntent().getParcelableExtra(EXTRA_WORKOUT, Workout::class.java)
        } else {
            @Suppress("DEPRECATION")
            getIntent().getParcelableExtra(EXTRA_WORKOUT)
        }
        workout?.getId()?.let { workoutId = it }
        when (intent.getSerializableExtra(EXTRA_CALLING_ACTIVITY) as CALLING_ACTIVITY?) {
            CALLING_ACTIVITY.WORKOUTS_LIST -> btnAddWorkout?.text =
                getString(R.string.update_workout)

            else -> Unit
        }
        formEntries = arrayOf(etExerciseName, etNumReps, etWeight, etNumSets)
        val workoutName = workout?.name
        if (workoutName != null) {
            etWorkoutName?.setText(workoutName)
        }
        setSpinnerWithArray(this, R.array.exerciseEquipment, sprEquipment)

        // ExerciseSet the layout manager
        rvExerciseList.setLayoutManager(
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        )
        rvExerciseList.setHasFixedSize(true)
        exercises = workout?.exercises.orEmpty() as ArrayList<Exercise>
        updateAdapter()
        etWorkoutName?.setText(workout?.name)
    }

    /* Toolbar back arrow handling */
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    //AppCompatActivity//Override//////////////////////////////////////////////////////////////////
    @OnItemSelected(R.id.spr_equipment)
    fun equipmentSelected(spinner: Spinner?, position: Int) {
        when (position) {
            0 -> {
                minWeight = BB_MIN_WEIGHT
                weightChange = BB_WEIGHT_CHANGE
            }

            1 -> {
                minWeight = DB_MIN_WEIGHT
                weightChange = DB_WEIGHT_CHANGE
                minWeight = MIN_WEIGHT
                weightChange = WEIGHT_CHANGE
            }

            else -> {
                minWeight = MIN_WEIGHT
                weightChange = WEIGHT_CHANGE
            }
        }
        if (ibtnDecWeight.isEnabled.not() || weight < minWeight) {
            etWeight.setText(minWeight.toString())
        }
    }

    // TextWatcher Handling
    // =============================================================================================
    @OnTextChanged(value = [R.id.et_exercise_name], callback = OnTextChanged.Callback.TEXT_CHANGED)
    fun onExerciseNameChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        val exerciseName = s.toString()
        if (!exerciseName.isEmpty()) {
            if (workout?.containsExercise(exerciseName) == true) {
                switchExerciseBtns(btnAddExercise, btnUpdateExercise)
            } else {
                switchExerciseBtns(btnUpdateExercise, btnAddExercise)
            }
        } else {
            switchExerciseBtns(btnUpdateExercise, btnAddExercise)
        }
    }

    private fun switchExerciseBtns(btnDisable: Button?, btnEnable: Button?) {
        if (btnDisable?.visibility == View.VISIBLE) {
            btnEnable?.visibility = View.VISIBLE
            btnDisable.visibility = View.INVISIBLE
        }
    }

    @OnTextChanged(value = [R.id.et_weight], callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    fun beforeNumExercisesChanged() {
        if (!ibtnDecWeight.isEnabled) {
            ibtnDecWeight?.isEnabled = true
            ibtnDecWeight.visibility = View.VISIBLE
        }
    }

    @OnTextChanged(value = [R.id.et_weight], callback = OnTextChanged.Callback.TEXT_CHANGED)
    fun onNumExercisesChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        val weightStr = s.toString()
        weight = if (weightStr.isEmpty()) minWeight else java.lang.Float.valueOf(weightStr)
        if (weight <= minWeight) {
            ibtnDecWeight?.isEnabled = false
            ibtnDecWeight?.visibility = View.INVISIBLE
            if (weight < minWeight) etWeight?.setText(minWeight.toString())
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

    fun beforeNumChanged(ibtnDec: ImageButton?) {
        if (ibtnDec?.isEnabled?.not() ?: false) {
            ibtnDec?.isEnabled = true
            ibtnDec?.visibility = View.VISIBLE
        }
    }

    @OnTextChanged(R.id.et_reps)
    fun onRepsChanged(
        s: CharSequence,
        start: Int,
        before: Int,
        count: Int
    ) {
        num_reps = onNumChanged(etNumReps, ibtnDecReps, s.toString())
    }

    @OnTextChanged(R.id.et_sets)
    fun onSetsChanged(
        s: CharSequence,
        start: Int,
        before: Int,
        count: Int
    ) {
        num_sets = onNumChanged(etNumSets, ibtnDecSets, s.toString())
    }

    fun onNumChanged(etNum: EditText?, ibtnDec: ImageButton?, str: String): Int {
        val value = if (str.isEmpty()) minInt else Integer.valueOf(str)
        if (value <= minInt) {
            ibtnDec?.isEnabled = false
            ibtnDec?.visibility = View.INVISIBLE
            if (value < minInt) etNum?.setText(minInt.toString())
        }
        return value
    }

    // =TextWatcher=Handling========================================================================
    // SingleItemAdapter.ItemClickObserver override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun onItemClick(view: View) {
        updateExerciseArea(getTextString(view as TextView))
    }

    var ex: Exercise? = null
    private fun updateExerciseArea(exName: String) {
        ex = workout?.getExerciseFromName(exName)
        etExerciseName?.setText(exName)
        etNumSets?.setText(ex?.sets.toString())
        etNumReps?.setText(ex?.reps.toString())
        etWeight?.setText(ex?.weight.toString())
        sprEquipment?.setSelection(EQUIPMENT_TYPES.indexOf(ex?.equipment))
    }

    override fun onItemLongClick(view: View) {}

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Click Handling
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /* Increase number of num_reps */
    @OnClick(R.id.ibtn_inc_reps)
    fun incReps() {
        etNumReps.setText(java.lang.String.valueOf(getTextInt(etNumReps) + MIN_INT))
    }

    /* Decrease number of num_reps */
    @OnClick(R.id.ibtn_dec_reps)
    fun decReps() {
        etNumReps?.setText(
            java.lang.String.valueOf(
                Math.max(
                    getTextInt(etNumReps) - MIN_INT,
                    MIN_INT
                )
            )
        )
    }

    /* Increase number of num_sets */
    @OnClick(R.id.ibtn_inc_sets)
    fun incSets() {
        etNumSets.setText(java.lang.String.valueOf(getTextInt(etNumSets) + MIN_INT))
    }

    /* Decrease number of num_sets */
    @OnClick(R.id.ibtn_dec_sets)
    fun decSets() {
        etNumSets?.setText(
            java.lang.String.valueOf(
                Math.max(
                    getTextInt(etNumSets) - MIN_INT,
                    MIN_INT
                )
            )
        )
    }

    /* Increase weight */
    @OnClick(R.id.ibtn_inc_weight)
    fun incWeight() {
        etWeight.setText(java.lang.String.valueOf(getTextFloat(etWeight) + MIN_FLOAT))
    }

    /* Decrease weight*/
    @OnClick(R.id.ibtn_dec_weight)
    fun decWeight() {
        etWeight?.setText(
            java.lang.String.valueOf(
                Math.max(
                    getTextFloat(etWeight) - MIN_FLOAT,
                    MIN_FLOAT
                )
            )
        )
    }

    /* Adds exercise to exercises ArrayList and updates the exercises GridLayout display */
    @OnClick(R.id.btn_add_exercise)
    fun addExercise() {
        if (validateForm(this, formEntries)) {
            val exName: String = getTextString(etExerciseName)
            if (workout?.containsExercise(exName) == true) {
                etExerciseName?.error = getString(R.string.err_exercise_exists)
            } else {
                exercises?.add(updateExerciseData(-1, exName))
                updateAdapter()
            }
        }
    }

    @OnClick(R.id.btn_update_exercise)
    fun updateExercise() {
        if (validateForm(this, formEntries)) {
            ex?.apply {
                exerciseNumber.let { num ->
                    exercises!![num!!] = updateExerciseData(num, name.toString())
                    updateAdapter()
                }
            }
        }
    }

    private fun updateExerciseData(exNumber: Int, exName: String): Exercise {
        var exNumber = exNumber
        val exercise: Exercise
        var id: Long = -1
        etExerciseName?.setText("")
        if (exNumber == -1) {
            exNumber = workout?.numExercises ?: -1
            Logger.d(exNumber)
        } else {
            id = ex?.id ?: -1
        }
        exercise = Exercise(
            exNumber,
            exName,
            "Strength",
            sprEquipment?.selectedItem.toString().lowercase(Locale.getDefault()),
            getTextInt(etNumSets),
            getTextInt(etNumReps),
            getTextFloat(etWeight),
            Exercise.SetsType.MAIN_SET
        )
        exercise.workoutId = workoutId
        if (id != -1L) {
            exercise.id = id
        }
        return exercise
    }

    /* Misc function to update the GridLayout exercises display */
    fun updateAdapter() {
        exerciseAdapter = SingleItemAdapter(
            this,
            workout?.exerciseNames,
            R.layout.part_square_button,
            R.id.sqrBtnListItem
        )
        exerciseAdapter?.setItemClickObserver(this)
        rvExerciseList.setAdapter(exerciseAdapter)
    }

    /* Adds workout to exercises ArrayList and updates exercises GridLayout display */
    @OnClick(R.id.btn_add_workout)
    fun addWorkout() {
        if (validateForm(this, arrayOf<EditText?>(etWorkoutName))) {
            if (exercises.isNullOrEmpty()) {
                Toast.makeText(this, "Error: No exercises added.", Toast.LENGTH_SHORT).show()
            } else {
                val btnText: String = getTextString(btnAddWorkout).toLowerCase()
                workout?.name = getTextString(etWorkoutName)
                workout?.exercises = exercises.orEmpty() as ArrayList<Exercise>
                workout?.let { addWorkoutFirebase(it) }
                if ("add workout" == btnText) {
                    workoutViewModel?.insertWorkout(workout)
                } else {
                    workoutViewModel?.updateWorkout(workout)
                    if (removeIncompleteWorkoutPref(this, workout?.name)) {
                        removeIncompleteSessionPref(this, workout?.name)
                    }
                }
                discardWorkout()
            }
        }
    }

    /* Adds workout to exercises ArrayList and updates exercises GridLayout display */
    @OnClick(R.id.btn_clear_exercise)
    fun clearExercise() {
        clearFormEntry(etExerciseName)
        etNumReps?.setText(getString(R.string.starting_reps))
        etNumSets?.setText(getString(R.string.starting_sets))
        etWeight?.setText(getString(R.string.starting_weight))
        sprEquipment?.setSelection(0)
    }

    /* Adds workout to exercises ArrayList and updates exercises GridLayout display */
    @OnClick(R.id.btn_discard_workout)
    fun discardWorkout() {
        setResult(RESULT_OK)
        finish()
    }

    @OnItemSelected(R.id.spr_equipment)
    fun changeSprType(spr: Spinner) {
        Toast.makeText(this, spr.getItemAtPosition(POS_NA).toString(), Toast.LENGTH_SHORT).show()
    } ///////////////////////////////////////////////////////////////////////////////////////////////

    companion object {
        // Constants
        // --------------------------------------------------------------------------------------------
        const val EXTRA_WORKOUT = "ca.judacribz.gainzassist.activities.add_workout.EXTRA_WORKOUT"
        const val EXTRA_CALLING_ACTIVITY =
            "ca.judacribz.gainzassist.activities.add_workout.EXTRA_CALLING_ACTIVITY"
        private const val MIN_INT = 1
        private const val MIN_FLOAT = 5.0f
        private const val POS_STREN = 0
        private const val POS_CARDIO = 1
        private const val POS_NA = 2
    }
}
