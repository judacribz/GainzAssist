package ca.judacribz.gainzassist.activities.add_workout

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import butterknife.BindView
import butterknife.OnClick
import butterknife.OnFocusChange
import butterknife.OnTextChanged
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.constants.ExerciseConst.MIN_INT
import ca.judacribz.gainzassist.util.UI.getTextInt
import ca.judacribz.gainzassist.util.UI.getTextString
import ca.judacribz.gainzassist.util.UI.handleFocusLeft
import ca.judacribz.gainzassist.util.UI.handleNumChanged
import ca.judacribz.gainzassist.util.UI.setInitView
import ca.judacribz.gainzassist.util.UI.setText
import ca.judacribz.gainzassist.util.UI.setVisibleIfDisabled
import ca.judacribz.gainzassist.util.UI.validateFormEntry

class WorkoutEntry : AppCompatActivity() {
    // --------------------------------------------------------------------------------------------
    // Global Vars
    // --------------------------------------------------------------------------------------------
    var isEmpty = true
    var numExs = 0

    @BindView(R.id.et_workout_name)
    lateinit var etWorkoutName: EditText

    @BindView(R.id.et_num_exercises)
    lateinit var etNumExercises: EditText

    @BindView(R.id.ibtn_inc_exercises)
    lateinit var ibtnIncExercises: ImageButton

    @BindView(R.id.ibtn_dec_exercises)
    lateinit var ibtnDecExercises: ImageButton

    @BindView(R.id.btn_enter)
    lateinit var btnEnter: Button

    // --------------------------------------------------------------------------------------------
    // AppCompatActivity Override
    ///////////////////////////////////////////////////////////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInitView(this, R.layout.activity_workout_entry, R.string.add_workout, true)
        numExs = Integer.valueOf(getString(R.string.initial_num_exercises))
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        when (res) {
            RESULT_OK -> finish()
        }
    }

    //AppCompatActivity//Override//////////////////////////////////////////////////////////////////
    // TextWatcher Handling
    // =============================================================================================
    @OnTextChanged(
        value = [R.id.et_num_exercises],
        callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED
    )
    fun beforeNumExercisesChanged() {
        setVisibleIfDisabled(ibtnDecExercises)
    }

    @OnTextChanged(value = [R.id.et_num_exercises], callback = OnTextChanged.Callback.TEXT_CHANGED)
    fun onNumExercisesChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        numExs = handleNumChanged(ibtnDecExercises, s.toString(), MIN_INT).toInt()
    }

    @OnTextChanged(value = [R.id.et_workout_name], callback = OnTextChanged.Callback.TEXT_CHANGED)
    fun onWorkoutNameChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.toString().trim { it <= ' ' }.isEmpty()) {
            if (!isEmpty) {
                isEmpty = true
                btnEnter!!.setText(R.string.skip)
            }
        } else {
            if (isEmpty) {
                isEmpty = false
                btnEnter!!.setText(R.string.enter)
            }
        }
    }

    // =TextWatcher=Handling========================================================================
    // OnFocusChanged Handling
    // =============================================================================================
    @OnFocusChange(R.id.et_num_exercises)
    fun onFocusLeft(et: EditText?, hasFocus: Boolean) {
        if (!hasFocus) {
            handleFocusLeft(et, MIN_INT, numExs)
        }
    }

    // =OnFocusChanged=Handling=====================================================================
    // Click Handling
    // =============================================================================================
    /* Increase number of num_sets */
    @OnClick(R.id.ibtn_inc_exercises)
    fun incNumExs() {
        setText(etNumExercises, numExs + 1)
    }

    /* Decrease number of num_sets */
    @OnClick(R.id.ibtn_dec_exercises)
    fun decNumExs() {
        setText(etNumExercises, numExs - 1)
    }

    @OnClick(R.id.btn_enter)
    fun enterWorkoutName() {
        if (validateFormEntry(this, etNumExercises)) {
            val exercisesEntry = Intent(this, ExercisesEntryActivity::class.java)
            if (!isEmpty) {
                Toast.makeText(this, "" + getTextString(etWorkoutName), Toast.LENGTH_SHORT).show()
                exercisesEntry.putExtra(EXTRA_WORKOUT_NAME, getTextString(etWorkoutName))
            }
            exercisesEntry.putExtra(
                EXTRA_NUM_EXERCISES,
                Math.max(MIN_INT, getTextInt(etNumExercises))
            )
            startActivityForResult(exercisesEntry, REQ_EXERCISES_ENTRY)
        }
    }

    @OnClick(R.id.btn_cancel)
    fun cancelWorkout() {
        finish()
    } //=Click=Handling===============================================================================

    companion object {
        // Constants
        // --------------------------------------------------------------------------------------------
        const val REQ_EXERCISES_ENTRY = 1001
        const val EXTRA_WORKOUT_NAME =
            "ca.judacribz.gainzassist.activities.add_workout.EXTRA_WORKOUT"
        const val EXTRA_NUM_EXERCISES =
            "ca.judacribz.gainzassist.activities.add_workout.EXTRA_NUM_EXERCISES"
    }
}