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

    var workout: Workout? = null
    var exercises: ArrayList<Exercise>? = null
    var workoutViewModel: WorkoutViewModel? = null
    var adapter: SingleItemAdapter? = null

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

    @BindView(R.id.ibtn_dec_reps)
    lateinit var ibtnDecReps: ImageButton

    @BindView(R.id.ibtn_dec_weight)
    lateinit var ibtnDecWeight: ImageButton

    @BindView(R.id.ibtn_dec_sets)
    lateinit var ibtnDecSets: ImageButton

    @BindViews(R.id.et_exercise_name, R.id.et_weight, R.id.et_reps, R.id.et_sets)
    lateinit var formEntries: List<@JvmSuppressWildcards EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInitView(this, R.layout.activity_new_workout_summary, R.string.title_new_workout_summary, true)
        workoutViewModel = ViewModelProviders.of(this).get(WorkoutViewModel::class.java)

        val bundle = intent.extras
        if (bundle != null) {
            workout = Parcels.unwrap(bundle.getParcelable(ca.judacribz.gainzassist.activities.main.Main.EXTRA_WORKOUT))
            exercises = workout!!.exercises
        }

        rvSummary.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvSummary.setHasFixedSize(true)

        updateAdapter()

        setSpinnerWithArray(this, R.array.exerciseEquipment, sprEquipment)
    }

    private fun updateAdapter() {
        adapter = SingleItemAdapter(
            this,
            workout!!.exerciseNames,
            R.layout.part_text_view_progress,
            R.id.tv_progress
        )
        adapter!!.setItemClickObserver(this)
        rvSummary.adapter = adapter
    }

    @OnItemSelected(R.id.spr_equipment)
    fun equipmentSelected(position: Int) {
        // Implementation if needed
    }

    @OnClick(R.id.ibtn_inc_reps)
    fun incReps() {
        setTextInt(etNumReps, getTextInt(etNumReps) + 1)
    }

    @OnClick(R.id.ibtn_dec_reps)
    fun decReps() {
        setTextInt(etNumReps, getTextInt(etNumReps) - 1)
    }

    @OnClick(R.id.ibtn_inc_sets)
    fun incSets() {
        setTextInt(etNumSets, getTextInt(etNumSets) + 1)
    }

    @OnClick(R.id.ibtn_dec_sets)
    fun decSets() {
        setTextInt(etNumSets, getTextInt(etNumSets) - 1)
    }

    @OnClick(R.id.ibtn_inc_weight)
    fun incWeight() {
        setTextFloat(etWeight, getTextFloat(etWeight) + 5)
    }

    @OnClick(R.id.ibtn_dec_weight)
    fun decWeight() {
        setTextFloat(etWeight, getTextFloat(etWeight) - 5)
    }

    private fun setTextInt(et: EditText, value: Int) {
        et.setText(value.toString())
    }

    private fun setTextFloat(et: EditText, value: Float) {
        et.setText(value.toString())
    }

    @OnClick(R.id.btn_add_exercise)
    fun addExercise() {
        if (validateForm(this, formEntries.toTypedArray())) {
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
        if (validateForm(this, formEntries.toTypedArray())) {
            val num = ex!!.exerciseNumber
            exercises!![num] = updateExerciseData(num, ex!!.name!!)
            updateAdapter()
        }
    }

    private fun updateExerciseData(exNumber: Int, exName: String): Exercise {
        var newExNumber = exNumber
        etExerciseName.setText("")
        if (newExNumber == -1) {
            newExNumber = workout!!.numExercises
            Logger.d(newExNumber)
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
        return exercise
    }

    @OnClick(R.id.btn_add_workout)
    fun createWorkout() {
        if (exercises!!.isNotEmpty()) {
            val exs = exercises
            if (exs != null) {
                workout!!.exercises = exs
            }
            workoutViewModel!!.insertWorkout(workout!!)
            addWorkoutFirebase(workout!!)
            if (removeIncompleteWorkoutPref(this, workout!!.name!!)) {
                removeIncompleteSessionPref(this, workout!!.name!!)
            }
            val intent = Intent(this, ca.judacribz.gainzassist.activities.main.Main::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        } else {
            Toast.makeText(this, "Add at least one exercise", Toast.LENGTH_SHORT).show()
        }
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
        val exName = getTextString(view as TextView)
        val exercise = workout!!.getExerciseFromName(exName)
        if (exercise != null) {
            workout!!.removeExercise(exercise)
            exercises = workout!!.exercises
            clearFormEntry(etExerciseName)
            updateAdapter()
        }
    }
}
