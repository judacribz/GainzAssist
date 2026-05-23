package ca.judacribz.gainzassist.activities.add_workout

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.constants.ExerciseConst.MIN_INT
import ca.judacribz.gainzassist.databinding.ActivityWorkoutEntryBinding
import ca.judacribz.gainzassist.util.UI.setInitTheme
import ca.judacribz.gainzassist.util.UI.setToolbar
import ca.judacribz.gainzassist.util.UI.setVisibleIfDisabled
import ca.judacribz.gainzassist.util.UI.handleNumChanged
import ca.judacribz.gainzassist.util.UI.handleFocusLeft
import ca.judacribz.gainzassist.util.UI.setText
import ca.judacribz.gainzassist.util.UI.validateFormEntry
import ca.judacribz.gainzassist.util.UI.getTextString
import ca.judacribz.gainzassist.util.UI.getTextInt

class WorkoutEntry : AppCompatActivity() {

    companion object {
        const val REQ_EXERCISES_ENTRY = 1001
        const val EXTRA_WORKOUT_NAME = "ca.judacribz.gainzassist.activities.add_workout.EXTRA_WORKOUT"
        const val EXTRA_NUM_EXERCISES = "ca.judacribz.gainzassist.activities.add_workout.EXTRA_NUM_EXERCISES"
    }

    var isEmpty = true
    var numExs: Int = 0

    private lateinit var binding: ActivityWorkoutEntryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInitTheme(this)
        binding = ActivityWorkoutEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setToolbar(this, R.string.add_workout, true)

        numExs = getString(R.string.initial_num_exercises).toInt()

        setupListeners()
    }

    private fun setupListeners() {
        binding.partNumExercises.etNumExercises.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                setVisibleIfDisabled(binding.ibtnDecExercises)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                numExs = handleNumChanged(binding.ibtnDecExercises, s.toString(), MIN_INT).toInt()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.partWorkoutName.etWorkoutName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().trim { it <= ' ' }.isEmpty()) {
                    if (!isEmpty) {
                        isEmpty = true
                        binding.btnEnter.setText(R.string.skip)
                    }
                } else {
                    if (isEmpty) {
                        isEmpty = false
                        binding.btnEnter.setText(R.string.enter)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.partNumExercises.etNumExercises.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus && v is EditText) {
                handleFocusLeft(v, MIN_INT, numExs)
            }
        }

        binding.ibtnIncExercises.setOnClickListener { incNumExs() }
        binding.ibtnDecExercises.setOnClickListener { decNumExs() }
        binding.btnEnter.setOnClickListener { enterWorkoutName() }
        binding.btnCancel.setOnClickListener { cancelWorkout() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        if (res == RESULT_OK) {
            finish()
        }
    }

    fun incNumExs() {
        setText(binding.partNumExercises.etNumExercises, numExs + 1)
    }

    fun decNumExs() {
        setText(binding.partNumExercises.etNumExercises, numExs - 1)
    }

    fun enterWorkoutName() {
        if (validateFormEntry(this, binding.partNumExercises.etNumExercises)) {
            val exercisesEntry = Intent(this, ExercisesEntry::class.java)

            if (!isEmpty) {
                Toast.makeText(this, "" + getTextString(binding.partWorkoutName.etWorkoutName), Toast.LENGTH_SHORT).show()
                exercisesEntry.putExtra(EXTRA_WORKOUT_NAME, getTextString(binding.partWorkoutName.etWorkoutName))
            }

            exercisesEntry.putExtra(EXTRA_NUM_EXERCISES, Math.max(MIN_INT, getTextInt(binding.partNumExercises.etNumExercises)))
            startActivityForResult(exercisesEntry, REQ_EXERCISES_ENTRY)
        }
    }

    fun cancelWorkout() {
        finish()
    }
}
